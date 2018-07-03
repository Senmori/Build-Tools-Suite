package net.senmori.btsuite.task;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.Gson;
import difflib.DiffUtils;
import difflib.Patch;
import javafx.concurrent.Task;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.buildtools.BuildTools;
import net.senmori.btsuite.buildtools.VersionInfo;
import net.senmori.btsuite.settings.Settings;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.GitUtil;
import net.senmori.btsuite.util.HashChecker;
import net.senmori.btsuite.util.ProcessRunner;
import net.senmori.btsuite.util.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class BuildToolsExecutor extends Task<Boolean> {
    private static final long MAX_SEC_WAIT = 10L;

    private final BuildTools options;
    private final ExecutorService threadPool;
    private final File BT_WORK_DIR;
    private final Settings settings;

    public BuildToolsExecutor(BuildTools options, ExecutorService threadPool, Settings settings) {
        this.options = options;
        this.threadPool = threadPool;
        this.BT_WORK_DIR = Main.WORK_DIR;
        this.settings = settings;
    }

    @Override
    protected Boolean call() throws Exception {
        String SHELL_PREFIX = "sh";
        File work = new File(BT_WORK_DIR, "work");
        createDir(work);
        if ( options.getOutputDirectories().isEmpty() ) {
            options.getOutputDirectories().add(BT_WORK_DIR.getPath()); // always have the WORK dir as an output
        }
        printOptions(options);

        File bukkit = new File(Main.WORK_DIR, "Bukkit");
        if ( ! bukkit.exists() ) {
            String repo = settings.getStashRepoLink() + "bukkit.git";
            GitCloneTask bukkitCloneTask = new GitCloneTask(repo, bukkit, options);
            threadPool.execute(bukkitCloneTask);
        }

        File craftBukkit = new File(Main.WORK_DIR, "CraftBukkit");
        if ( ! craftBukkit.exists() ) {
            String repo = settings.getStashRepoLink() + "craftbukkit.git";
            GitCloneTask cbCloneTask = new GitCloneTask(repo, craftBukkit, options);
            threadPool.submit(cbCloneTask);
        }

        File spigot = new File(Main.WORK_DIR, "Spigot");
        if ( ! spigot.exists() ) {
            String repo = settings.getStashRepoLink() + "spigot.git";
            GitCloneTask spigotCloneTask = new GitCloneTask(repo, spigot, options);
            threadPool.submit(spigotCloneTask);
        }

        File buildData = new File(Main.WORK_DIR, "BuildData");
        if ( ! buildData.exists() ) {
            String repo = settings.getStashRepoLink() + "builddata.git";
            GitCloneTask buildDataCloneTask = new GitCloneTask(repo, spigot, options);
            threadPool.submit(buildDataCloneTask);
        }

        File maven = new File(System.getenv("M2_HOME"));
        if ( ! maven.exists() ) {
            Future<File> task = Main.TASK_RUNNER.submit(new MavenInstaller()); // this should NEVER happen.
            do {
                ;
            } while ( ! task.isDone() );
            maven = task.get();
        }
        String mvn = maven.getAbsolutePath() + "/bin/mvn";

        Git bukkitGit = Git.open(bukkit);
        Git craftBukkitGit = Git.open(craftBukkit);
        Git spigotGit = Git.open(spigot);
        Git buildGit = Git.open(buildData);

        BuildInfo buildInfo = new BuildInfo("Dev Build", "Development", 0, new BuildInfo.Refs("master", "master", "master", "master"));

        if ( ! options.isDontUpdate() ) {
            String askedVersion = options.getVersion();
            System.out.println("Attempting to build version: '" + askedVersion + "' use --rev <version> to override");

            String text = askedVersion + ".json";
            File verInfo = new File(work, text);
            if ( ! verInfo.exists() ) {
                // download file
                String url = settings.getVersionLink() + askedVersion + ".json";
                FileDownloader fDown = new FileDownloader(url, verInfo);
                threadPool.submit(fDown);
            }
            System.out.println("Found version " + askedVersion);
            buildInfo = new Gson().fromJson(new FileReader(verInfo), BuildInfo.class);

            GitUtil.pull(buildGit, buildInfo.getRefs().getBuildData());
            GitUtil.pull(bukkitGit, buildInfo.getRefs().getBukkit());
            GitUtil.pull(craftBukkitGit, buildInfo.getRefs().getCraftBukkit());
            GitUtil.pull(spigotGit, buildInfo.getRefs().getSpigot());
        }

        VersionInfo versionInfo = new Gson().fromJson(new FileReader(new File("BuildData/info.json")), VersionInfo.class
        );
        // Default to 1.8 builds.
        if ( versionInfo == null ) {
            versionInfo = new VersionInfo("1.12.2", "bukkit-1.12.2.at", "bukkit-1.12.2-cl.csrg", "bukkit-1.12.2-members.csrg", "package.srg", null);
        }
        System.out.println("Attempting to build Minecraft with details: " + versionInfo);

        File vanillaJar = new File(Main.WORK_DIR, "minecraft_server." + versionInfo.getMinecraftVersion() + ".jar");
        if ( ! vanillaJar.exists() || ! HashChecker.checkHash(vanillaJar, versionInfo) ) {
            if ( versionInfo.getServerUrl() != null ) {
                FileDownloader fDown = new FileDownloader(versionInfo.getServerUrl(), vanillaJar);
                threadPool.submit(fDown);
            } else {
                String s3_url = String.format(settings.getFallbackServerUrl(), versionInfo.getMinecraftVersion());
                FileDownloader fDown = new FileDownloader(s3_url, vanillaJar);

                // Legacy versions can also specify a specific shell to build with which has to be bash-compatible
                SHELL_PREFIX = System.getenv().get("SHELL");
                if ( SHELL_PREFIX == null || SHELL_PREFIX.trim().isEmpty() ) {
                    SHELL_PREFIX = "bash";
                }
            }
        }
        if ( ! HashChecker.checkHash(vanillaJar, versionInfo) ) {
            System.err.println("**** Could not download clean Minecraft jar, giving up.");
            return false;
        }

        Iterable<RevCommit> mappings = buildGit.log()
                                               .addPath("mappings/" + versionInfo.getAccessTransforms())
                                               .addPath("mappings/" + versionInfo.getClassMappings())
                                               .addPath("mappings/" + versionInfo.getMemberMappings())
                                               .addPath("mappings/" + versionInfo.getPackageMappings())
                                               .setMaxCount(1).call();

        Hasher mappingsHash = Hashing.md5().newHasher();
        for ( RevCommit rev : mappings ) {
            mappingsHash.putString(rev.getName(), Charsets.UTF_8);
        }
        String mappingsVersion = mappingsHash.hash().toString().substring(24); // Last 8 chars

        File finalMappedJar = new File(Main.WORK_DIR, "mapped." + mappingsVersion + ".jar");
        if ( ! finalMappedJar.exists() ) {
            System.out.println("Final mapped jar: " + finalMappedJar + " does not exist, creating!");

            File clMappedJar = new File(finalMappedJar + "-cl");
            File mMappedJar = new File(finalMappedJar + "-m");

            ProcessRunner.runProcess(Main.WORK_DIR, "java", "-jar", "BuildData/bin/SpecialSource-2.jar", "map", "-i", vanillaJar.getPath(), "-m", "BuildData/mappings/" + versionInfo.getClassMappings(), "-o", clMappedJar.getPath());

            ProcessRunner.runProcess(Main.WORK_DIR, "java", "-jar", "BuildData/bin/SpecialSource-2.jar", "map", "-i", clMappedJar.getPath(),
                    "-m", "BuildData/mappings/" + versionInfo.getMemberMappings(), "-o", mMappedJar.getPath());

            ProcessRunner.runProcess(Main.WORK_DIR, "java", "-jar", "BuildData/bin/SpecialSource.jar", "--kill-lvt", "-i", mMappedJar.getPath(), "--access-transformer", "BuildData/mappings/" + versionInfo.getAccessTransforms(),
                    "-m", "BuildData/mappings/" + versionInfo.getPackageMappings(), "-o", finalMappedJar.getPath());
        }

        ProcessRunner.runProcess(Main.WORK_DIR, "sh", mvn, "install:install-file", "-Dfile=" + finalMappedJar, "-Dpackaging=jar", "-DgroupId=org.spigotmc",
                "-DartifactId=minecraft-server", "-Dversion=" + versionInfo.getMinecraftVersion() + "-SNAPSHOT");

        File decompileDir = new File(Main.WORK_DIR, "decompile-" + mappingsVersion);
        if ( ! decompileDir.exists() ) {
            decompileDir.mkdir();

            File clazzDir = new File(decompileDir, "classes");
            ZipUtil.unzip(finalMappedJar, clazzDir, new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    return input.startsWith("net/minecraft/server");
                }
            });
            if ( versionInfo.getDecompileCommand() == null ) {
                versionInfo.setDecompileCommand("java -jar BuildData/bin/fernflower.jar -dgs=1 -hdc=0 -rbr=0 -asc=1 -udv=0 {0} {1}");
            }

            ProcessRunner.runProcess(Main.WORK_DIR, MessageFormat.format(versionInfo.getDecompileCommand(), clazzDir.getPath(), decompileDir.getPath()).split(" "));
        }

        System.out.println("Applying CraftBukkit Patches");
        File nmsDir = new File(craftBukkit, "src/main/java/net");
        if ( nmsDir.exists() ) {
            System.out.println("Backing up NMS dir");
            FileUtils.moveDirectory(nmsDir, new File(Main.WORK_DIR, "nms.old." + System.currentTimeMillis()));
        }
        File patchDir = new File(craftBukkit, "nms-patches");
        for ( File file : patchDir.listFiles() ) {
            if ( ! file.getName().endsWith(".patch") ) {
                continue;
            }

            String targetFile = "net/minecraft/server/" + file.getName().replaceAll(".patch", ".java");

            File clean = new File(decompileDir, targetFile);
            File t = new File(nmsDir.getParentFile(), targetFile);
            t.getParentFile().mkdirs();

            System.out.println("Patching with " + file.getName());

            List<String> readFile = Files.readLines(file, Charsets.UTF_8);

            // Manually append prelude if it is not found in the first few lines.
            boolean preludeFound = false;
            for ( int i = 0; i < Math.min(3, readFile.size()); i++ ) {
                if ( readFile.get(i).startsWith("+++") ) {
                    preludeFound = true;
                    break;
                }
            }
            if ( ! preludeFound ) {
                readFile.add(0, "+++");
            }

            Patch parsedPatch = DiffUtils.parseUnifiedDiff(readFile);
            List<?> modifiedLines = DiffUtils.patch(Files.readLines(clean, Charsets.UTF_8), parsedPatch);

            BufferedWriter bw = new BufferedWriter(new FileWriter(t));
            for ( String line : ( List<String> ) modifiedLines ) {
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        }
        File tmpNms = new File(craftBukkit, "tmp-nms");
        FileUtils.copyDirectory(nmsDir, tmpNms);

        craftBukkitGit.branchDelete().setBranchNames("patched").setForce(true).call();
        craftBukkitGit.checkout().setCreateBranch(true).setForce(true).setName("patched").call();
        craftBukkitGit.add().addFilepattern("src/main/java/net/").call();
        craftBukkitGit.commit().setMessage("CraftBukkit $ " + new Date()).call();
        craftBukkitGit.checkout().setName(buildInfo.getRefs().getCraftBukkit()).call();

        FileUtils.moveDirectory(tmpNms, nmsDir);

        File spigotApi = new File(spigot, "Bukkit");
        if ( ! spigotApi.exists() ) {
            String url = "file://" + bukkit.getAbsolutePath();
            GitCloneTask task = new GitCloneTask(url, spigotApi, options);
            threadPool.submit(task);
        }
        File spigotServer = new File(spigot, "CraftBukkit");
        if ( ! spigotServer.exists() ) {
            String url = "file://" + craftBukkit.getAbsolutePath();
            GitCloneTask task = new GitCloneTask(url, spigotServer, options);
            threadPool.submit(task);
        }

        // Git spigotApiGit = Git.open( spigotApi );
        // Git spigotServerGit = Git.open( spigotServer );
        if ( ! options.isSkipCompile() ) {
            System.out.println("Compiling Bukkit");
            ProcessRunner.runProcess(bukkit, "sh", mvn, "install");
            if ( options.isGenDoc() ) {
                ProcessRunner.runProcess(bukkit, "sh", mvn, "javadoc:jar");
            }
            if ( options.isGenSrc() ) {
                ProcessRunner.runProcess(bukkit, "sh", mvn, "source:jar");
            }

            System.out.println("Compiling CraftBukkit");
            ProcessRunner.runProcess(craftBukkit, "sh", mvn, "install");
        }


        for ( int i = 0; i < 15; i++ ) {
            System.out.println(" ");
        }

        if ( ! options.isSkipCompile() ) {
            System.out.println("Success! Everything compiled successfully. Copying final .jar files now.");
            for ( String outputDir : options.getOutputDirectories() ) {
                FileUtil.copyJar("CraftBukkit/target", "craftbukkit", new File(outputDir, "craftbukkit-" + versionInfo.getMinecraftVersion() + ".jar"));
                FileUtil.copyJar("Spigot/Spigot-Server/target", "spigot", new File(outputDir, "spigot-" + versionInfo.getMinecraftVersion() + ".jar"));
            }
        }
        return true;
    }


    private void createDir(File file) {
        if ( ! FileUtil.isDirectory(file) ) {
            file.mkdir();
        }
    }

    private void printOptions(BuildTools options) {
        System.out.println("BuildTool Options: ");
        System.out.println("Disable Certificate Check: " + options.isDisableCertificateCheck());
        System.out.println("Don't Update: " + options.isDontUpdate());
        System.out.println("Skip Compile: " + options.isSkipCompile());
        System.out.println("Generate Sources: " + options.isGenSrc());
        System.out.println("Generate Docs: " + options.isGenDoc());
        System.out.println("Version: " + options.getVersion());
        for ( String dir : options.getOutputDirectories() ) {
            System.out.println("Output Dir: " + dir);
        }
    }
}
