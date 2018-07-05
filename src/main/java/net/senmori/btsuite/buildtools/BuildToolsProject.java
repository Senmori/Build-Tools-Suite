package net.senmori.btsuite.buildtools;

import com.google.common.base.Charsets;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.Gson;
import difflib.DiffUtils;
import difflib.Patch;
import javafx.application.Platform;
import net.senmori.btsuite.Settings;
import net.senmori.btsuite.pool.TaskPool;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.task.FileDownloader;
import net.senmori.btsuite.task.GitCloneTask;
import net.senmori.btsuite.task.GitPullTask;
import net.senmori.btsuite.task.MavenInstaller;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.HashChecker;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.MavenCommandBuilder;
import net.senmori.btsuite.util.ProcessRunner;
import net.senmori.btsuite.util.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.InvocationResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

public class BuildToolsProject implements Callable<Boolean> {
    private static final Gson GSON = new Gson();

    private final BuildTools options;
    private final Settings settings;
    private final Settings.Directories dirs;

    private final TaskPool projectPool = TaskPools.createSingleTaskPool();

    public BuildToolsProject(BuildTools options, Settings settings) {
        this.options = options;
        this.settings = settings;
        this.dirs = settings.getDirectories();
    }

    @Override
    public Boolean call() throws Exception {
        String SHELL_PREFIX = "sh";
        File work = dirs.getWorkDir();
        if ( options.getOutputDirectories().isEmpty() ) {
            options.getOutputDirectories().add(dirs.getWorkingDir().getPath()); // always have the WORK dir as an output
        }
        printOptions(options);

        File bukkit = new File(dirs.getWorkingDir(), "Bukkit");
        LogHandler.debug(bukkit.getName() + " Git Cloning");
        if ( ! bukkit.exists() ) {
            String repo = settings.getStashRepoLink() + "bukkit.git";
            GitCloneTask task = GitCloneTask.clone(repo, bukkit);
            bukkit = projectPool.submit(task).get();
        }

        File craftBukkit = new File(dirs.getWorkingDir(), "CraftBukkit");
        LogHandler.debug(craftBukkit.getName() + " Git Cloning");
        if ( ! craftBukkit.exists() ) {
            String repo = settings.getStashRepoLink() + "craftbukkit.git";
            GitCloneTask task = GitCloneTask.clone(repo, craftBukkit);
            craftBukkit = projectPool.submit(task).get();
        }

        File spigot = new File(dirs.getWorkingDir(), "Spigot");
        LogHandler.debug(spigot.getName() + " Git Cloning");
        if ( ! spigot.exists() ) {
            String repo = settings.getStashRepoLink() + "spigot.git";
            GitCloneTask task = GitCloneTask.clone(repo, spigot);
            spigot = projectPool.submit(task).get();
        }

        File buildData = new File(dirs.getWorkingDir(), "BuildData");
        LogHandler.debug(buildData.getName() + " Git Cloning");
        if ( ! buildData.exists() ) {
            String repo = settings.getStashRepoLink() + "builddata.git";
            GitCloneTask task = GitCloneTask.clone(repo, buildData);
            buildData = projectPool.submit(task).get();
        }

        File maven = dirs.getMvnDir();
        if ( !maven.exists() ) {
            MavenInstaller mvn = new MavenInstaller();
            buildData = projectPool.submit(mvn).get();
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
                verInfo = projectPool.submit(new FileDownloader(url, verInfo)).get();
            }
            LogHandler.debug("Found version " + askedVersion);
            buildInfo = GSON.fromJson(new FileReader(verInfo), BuildInfo.class);

            projectPool.submit(GitPullTask.pull(buildGit, buildInfo.getRefs().getBuildData())).get();
            projectPool.submit(GitPullTask.pull(bukkitGit, buildInfo.getRefs().getBukkit())).get();
            projectPool.submit(GitPullTask.pull(craftBukkitGit, buildInfo.getRefs().getCraftBukkit())).get();
            projectPool.submit(GitPullTask.pull(spigotGit, buildInfo.getRefs().getSpigot())).get();
        }

        File infoFile = new File(dirs.getWorkingDir(), "BuildData/info.json");
        infoFile.mkdirs();
        VersionInfo versionInfo = GSON.fromJson(new FileReader(infoFile), VersionInfo.class);
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

