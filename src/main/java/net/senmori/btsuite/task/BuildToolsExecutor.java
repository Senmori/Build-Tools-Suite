package net.senmori.btsuite.task;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.CharStreams;
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
import net.senmori.btsuite.version.Version;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class BuildToolsExecutor extends Task<Boolean> {

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
        // check for work directory
        String applyPatchesShell = "sh";
        File work = new File(BT_WORK_DIR, "work");
        createDir(work);

        File bukkit = new File(BT_WORK_DIR, "Bukkit");
        if(!bukkit.exists()) {
            threadPool.execute(new GitCloneTask(settings.getStashRepoLink() + "bukkit.git", bukkit));
        }

        File craftbukkit = new File(BT_WORK_DIR, "CraftBukkit");
        if(!craftbukkit.exists()) {
            threadPool.execute(new GitCloneTask(settings.getStashRepoLink() + "craftbukkit.git", craftbukkit));
        }

        File spigot = new File(BT_WORK_DIR, "Spigot");
        if(!spigot.exists()) {
            threadPool.execute(new GitCloneTask(settings.getStashRepoLink() + "spigot.git", spigot));
        }

        File buildData = new File(BT_WORK_DIR, "BuildData");
        if(!buildData.exists()) {
            threadPool.execute(new GitCloneTask(settings.getStashRepoLink() + "builddata.git", buildData));
        }

        File maven = new File(System.getenv("M2_HOME"));
        if(!maven.exists()) {
            Future<File> future = threadPool.submit(new MavenInstaller(), maven);
            if(future.isDone()) {
                maven = future.get();
            }
        }

        String mvnBin = maven.getAbsolutePath() + "/bin/mvn";

        Git bukkitGit = Git.open(bukkit);
        Git craftBukkitGit = Git.open(craftbukkit);
        Git spigotGit = Git.open(spigot);
        Git buildDataGit = Git.open(buildData);

        BuildInfo buildInfo = new BuildInfo("Dev Build", "Development", 0, new BuildInfo.Refs("master", "master", "master", "master"));

        if(!options.isDontUpdate()) {
            String askedVersion = options.getVersion();

            System.out.println("Attempting to build version: \'" + askedVersion + "\'");

            BuildInfo versionBuildInfo = null;
            for(Map.Entry<Version, BuildInfo> entry : options.getVersionMap().entrySet()) {
                if(entry.getKey().getVersionString().equalsIgnoreCase(askedVersion)) {
                    versionBuildInfo = entry.getValue();
                    break;
                }
            }
            if(versionBuildInfo == null) {
                // download version
                File versionsJsonFile = new File(BT_WORK_DIR, "tmp/ " + askedVersion + ".json");
                if(!versionsJsonFile.exists()) {
                    // download it
                    Future<File> future = threadPool.submit(new FileDownloader(settings.getVersionLink() + askedVersion + ".json", versionsJsonFile), versionsJsonFile);
                    if(future.isDone()) {
                        versionsJsonFile = future.get();
                    }

                    InputStreamReader r = null;
                    try {
                        r = new FileReader(versionsJsonFile);
                        String rawJsonString = CharStreams.toString(r);
                        versionBuildInfo = new Gson().fromJson(rawJsonString, BuildInfo.class);
                    } finally {
                        if(r != null) {
                            r.close();
                        }
                    }
                }
            } // end if versionBuildInfo


            threadPool.execute(new GitPullTask(buildData, versionBuildInfo.getRefs().getBuildData()));
            threadPool.execute(new GitPullTask(bukkit, versionBuildInfo.getRefs().getBukkit()));
            threadPool.execute(new GitPullTask(craftbukkit, versionBuildInfo.getRefs().getCraftBukkit()));
            threadPool.execute(new GitPullTask(spigot, versionBuildInfo.getRefs().getSpigot()));
        } // end !dontUpdate if

        VersionInfo versionInfo = new Gson().fromJson(Files.newReader(new File(buildData, "info.json"), Charsets.UTF_8), VersionInfo.class);
        if(versionInfo == null) {
            versionInfo = new VersionInfo("1.12.2", "bukkit-1.12.2.at", "bukkit-1.12.2-cl.csrg", "bukkit.1.12.2-members.srg", null);
        }
        System.out.println("Attempting to build Minecraft with details: " + versionInfo);

        File vanillaJar = new File(BT_WORK_DIR, "minecraft_server." + versionInfo.getMinecraftVersion() + ".jar");
        if(!vanillaJar.exists() || ! HashChecker.checkHash(vanillaJar, versionInfo)) {
            // download jar
            String minecraftVersionUrl = String.format( "https://s3.amazonaws.com/Minecraft.Download/versions/%1$s/minecraft_server.%1$s.jar", versionInfo.getMinecraftVersion() );
            Future<File> future = threadPool.submit(new FileDownloader(minecraftVersionUrl, vanillaJar), vanillaJar);
            if(future.isDone()) {
                vanillaJar = future.get();
            }

            applyPatchesShell = System.getenv("SHELL");
            if(applyPatchesShell == null || applyPatchesShell.trim().isEmpty()) {
                applyPatchesShell = "bash";
            }
        }

        if(!HashChecker.checkHash(vanillaJar, versionInfo)) {
            System.out.println("*** Could not download clean Minecraft jar, giving up.");
            return false;
        }

        Iterable<RevCommit> mappings = buildDataGit.log()
                .addPath("mappings/" + versionInfo.getAccessTransforms())
                .addPath("mappings/" + versionInfo.getClassMappings())
                .addPath("mappings/" + versionInfo.getMemberMappings())
                .addPath("mappings/" + versionInfo.getPackageMappings())
                .setMaxCount(1).call();

        Hasher mappingsHash = Hashing.md5().newHasher();
        for(RevCommit rev : mappings) {
            mappingsHash.putString(rev.getName(), Charsets.UTF_8);
        }

        String mappingsVersion = mappingsHash.hash().toString().substring(24); // last 8 chars

        File finalMappedJar = new File(BT_WORK_DIR, "mapped." + mappingsVersion + ".jar");
        if(!finalMappedJar.exists()) {
            System.out.println("Final mapped jar: " + finalMappedJar + " does not exist! Creating...");

            File clMappedJar = new File(finalMappedJar + "-cl");
            File mMappedJar = new File(finalMappedJar + "-m");

            ProcessRunner.runProcess("java", "-jar", "BuildData/bin/SpecialSource-2.jar", "map", "-i", vanillaJar.getPath(), "-m", "BuildData/mappings/" + versionInfo.getClassMappings(), "-o", clMappedJar.getPath());

            ProcessRunner.runProcess("java", "-jar", "BuildData/bin/SpecialSource-2.jar", "map", "-i", clMappedJar.getPath(),
                    "-m", "BuildData/mappings/" + versionInfo.getMemberMappings(), "-o", mMappedJar.getPath() );

            ProcessRunner.runProcess("java", "-jar", "BuildData/bin/SpecialSource.jar", "--kill-lvt", "-i", mMappedJar.getPath(), "--access-transformer", "BuildData/mappings/" + versionInfo.getAccessTransforms(),
                    "-m", "BuildData/mappings/" + versionInfo.getPackageMappings(), "-o", finalMappedJar.getPath() );
        }

        ProcessRunner.runProcess( "sh", mvnBin, "install:install-file", "-Dfile=" + finalMappedJar, "-Dpackaging=jar", "-DgroupId=org.spigotmc",
                "-DartifactId=minecraft-server", "-Dversion=" + versionInfo.getMinecraftVersion() + "-SNAPSHOT" );

        File decompileDir = new File(BT_WORK_DIR, "decompile-" + mappingsVersion);
        if(!decompileDir.exists()) {
            decompileDir.mkdir();

            File clazzDir = new File(decompileDir, "classes");
            ZipUtil.unzip(finalMappedJar, clazzDir, new Predicate<String>() {
                @Override
                public boolean apply(String s) {
                    return s.startsWith("net/minecraft/server");
                }
            });
            if(versionInfo.getDecompileCommand() == null) {
                versionInfo.setDecompileCommand( "java -jar BuildData/bin/fernflower.jar -dgs=1 -hdc=0 -rbr=0 -asc=1 -udv=0 {0} {1}" );
            }

            ProcessRunner.runProcess(MessageFormat.format( versionInfo.getDecompileCommand(), clazzDir.getPath(), decompileDir.getPath() ).split( " " ));
        }

        System.out.println("Applying CraftBukkit patches");
        File nmsDir = new File(craftbukkit, "src/main/java/net");
        if(nmsDir.exists()) {
            System.out.println("Backing up NMS dir");
            FileUtils.moveDirectory(nmsDir, new File(BT_WORK_DIR, "nms.old." + System.currentTimeMillis()));
        }
        File patchDir = new File(craftbukkit, "nms-patches");
        for(File file : patchDir.listFiles()) {
            if(!file.getName().endsWith(".patch")) {
                continue;
            }

            String targetFile = "net/minecraft/server/" + file.getName().replaceAll(".patch", ".java");

            File clean = new File(decompileDir, targetFile);
            File t = new File(nmsDir.getParentFile(), targetFile);

            t.getParentFile().mkdirs();
            System.out.println("Patching with " + file.getName());

            List<String> readFile = Files.readLines(file, Charsets.UTF_8);

            boolean preludeFound = false;
            for ( int i = 0; i < Math.min( 3, readFile.size() ); i++ ) {
                if ( readFile.get( i ).startsWith( "+++" ) ) {
                    preludeFound = true;
                    break;
                }
            }
            if ( !preludeFound ) {
                readFile.add( 0, "+++" );
            }
            Patch parsedPatch = DiffUtils.parseUnifiedDiff( readFile );
            List<?> modifiedLines = DiffUtils.patch( Files.readLines( clean, Charsets.UTF_8 ), parsedPatch );

            BufferedWriter bw = new BufferedWriter( new FileWriter( t ) );
            for ( String line : (List<String>) modifiedLines )
            {
                bw.write( line );
                bw.newLine();
            }
            bw.close();
        } // end patchFiles for

        File tmpNms = new File(craftbukkit, "tmp-nms");
        FileUtils.copyDirectory(nmsDir, tmpNms);

        craftBukkitGit.branchDelete().setBranchNames("patched").setForce(true).call();
        craftBukkitGit.checkout().setCreateBranch(true).setForce(true).setName("patched").call();
        craftBukkitGit.add().addFilepattern("src/main/java/net/").call();
        craftBukkitGit.commit().setMessage("CraftBukkit $ " + new Date()).call();
        craftBukkitGit.checkout().setName(buildInfo.getRefs().getCraftBukkit()).call();

        FileUtils.moveDirectory(tmpNms, nmsDir);

        File spigotAPIDir = new File(spigot, "Bukkit");
        if(!spigotAPIDir.exists()) {
            threadPool.execute(new GitCloneTask("file:://" + bukkit.getAbsolutePath(), spigotAPIDir));
        }
        File spigotServerDir = new File(spigot, "CraftBukkit");
        if(!spigotServerDir.exists()) {
            threadPool.execute(new GitCloneTask("file:://" + craftbukkit.getAbsolutePath(), spigotServerDir));
        }

        if(!options.isSkipCompile()) {
            System.out.println("Compiling Bukkit");
            ProcessRunner.runProcess(bukkit, "sh", mvnBin, "clean", "install");
            if(options.isGenDoc()) {
                ProcessRunner.runProcess(bukkit, "sh", mvnBin, "javadoc:jar");
            }
            if(options.isGenSrc()) {
                ProcessRunner.runProcess(bukkit, "sh", mvnBin, "source:jar");
            }

            System.out.println("Compiling CraftBukkit");
            ProcessRunner.runProcess(craftbukkit, "sh", mvnBin, "clean", "install");
        }

        try {
            ProcessRunner.runProcess(spigot, applyPatchesShell, "applypatches.sh");
            System.out.println("*** Spigot patcehs applied!");

            if(!options.isSkipCompile()) {
                System.out.println("Compiling Spigot & Spigot API");
                ProcessRunner.runProcess(spigot, "sh", mvnBin, "clean", "install");
            }
        } catch(Exception e) {
            System.err.println("Error compiling Spigot. Please check the wiki for FAQs.");
            System.err.println("If this does not resolve your issue then please pastebin this log when seeking support.");
            e.printStackTrace();
            System.exit(1);
        }

        IntStream.range(1, 26).forEach((consumer) -> System.out.println(" "));

        if(!options.isSkipCompile()) {
            System.out.println("Success! Everything compiled successfully. Copying final .jar files now.");
            FileUtil.copyJar("CraftBukkit/target", "craftbukkit", new File(BT_WORK_DIR, "out/" + "craftbukkit-" + versionInfo.getMinecraftVersion() + ".jar"));
            FileUtil.copyJar("Spigot/Spigot-Server/target", "spigot", new File(BT_WORK_DIR, "out/ " + "spigot-" + versionInfo.getMinecraftVersion() + ".jar"));

            File out;
            for(String dirPath : options.getOutputDirectories()) {
                out = new File(dirPath);
                if(!out.exists()) {
                    out.mkdirs();
                }
                FileUtil.copyJar("CraftBukkit/target", "craftbukkit", new File(out, "craftbukkit-" + versionInfo.getMinecraftVersion() + ".jar"));
                FileUtil.copyJar("Spigot/Spigot-Server/target", "spigot", new File(out, "spigot-" + versionInfo.getMinecraftVersion() + ".jar"));
            }
        }

        return true;
    }

    private void createDir(File file) {
        if(!FileUtil.isDirectory(file)) {
            file.mkdir();
        }
    }
}
