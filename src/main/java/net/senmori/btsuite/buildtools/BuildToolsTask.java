package net.senmori.btsuite.buildtools;

import co.aikar.taskchain.TaskChain;
import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.Gson;
import difflib.DiffUtils;
import difflib.Patch;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.Settings;
import net.senmori.btsuite.task.MavenInstaller;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.GitUtil;
import net.senmori.btsuite.util.HashChecker;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.ProcessRunner;
import net.senmori.btsuite.util.TaskUtil;
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

public class BuildToolsTask {

    private final BuildTools options;
    private final Settings settings;
    private final Settings.Directories dirs;

    private final TaskChain<?> chain = Main.newChain();
    private final TaskChain<File> fileChain = Main.<File>newSharedChain("fileChain");

    public BuildToolsTask(BuildTools options, Settings settings) {
        this.options = options;
        this.settings = settings;
        this.dirs = settings.getDirectories();
    }

    public void build() throws Exception {
        String SHELL_PREFIX = "sh";
        File work = dirs.getWorkDir();
        if ( options.getOutputDirectories().isEmpty() ) {
            options.getOutputDirectories().add(dirs.getWorkingDir().getPath()); // always have the WORK dir as an output
        }
        printOptions(options);

        File bukkit = new File(dirs.getWorkingDir(), "Bukkit");
        if ( ! bukkit.exists() ) {
            String repo = settings.getStashRepoLink() + "bukkit.git";
            TaskUtil.asyncCloneRepo(chain, repo, bukkit);
        }

        File craftBukkit = new File(dirs.getWorkingDir(), "CraftBukkit");
        if ( ! craftBukkit.exists() ) {
            String repo = settings.getStashRepoLink() + "craftbukkit.git";
            TaskUtil.asyncCloneRepo(chain, repo, craftBukkit);
        }

        File spigot = new File(dirs.getWorkingDir(), "Spigot");
        if ( ! spigot.exists() ) {
            String repo = settings.getStashRepoLink() + "spigot.git";
            TaskUtil.asyncCloneRepo(chain, repo, spigot);
        }

        File buildData = new File(dirs.getWorkingDir(), "BuildData");
        if ( ! buildData.exists() ) {
            String repo = settings.getStashRepoLink() + "builddata.git";
            TaskUtil.asyncCloneRepo(chain, repo, buildData);
        }

        File maven = dirs.getMvnDir();
        if ( !maven.exists() ) {
            chain.async(() -> MavenInstaller.install() )
                 .execute(() -> LogHandler.debug("Finished downloading Maven to " + dirs.getMvnDir()));
            maven = dirs.getMvnDir();
            chain.removeTaskData("chain");
        }
        String mvn = maven.getAbsolutePath() + "/bin/mvn";

        Git bukkitGit = Git.open(bukkit);
        Git craftBukkitGit = Git.open(craftBukkit);
        Git spigotGit = Git.open(spigot);
        Git buildGit = Git.open(buildData);

        BuildInfo buildInfo = BuildInfo.getDefaultImpl();

        if ( !options.isDontUpdate() ) {
            final String askedVersion = options.getVersion();
            LogHandler.info("Attempting to build version: '" + askedVersion + "' use --rev <version> to override");

            String text = askedVersion + ".json";
            File verInfo = new File(dirs.getVersionsDir(), text);
            if ( !verInfo.exists() ) {
                // download file
                String url = settings.getVersionLink() + askedVersion + ".json";
                verInfo = TaskUtil.asyncDownloadFile(fileChain, url, verInfo);
            }
            LogHandler.debug("Found version " + askedVersion);
            buildInfo = new Gson().fromJson(new FileReader(verInfo), BuildInfo.class);

            final BuildInfo fBuildInfo = buildInfo;
            chain.async(() -> GitUtil.pullWrapped(buildGit, fBuildInfo.getRefs().getBuildData()))
                 .async(() -> GitUtil.pullWrapped(bukkitGit, fBuildInfo.getRefs().getBukkit()))
                 .async(() -> GitUtil.pullWrapped(craftBukkitGit, fBuildInfo.getRefs().getCraftBukkit()))
                 .async(() -> GitUtil.pullWrapped(spigotGit, fBuildInfo.getRefs().getSpigot()));
        }

        VersionInfo versionInfo = new Gson().fromJson(new FileReader(new File("BuildData/info.json")), VersionInfo.class);
        // Default to 1.12.2 builds.
        if ( versionInfo == null ) {
            LogHandler.debug("Could not generate version. Using default ( " + settings.getDefaultVersion() + " ).");
            versionInfo = settings.getBukkitVersionInfo(settings.getDefaultVersion());
        }
        LogHandler.info("Attempting to build Minecraft with details: " + versionInfo);

        String jarName = "minecraft_server." + versionInfo.getMinecraftVersion() + ".jar";
        File vanillaJar = new File(dirs.getJarDir(), jarName);
        if ( ! vanillaJar.exists() || ! HashChecker.checkHash(vanillaJar, versionInfo) ) {
            if ( versionInfo.getServerUrl() != null ) {
                vanillaJar = TaskUtil.asyncDownloadFile(fileChain, versionInfo.getServerUrl(), vanillaJar);
            } else {
                final String downloadLink = String.format(settings.getFallbackServerUrl(), versionInfo.getMinecraftVersion());
                vanillaJar = TaskUtil.asyncDownloadFile(fileChain, downloadLink, vanillaJar);

                // Legacy versions can also specify a specific shell to build with which has to be bash-compatible
                SHELL_PREFIX = System.getenv().get("SHELL");
                if ( SHELL_PREFIX == null || SHELL_PREFIX.trim().isEmpty() ) {
                    SHELL_PREFIX = "bash";
                }
            }
        }
        if ( !HashChecker.checkHash(vanillaJar, versionInfo) ) {
            LogHandler.error("**** Could not download clean Minecraft jar, giving up.");
            return;
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

        File finalMappedJar = new File(dirs.getJarDir(), "mapped." + mappingsVersion + ".jar");
        if ( ! finalMappedJar.exists() ) {
            LogHandler.info("Final mapped jar: " + finalMappedJar + " does not exist, creating!");

            File clMappedJar = new File(finalMappedJar + "-cl");
            File mMappedJar = new File(finalMappedJar + "-m");

            ProcessRunner.runProcess(dirs.getJarDir(), "java", "-jar", "BuildData/bin/SpecialSource-2.jar", "map", "-i", vanillaJar.getPath(), "-m", "BuildData/mappings/" + versionInfo.getClassMappings(), "-o", clMappedJar.getPath());

            ProcessRunner.runProcess(dirs.getJarDir(), "java", "-jar", "BuildData/bin/SpecialSource-2.jar", "map", "-i", clMappedJar.getPath(),
                    "-m", "BuildData/mappings/" + versionInfo.getMemberMappings(), "-o", mMappedJar.getPath());

            ProcessRunner.runProcess(dirs.getJarDir(), "java", "-jar", "BuildData/bin/SpecialSource.jar", "--kill-lvt", "-i", mMappedJar.getPath(), "--access-transformer", "BuildData/mappings/" + versionInfo.getAccessTransforms(),
                    "-m", "BuildData/mappings/" + versionInfo.getPackageMappings(), "-o", finalMappedJar.getPath());
        }

        ProcessRunner.runProcess(dirs.getJarDir(), "sh", mvn, "install:install-file", "-Dfile=" + finalMappedJar, "-Dpackaging=jar", "-DgroupId=org.spigotmc",
                "-DartifactId=minecraft-server", "-Dversion=" + versionInfo.getMinecraftVersion() + "-SNAPSHOT");

        File decompileDir = new File(dirs.getWorkDir(), "decompile-" + mappingsVersion);
        if ( !decompileDir.exists() ) {
            decompileDir.mkdir();

            File clazzDir = new File(decompileDir, "classes");
            clazzDir.mkdir();
            ZipUtil.unzip(finalMappedJar, clazzDir, new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    return input.startsWith("net/minecraft/server");
                }
            });
            if ( versionInfo.getDecompileCommand() == null ) {
                versionInfo.setDecompileCommand("java -jar BuildData/bin/fernflower.jar -dgs=1 -hdc=0 -rbr=0 -asc=1 -udv=0 {0} {1}");
            }

            ProcessRunner.runProcess(dirs.getWorkingDir(), MessageFormat.format(versionInfo.getDecompileCommand(), clazzDir.getPath(), decompileDir.getPath()).split(" "));
        }

