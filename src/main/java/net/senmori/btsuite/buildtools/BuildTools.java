package net.senmori.btsuite.buildtools;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.Gson;
import difflib.DiffUtils;
import difflib.Patch;
import lombok.Data;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.TaskRunner;
import net.senmori.btsuite.WindowTab;
import net.senmori.btsuite.settings.Settings;
import net.senmori.btsuite.task.FileDownloader;
import net.senmori.btsuite.task.GitCloneTask;
import net.senmori.btsuite.task.MavenInstaller;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.GitUtil;
import net.senmori.btsuite.util.HashChecker;
import net.senmori.btsuite.util.ProcessRunner;
import net.senmori.btsuite.util.ZipUtil;
import net.senmori.btsuite.version.Version;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Data
public final class BuildTools {
    private boolean disableCertificateCheck = false;
    private boolean dontUpdate = false;
    private boolean skipCompile = false;
    private boolean genSrc = false;
    private boolean genDoc = false;

    private String version = "latest";

    private List<String> outputDirectories = Lists.newArrayList();

    private Map<Version, BuildInfo> versionMap = Maps.newHashMap();

    public void setVersionMap(Map<Version, BuildInfo> newMap) {
        this.versionMap.clear();
        this.versionMap.putAll(newMap);
    }

    public void setOutputDirectories(List<String> directories) {
        this.outputDirectories.clear();
        this.outputDirectories.addAll(directories);
    }

    public void addOutputDirectory(File directory) {
        if(FileUtil.isDirectory(directory))
            outputDirectories.add(directory.getAbsolutePath());
    }