                vanillaJar = projectPool.submit(new FileDownloader(versionInfo.getServerUrl(), vanillaJar)).get();
            }
            else {
                final String downloadLink = String.format(settings.getFallbackServerUrl(), versionInfo.getMinecraftVersion());
                vanillaJar = projectPool.submit(new FileDownloader(downloadLink, vanillaJar)).get();

                // Legacy versions can also specify a specific shell to build with which has to be bash-compatible
                SHELL_PREFIX = System.getenv().get("SHELL");
                if ( SHELL_PREFIX == null || SHELL_PREFIX.trim().isEmpty() ) {
                    SHELL_PREFIX = "bash";
                }
            }
        }
        if ( !HashChecker.checkHash(vanillaJar, versionInfo) ) {
            LogHandler.error("**** Could not download clean Minecraft jar, giving up.");
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

        File finalMappedJar = new File(dirs.getJarDir(), "mapped." + mappingsVersion + ".jar");
        if ( ! finalMappedJar.exists() ) {
            LogHandler.info("Final mapped jar: " + finalMappedJar + " does not exist, creating!");

            File clMappedJar = new File(finalMappedJar + "-cl");
            File mMappedJar = new File(finalMappedJar + "-m");

            File fernflower = new File(buildData, "bin/fernflower.jar");
            File specialSource = new File(buildData, "bin/SpecialSource.jar");
            File specialSource_2 = new File(buildData, "bin/SpecialSource-2.jar");

            LogHandler.debug("Applying first SpecialSource-2");
            ProcessRunner.runProcess(buildData, "java", "-jar", specialSource_2.getAbsolutePath(), "map", "-i", vanillaJar.getAbsolutePath(), "-m", "mappings/" + versionInfo.getClassMappings(), "-o", clMappedJar.getAbsolutePath());

            LogHandler.debug("Applying second SpecialSource-2");
            ProcessRunner.runProcess(buildData, "java", "-jar", specialSource_2.getAbsolutePath(), "map", "-i", clMappedJar.getAbsolutePath(),
                    "-m", "mappings/" + versionInfo.getMemberMappings(), "-o", mMappedJar.getAbsolutePath());

            LogHandler.debug("Applying third SpecialSource-2");
            ProcessRunner.runProcess(buildData, "java", "-jar", specialSource.getAbsolutePath(), "--kill-lvt", "-i", mMappedJar.getAbsolutePath(), "--access-transformer", "mappings/" + versionInfo.getAccessTransforms(),
                    "-m", "mappings/" + versionInfo.getPackageMappings(), "-o", finalMappedJar.getAbsolutePath());
        }

        LogHandler.debug("Installing '" + finalMappedJar.getName() + "' to local repository");
        File mvnExecutable = new File(dirs.getMvnDir(), "bin/mvn");

        MavenCommandBuilder install = MavenCommandBuilder.builder();
        String[] goals = new String[] {
                "install:install-file",
                "-Dfile=" + finalMappedJar.getAbsolutePath(),
                "-Dpackaging=jar",
                "-DgroupId=org.spigotmc",
                "-DartifactId=minecraft-server",
                "-Dversion=" + versionInfo.getMinecraftVersion() + "-SNAPSHOT"
        };
        install.setMavenOpts("-Xmx1024M")
               .setInteractiveMode(false)
               .setBaseDirectory(dirs.getJarDir())
               .setGoals( Arrays.asList(goals) );
        InvocationResult result = install.execute();
        install = null;
        if(result.getExitCode() != 0) {
            LogHandler.error(result.getExecutionException().getMessage());
            return false;
        }

        LogHandler.debug("Decompiling:");
        File decompileDir = new File(dirs.getWorkDir(), "decompile-" + mappingsVersion);
        if ( !decompileDir.exists() ) {
            decompileDir.mkdir();

            File clazzDir = new File(decompileDir, "classes");
            clazzDir.mkdir();
            LogHandler.debug("Unzipping " + finalMappedJar.getName() + " into " + clazzDir);
            boolean unzipped = projectPool.<Boolean>submit(() -> {
                try {
                    ZipUtil.unzip(finalMappedJar, clazzDir, (str) -> str.startsWith("net/minecraft/server/"));
                    return true;
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
                return false;
            }).get();
            if ( versionInfo.getDecompileCommand() == null ) {
                LogHandler.debug("Set decompile command for VersionInfo ( " + versionInfo.getMinecraftVersion() + " )");
                versionInfo.setDecompileCommand("java -jar BuildData/bin/fernflower.jar -dgs=1 -hdc=0 -rbr=0 -asc=1 -udv=0 {0} {1}");
            }

            LogHandler.debug("Running decompile command");
            ProcessRunner.runProcess(dirs.getWorkingDir(), MessageFormat.format(versionInfo.getDecompileCommand(), clazzDir.getAbsolutePath(), decompileDir.getAbsolutePath()).split(" "));
        }

        LogHandler.info("Applying CraftBukkit Patches");
        File nmsDir = new File(craftBukkit, "src/main/java/net");
        nmsDir.mkdirs();
        if ( nmsDir.exists() ) {
            LogHandler.info("Backing up NMS dir");
            FileUtils.moveDirectory(nmsDir, new File(dirs.getTmpDir(), "nms.old." + System.currentTimeMillis()));
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
        LogHandler.info("Patching complete!");
        File tmpNms = new File(craftBukkit, "tmp-nms");
        FileUtils.copyDirectory(nmsDir, tmpNms);

        craftBukkitGit.branchDelete().setBranchNames("patched").setForce(true).call();
        craftBukkitGit.checkout().setCreateBranch(true).setForce(true).setName("patched").call();
        craftBukkitGit.add().addFilepattern("src/main/java/net/").call();
        craftBukkitGit.commit().setMessage("CraftBukkit $ " + new Date()).call();
        craftBukkitGit.checkout().setName(buildInfo.getRefs().getCraftBukkit()).call();

        LogHandler.info("Moving " + tmpNms.getName() + " to " + nmsDir.getName());
        FileUtils.moveDirectory(tmpNms, nmsDir);

        LogHandler.debug("Cloning SpigotAPI");
        File spigotApi = new File(spigot, "Bukkit");
        if ( !spigotApi.exists() ) {
            String url = "file://" + bukkit.getAbsolutePath();
            spigotApi = projectPool.submit(GitCloneTask.clone(url, spigotApi)).get();
        }
        LogHandler.debug("Cloning SpigotServer");
        File spigotServer = new File(spigot, "CraftBukkit");
        if ( !spigotServer.exists() ) {
            String url = "file://" + craftBukkit.getAbsolutePath();
            spigotServer = projectPool.submit(GitCloneTask.clone(url, spigotServer)).get();
        }

        // Git spigotApiGit = Git.open( spigotApi );
        // Git spigotServerGit = Git.open( spigotServer );
        if ( !options.isSkipCompile() ) {
            MavenCommandBuilder root = MavenCommandBuilder.builder().setInteractiveMode(false);
            LogHandler.info("Compiling Bukkit");
            root.setBaseDirectory(bukkit).setGoals( Arrays.asList("install") ).execute();
            if ( options.isGenDoc() ) {
                root.setBaseDirectory(bukkit).setGoals( Arrays.asList( "javadoc:jar" ) ).execute();
            }
            if ( options.isGenSrc() ) {
                root.setBaseDirectory(bukkit).setGoals( Arrays.asList( "source:jar" ) ).execute();
            }
            LogHandler.info("Compiling CraftBukkit");
            root.setBaseDirectory(craftBukkit).setGoals( Arrays.asList( "install" ) ).execute();
        }

        try {
            final File fSpigot = spigot;
            final String sh = SHELL_PREFIX;
            final File decomp = new File(dirs.getWorkDir(), "decompile-" + mappingsVersion);
            final File patchesScript = new File(fSpigot, "applyPatches.sh");
            // <path-to-dir> sh applyPatches.sh <decompile-dir>
            int returnCode = TaskPools.submit(() -> {
                return ProcessRunner.runProcess(fSpigot, sh, patchesScript.getAbsolutePath(), decomp.getAbsolutePath());
            }).get();
            LogHandler.info("** Spigot patches applied!");

            if( !options.isSkipCompile() ) {
                LogHandler.info("Compiling Spigot & Spigot-API");
                int code = MavenCommandBuilder.builder()
                                   .setBaseDirectory(spigot)
                                   .setInteractiveMode(false)
                                   .setGoals( Arrays.asList("clean", "install")).execute().getExitCode();
                if(code != 0) {
                    throw new IllegalStateException();
                }
                LogHandler.info("Finished installing Spigot & Spigot-API to local repository.");
            }
        } catch ( Exception e ) {
            LogHandler.error("Error compiling Spigot. Please check the wiki for FAQs.");
            LogHandler.error("If this does not resolve your issue then please pastebin the entire log when seeking support.");
            e.printStackTrace();
            options.setFinished();
            return false;
        }


        for ( int i = 0; i < 15; i++ ) {
            System.out.println(" ");
        }

        if ( !options.isSkipCompile() ) {
            LogHandler.info("Success! Everything compiled successfully. Copying final .jar files now.");
            String version = versionInfo.getMinecraftVersion();
            TaskPools.submit(() -> {
                final File craftDir = new File(dirs.getWorkingDir(), "CraftBukkit/target");
                final File spigotDir = new File(dirs.getWorkingDir(), "Spigot/Spigot-Server/target");
                String craftbukkitName = "craftbukkit-" + version + "-SNAPSHOT.jar";
                String spigotName = "spigot-" + version + "-SNAPSHOT.jar";
                final File craftJar = new File(craftDir, craftbukkitName);
                final File spigotJar = new File(spigotDir, spigotName);
                for ( String outputDir : options.getOutputDirectories() ) {
                    try {
                        Files.copy(craftJar, new File(outputDir, "craftbukkit-" + version + ".jar"));
                        Files.copy(spigotJar, new File(outputDir, "spigot-" + version + ".jar"));
                    } catch ( Exception e ) {
                        e.printStackTrace();
                    }
                }
                return true;
            }).get();
        }
        LogHandler.info("BuildTools has finished!");
        projectPool.getService().shutdown();
        options.setFinished();
        return true;
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