        LogHandler.info("Applying CraftBukkit Patches");
        File nmsDir = new File(craftBukkit, "src/main/java/net");
        nmsDir.mkdirs();
        if ( nmsDir.exists() ) {
            LogHandler.info("Backing up NMS dir");
            FileUtils.moveDirectory(nmsDir, new File(dirs.getWorkDir(), "nms.old." + System.currentTimeMillis()));
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

            LogHandler.info("Patching with " + file.getName());

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
        if ( !spigotApi.exists() ) {
            String url = "file://" + bukkit.getAbsolutePath();
            TaskUtil.asyncCloneRepo(chain, url, spigotApi);
        }
        File spigotServer = new File(spigot, "CraftBukkit");
        if ( !spigotServer.exists() ) {
            String url = "file://" + craftBukkit.getAbsolutePath();
            TaskUtil.asyncCloneRepo(chain, url, spigotServer);
        }

        // Git spigotApiGit = Git.open( spigotApi );
        // Git spigotServerGit = Git.open( spigotServer );
        if ( !options.isSkipCompile() ) {
            LogHandler.info("Compiling Bukkit");
            ProcessRunner.runProcess(bukkit, "sh", mvn, "install");
            if ( options.isGenDoc() ) {
                ProcessRunner.runProcess(bukkit, "sh", mvn, "javadoc:jar");
            }
            if ( options.isGenSrc() ) {
                ProcessRunner.runProcess(bukkit, "sh", mvn, "source:jar");
            }
            LogHandler.info("Compiling CraftBukkit");
            ProcessRunner.runProcess(craftBukkit, "sh", mvn, "install");
        }

        for ( int i = 0; i < 15; i++ ) {
            System.out.println(" ");
        }

        if ( !options.isSkipCompile() ) {
            LogHandler.info("Success! Everything compiled successfully. Copying final .jar files now.");
            for ( String outputDir : options.getOutputDirectories() ) {
                FileUtil.copyJar("CraftBukkit/target", "craftbukkit", new File(outputDir, "craftbukkit-" + versionInfo.getMinecraftVersion() + ".jar"));
                FileUtil.copyJar("Spigot/Spigot-Server/target", "spigot", new File(outputDir, "spigot-" + versionInfo.getMinecraftVersion() + ".jar"));
            }
        }
    }

    private void printOptions(BuildTools options) {
        LogHandler.info("BuildTool Options: ");
        LogHandler.info("Disable Certificate Check: " + options.isDisableCertificateCheck());
        LogHandler.info("Don't Update: " + options.isDontUpdate());
        LogHandler.info("Skip Compile: " + options.isSkipCompile());
        LogHandler.info("Generate Sources: " + options.isGenSrc());
        LogHandler.info("Generate Docs: " + options.isGenDoc());
        LogHandler.info("VersionString: " + options.getVersion());
        for ( String dir : options.getOutputDirectories() ) {
            LogHandler.info("Output Dir: " + dir);
        }
    }

}