    boolean running = false;
    Settings settings = Main.getSettings();
    TaskRunner runner = Main.getTaskRunner();
    public void run() {
        running = true;
        Main.setActiveTab(WindowTab.CONSOLE);
        String SHELL_PREFIX = "sh";
        File work = new File(Main.WORK_DIR, "work");
        createDir(work);
        if (getOutputDirectories().isEmpty()) {
            getOutputDirectories().add(Main.WORK_DIR.getPath()); // always have the WORK dir as an output
        }
        printOptions(this);

        // clone bukkit
        File bukkit = new File(Main.WORK_DIR, "Bukkit");
        if (! bukkit.exists()) {
            String repo = settings.getStashRepoLink() + "bukkit.git";
            GitCloneTask bukkitCloneTask = new GitCloneTask(repo, bukkit, this);
            waitForFuture(runner.submit(bukkitCloneTask));
        }

        // clone CB
        File craftBukkit = new File(Main.WORK_DIR, "CraftBukkit");
        if (! craftBukkit.exists()) {
            String repo = settings.getStashRepoLink() + "craftbukkit.git";
            GitCloneTask cbCloneTask = new GitCloneTask(repo, craftBukkit, this);
            waitForFuture(runner.submit(cbCloneTask));
        }

        // clone Spigot
        File spigot = new File(Main.WORK_DIR, "Spigot");
        if (! spigot.exists()) {
            String repo = settings.getStashRepoLink() + "spigot.git";
            GitCloneTask spigotCloneTask = new GitCloneTask(repo, spigot, this);
            waitForFuture(runner.submit(spigotCloneTask));
        }

        // clone BuildData
        File buildData = new File(Main.WORK_DIR, "BuildData");
        if (! buildData.exists()) {
            String repo = settings.getStashRepoLink() + "builddata.git";
            GitCloneTask buildDataCloneTask = new GitCloneTask(repo, buildData, this);
            waitForFuture(runner.submit(buildDataCloneTask));
        }

        // verify maven is installed
        File maven = new File(System.getenv("M2_HOME"));
        if (! maven.exists()) {
            Future<File> task = Main.TASK_RUNNER.submit(new MavenInstaller()); // this should NEVER happen.
            waitForFuture(task);
            try {
                maven = task.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        String mvn = maven.getAbsolutePath() + "/bin/mvn";


        Git bukkitGit = null;
        Git craftBukkitGit = null;
        Git spigotGit = null;
        Git buildGit = null;
        try {
            bukkitGit = Git.open(bukkit);
            craftBukkitGit = Git.open(craftBukkit);
            spigotGit = Git.open(spigot);
            buildGit = Git.open(buildData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BuildInfo buildInfo = new BuildInfo("Dev Build", "Development", 0, new BuildInfo.Refs("master", "master", "master", "master"));

        if (! isDontUpdate()) // do update
        {
            String askedVersion = getVersion();
            System.out.println("Attempting to build version: '" + askedVersion + "' use --rev <version> to override");

            String text = askedVersion + ".json";
            File versionDir = new File(work, "versions");
            File verInfo = new File(versionDir, text);
            if (! verInfo.exists()) {
                // download file
                String url = settings.getVersionLink() + askedVersion + ".json";
                FileDownloader fDown = new FileDownloader(url, verInfo);
                waitForFuture(runner.submit(fDown));
            }
            try {
                buildInfo = new Gson().fromJson(new FileReader(verInfo), BuildInfo.class);
                System.out.println("Found version " + buildInfo);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try {
                GitUtil.pull(buildGit, buildInfo.getRefs().getBuildData());
                GitUtil.pull(bukkitGit, buildInfo.getRefs().getBukkit());
                GitUtil.pull(craftBukkitGit, buildInfo.getRefs().getCraftBukkit());
                GitUtil.pull(spigotGit, buildInfo.getRefs().getSpigot());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } // end isDontUpdate if

        VersionInfo versionInfo = null;
        try {
            File buildDataDir = new File(Main.WORK_DIR, "BuildData");
            versionInfo = new Gson().fromJson(new FileReader(new File(buildDataDir, "info.json")), VersionInfo.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Default to 1.12.2 builds.
        if (versionInfo == null) {
            versionInfo = new VersionInfo("1.12.2", "bukkit-1.12.2.at", "bukkit-1.12.2-cl.csrg", "bukkit-1.12.2-members.csrg", "package.srg", null);
        }
        System.out.println("Attempting to build Minecraft with details: " + versionInfo);

        File vanillaJar = new File(Main.JAR_DIR, "minecraft_server." + versionInfo.getMinecraftVersion() + ".jar");
        if (! vanillaJar.exists()) {
            if (versionInfo.getServerUrl() != null) {
                FileDownloader fDown = new FileDownloader(versionInfo.getServerUrl(), vanillaJar);
                fDown.setOnScheduled((event) -> {
                    System.out.println("Downloading Minecraft Server jar version " + version);
                });
                Future<File> future = runner.submit(fDown);
                waitForFuture(future);
                try {
                    vanillaJar = fDown.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("*** Could not download " + versionInfo.getMinecraftVersion() + " jar. Giving up.");
                }
            } else {
                String s3URL = String.format(settings.getGetFallbackServerUrl(), versionInfo.getMinecraftVersion());
                FileDownloader fDown = new FileDownloader(s3URL, vanillaJar);
                fDown.setOnScheduled((event) -> {
                    System.out.println("Downloading Minecraft Server jar version " + version);
                });
                waitForFuture(runner.submit(fDown));
                try {
                    vanillaJar = fDown.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("*** Could not download " + versionInfo.getMinecraftVersion() + " jar");
                    return;
                }

                // Legacy versions can also specify a specific shell to build with which has to be bash-compatible
                SHELL_PREFIX = System.getenv().get("SHELL");
                if (SHELL_PREFIX == null || SHELL_PREFIX.trim().isEmpty()) {
                    SHELL_PREFIX = "bash";
                }
            }
        }
        boolean validHash = false;
        try {
            validHash = HashChecker.checkHash(vanillaJar, versionInfo);
        } catch (IOException e) {
            validHash = false;
        }
        if (! validHash) {
            System.err.println("**** Could not download clean Minecraft jar, giving up.");
            return;
        }

        Iterable<RevCommit> mappings = null;
        try {
            mappings = buildGit.log()
                               .addPath("mappings/" + versionInfo.getAccessTransforms())
                               .addPath("mappings/" + versionInfo.getClassMappings())
                               .addPath("mappings/" + versionInfo.getMemberMappings())
                               .addPath("mappings/" + versionInfo.getPackageMappings())
                               .setMaxCount(1).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        Hasher mappingsHash = Hashing.md5().newHasher();
        for (RevCommit rev : mappings) {
            mappingsHash.putString(rev.getName(), Charsets.UTF_8);
        }

        String mappingsVersion = mappingsHash.hash().toString().substring(24); // Last 8 chars

        File finalMappedJar = new File(work, "mapped." + mappingsVersion + ".jar");
        if (! finalMappedJar.exists()) {
            System.out.println("Final mapped jar: " + finalMappedJar + " does not exist, creating!");

            File clMappedJar = new File(finalMappedJar + "-cl");
            File mMappedJar = new File(finalMappedJar + "-m");

            try {
                ProcessRunner.runProcess(work, "java", "-jar", "BuildData/bin/SpecialSource-2.jar", "map", "-i", vanillaJar.getAbsolutePath(), "-m", "BuildData/mappings/" + versionInfo.getClassMappings(), "-o", clMappedJar.getAbsolutePath());

                ProcessRunner.runProcess(work, "java", "-jar", "BuildData/bin/SpecialSource-2.jar", "map", "-i", clMappedJar.getAbsolutePath(),
                        "-m", "BuildData/mappings/" + versionInfo.getMemberMappings(), "-o", mMappedJar.getAbsolutePath());

                ProcessRunner.runProcess(work, "java", "-jar", "BuildData/bin/SpecialSource.jar", "--kill-lvt", "-i", mMappedJar.getAbsolutePath(), "--access-transformer", "BuildData/mappings/" + versionInfo.getAccessTransforms(),
                        "-m", "BuildData/mappings/" + versionInfo.getPackageMappings(), "-o", finalMappedJar.getAbsolutePath());

                ProcessRunner.runProcess(work, "sh", mvn, "install:install-file", "-Dfile=" + finalMappedJar, "-Dpackaging=jar", "-DgroupId=org.spigotmc",
                        "-DartifactId=minecraft-server", "-Dversion=" + versionInfo.getMinecraftVersion() + "-SNAPSHOT");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File decompileDir = new File(work, "decompile-" + mappingsVersion);
        if (! decompileDir.exists()) {
            createDir(decompileDir);
            File clazzDir = new File(decompileDir, "classes");
            createDir(clazzDir);
            try {
                ZipUtil.unzip(finalMappedJar, clazzDir, ( input -> input.startsWith("net/minecraft/server") ));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (versionInfo.getDecompileCommand() == null) {
                versionInfo.setDecompileCommand("java -jar BuildData/bin/fernflower.jar -dgs=1 -hdc=0 -rbr=0 -asc=1 -udv=0 {0} {1}");
            }

            try {
                ProcessRunner.runProcess(Main.WORK_DIR, MessageFormat.format(versionInfo.getDecompileCommand(), clazzDir.getPath(), decompileDir.getPath()).split(" "));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Applying CraftBukkit Patches");
        File nmsDir = new File(craftBukkit, "src/main/java/net");
        if (nmsDir.exists()) {
            System.out.println("Backing up NMS dir");
            try {
                FileUtils.moveDirectory(nmsDir, new File(work, "nms.old." + System.currentTimeMillis()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Do patches
        File patchDir = new File(craftBukkit, "nms-patches");
        for (File file : patchDir.listFiles()) {
            if (! file.getName().endsWith(".patch")) {
                continue;
            }

            String targetFile = "net/minecraft/server/" + file.getName().replaceAll(".patch", ".java");

            File clean = new File(decompileDir, targetFile);
            File t = new File(nmsDir.getParentFile(), targetFile);
            t.getParentFile().mkdirs();

            System.out.println("Patching with " + file.getName());

            List<String> readFile = null;
            try {
                readFile = Files.readLines(file, Charsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Manually append prelude if it is not found in the first few lines.
            boolean preludeFound = false;
            for (int i = 0; i < Math.min(3, readFile.size()); i++) {
                if (readFile.get(i).startsWith("+++")) {
                    preludeFound = true;
                    break;
                }
            }
            if (! preludeFound) {
                readFile.add(0, "+++");
            }

            Patch parsedPatch = DiffUtils.parseUnifiedDiff(readFile);
            try {
                List<?> modifiedLines = DiffUtils.patch(Files.readLines(clean, Charsets.UTF_8), parsedPatch);

                BufferedWriter bw = new BufferedWriter(new FileWriter(t));
                for (String line : (List<String>) modifiedLines) {
                    bw.write(line);
                    bw.newLine();
                }
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } // end patch for

        File tmpNms = new File(craftBukkit, "tmp-nms");
        try {
            FileUtils.copyDirectory(nmsDir, tmpNms);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            craftBukkitGit.branchDelete().setBranchNames("patched").setForce(true).call();
            craftBukkitGit.checkout().setCreateBranch(true).setForce(true).setName("patched").call();
            craftBukkitGit.add().addFilepattern("src/main/java/net/").call();
            craftBukkitGit.commit().setMessage("CraftBukkit $ " + new Date()).call();
            craftBukkitGit.checkout().setName(buildInfo.getRefs().getCraftBukkit()).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        try {
            FileUtils.moveDirectory(tmpNms, nmsDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File spigotApi = new File(spigot, "Bukkit");
        if (! spigotApi.exists()) {
            String url = "file://" + bukkit.getAbsolutePath();
            GitCloneTask task = new GitCloneTask(url, spigotApi, this);
            runner.submit(task);
        }
        File spigotServer = new File(spigot, "CraftBukkit");
        if (! spigotServer.exists()) {
            String url = "file://" + craftBukkit.getAbsolutePath();
            GitCloneTask task = new GitCloneTask(url, spigotServer, this);
            runner.submit(task);
        }

        // Git spigotApiGit = Git.open( spigotApi );
        // Git spigotServerGit = Git.open( spigotServer );
        if (! isSkipCompile()) {
            try {
                System.out.println("Compiling Bukkit");
                ProcessRunner.runProcess(bukkit, "sh", mvn, "install");
                if (isGenDoc()) {
                    ProcessRunner.runProcess(bukkit, "sh", mvn, "javadoc:jar");
                }
                if (isGenSrc()) {
                    ProcessRunner.runProcess(bukkit, "sh", mvn, "source:jar");
                }

                System.out.println("Compiling CraftBukkit");
                ProcessRunner.runProcess(craftBukkit, "sh", mvn, "install");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            ProcessRunner.runProcess(spigot, SHELL_PREFIX, "applyPatches.sh");
            System.out.println("*** Spigot patches applied!");

            if (! isSkipCompile()) {
                System.out.println("Compiling Spigot & Spigot-API");
                ProcessRunner.runProcess(spigot, "sh", mvn, "clean", "install");
            }
        } catch (Exception ex) {
            System.err.println("Error compiling Spigot. Please check the wiki for FAQs.");
            System.err.println("If this does not resolve your issue then please pastebin the entire BuildTools.log.txt file when seeking support.");
            ex.printStackTrace();
            return;
        }

        for (int i = 0; i < 15; i++) {
            System.out.println(" ");
        }

        if (! isSkipCompile()) {
            try {
                System.out.println("Success! Everything compiled successfully. Copying final .jar files now.");
                for (String outputDir : getOutputDirectories()) {
                    FileUtil.copyJar("CraftBukkit/target", "craftbukkit", new File(outputDir, "craftbukkit-" + versionInfo.getMinecraftVersion() + ".jar"));
                    FileUtil.copyJar("Spigot/Spigot-Server/target", "spigot", new File(outputDir, "spigot-" + versionInfo.getMinecraftVersion() + ".jar"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        running = false;
    }

    private void printOptions(BuildTools options) {
        System.out.println("BuildTool Options: ");
        System.out.println("Disable Certificate Check: " + options.isDisableCertificateCheck());
        System.out.println("Don't Update: " + options.isDontUpdate());
        System.out.println("Skip Compile: " + options.isSkipCompile());
        System.out.println("Generate Sources: " + options.isGenSrc());
        System.out.println("Generate Docs: " + options.isGenDoc());
        System.out.println("Version: " + options.getVersion());
        for (String dir : options.getOutputDirectories()) {
            System.out.println("Output Dir: " + dir);
        }
    }

    private void createDir(File file) {
        if (! FileUtil.isDirectory(file)) {
            file.mkdir();
        }
    }

    private void waitForFuture(Future<?> future) {
        do {
            ;
        } while (! future.isDone());
    }
}
