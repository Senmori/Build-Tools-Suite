/*
 * Copyright (c) 2018, Senmori. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package net.senmori.btsuite.buildtools;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import difflib.DiffUtils;
import difflib.Patch;
import net.senmori.btsuite.Builder;
import net.senmori.btsuite.command.CommandHandler;
import net.senmori.btsuite.minecraft.VersionManifest;
import net.senmori.btsuite.pool.TaskPool;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.SettingsFactory;
import net.senmori.btsuite.task.FileDownloadTask;
import net.senmori.btsuite.task.GitCloneTask;
import net.senmori.btsuite.task.GitPullTask;
import net.senmori.btsuite.task.InvalidateCacheTask;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.HashChecker;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.ZipUtil;
import net.senmori.btsuite.util.builders.MavenCommandBuilder;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class BuildToolsProject implements Callable<Boolean> {
    private static String applyPatchesShell = "sh";

    private final BuildTools options;
    private final BuildToolsSettings buildToolsSettings;
    private final BuildToolsSettings.Directories dirs;

    private final TaskPool projectPool = TaskPools.createSingleTaskPool();

    public BuildToolsProject(BuildTools options, BuildToolsSettings buildToolsSettings) {
        this.options = options;
        this.buildToolsSettings = buildToolsSettings;
        this.dirs = buildToolsSettings.getDirectories();
    }

    @Override
    public Boolean call() throws Exception {
        Stopwatch watch = Stopwatch.createStarted();
        File work = dirs.getWorkDir().getFile();
        printOptions(options);

        if ( options.isInvalidateCache() ) {
            InvalidateCacheTask task = new InvalidateCacheTask( Builder.WORKING_DIR.getFile() );
            boolean taskResult = task.call();
        }

        File bukkit = new File( dirs.getWorkingDir().getFile(), "Bukkit" );
        if ( ! bukkit.exists() ) {
            String repo = buildToolsSettings.getStashRepoLink() + "bukkit.git";
            GitCloneTask task = GitCloneTask.clone(repo, bukkit);
            projectPool.submit( task ).get();
        }

        File craftBukkit = new File( dirs.getWorkingDir().getFile(), "CraftBukkit" );
        if ( ! craftBukkit.exists() ) {
            String repo = buildToolsSettings.getStashRepoLink() + "craftbukkit.git";
            GitCloneTask task = GitCloneTask.clone(repo, craftBukkit);
            projectPool.submit( task ).get();
        }

        File spigot = new File( dirs.getWorkingDir().getFile(), "Spigot" );
        if ( ! spigot.exists() ) {
            String repo = buildToolsSettings.getStashRepoLink() + "spigot.git";
            GitCloneTask task = GitCloneTask.clone(repo, spigot);
            projectPool.submit( task ).get();
        }

        File buildData = new File( dirs.getWorkingDir().getFile(), "BuildData" );
        if ( ! buildData.exists() ) {
            String repo = buildToolsSettings.getStashRepoLink() + "builddata.git";
            GitCloneTask task = GitCloneTask.clone(repo, buildData);
            projectPool.submit( task ).get();
        }


        String mvn = new File( dirs.getMvnDir().getFile(), "/bin/mvn" ).getAbsolutePath();

        Git bukkitGit = Git.open(bukkit);
        Git craftBukkitGit = Git.open(craftBukkit);
        Git spigotGit = Git.open(spigot);
        Git buildGit = Git.open(buildData);

        BuildInfo buildInfo = BuildInfo.getDefaultImpl();

        if ( !options.isDontUpdate() ) {
            final String askedVersion = options.getVersion();
            LogHandler.info("Attempting to build version: '" + askedVersion + "' use --rev <version> to override");

            String text = askedVersion + ".json";
            File versionsDir = new File( dirs.getVersionsDir().getFile(), "spigot" );
            versionsDir.mkdirs();
            File verInfo = new File( versionsDir, text );
            if ( !verInfo.exists() ) {
                // download file
                String url = buildToolsSettings.getVersionLink() + askedVersion + ".json";
                verInfo = projectPool.submit( new FileDownloadTask( url, verInfo ) ).get();
            }
            LogHandler.debug("Found version " + askedVersion);
            buildInfo = SettingsFactory.getGson().fromJson( new FileReader( verInfo ), BuildInfo.class );

            projectPool.submit(GitPullTask.pull(buildGit, buildInfo.getRefs().getBuildData())).get();
            projectPool.submit(GitPullTask.pull(bukkitGit, buildInfo.getRefs().getBukkit())).get();
            projectPool.submit(GitPullTask.pull(craftBukkitGit, buildInfo.getRefs().getCraftBukkit())).get();
            projectPool.submit(GitPullTask.pull(spigotGit, buildInfo.getRefs().getSpigot())).get();
        }

        File infoFile = new File( dirs.getWorkingDir().getFile(), "BuildData/info.json" );
        infoFile.mkdirs();
        VersionInfo versionInfo = SettingsFactory.getGson().fromJson( new FileReader( infoFile ), VersionInfo.class );
        // Default to latest version
        if ( versionInfo == null ) {
            LogHandler.debug( "Could not generate version. Using default ( " + buildToolsSettings.getDefaultVersion() + " )." );
            versionInfo = buildToolsSettings.getBukkitVersionInfo( buildToolsSettings.getDefaultVersion() );
        }
        LogHandler.info("Attempting to build Minecraft with details: " + versionInfo);

        String jarName = "minecraft_server." + versionInfo.getMinecraftVersion() + ".jar";
        File vanillaJar = new File( dirs.getJarDir().getFile(), jarName );

        if ( ! vanillaJar.exists() || ! HashChecker.checkHash(vanillaJar, versionInfo) ) {
                String url = VersionManifest.getInstance().getVersion( versionInfo.getMinecraftVersion() ).getServerDownloadURL();
                vanillaJar = projectPool.submit( new FileDownloadTask( url, vanillaJar ) ).get();
        }

        if ( !HashChecker.checkHash(vanillaJar, versionInfo) ) {
            LogHandler.error("**** Could not download clean Minecraft jar, giving up.");
            options.setFinished();
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

        File finalMappedJar = new File( dirs.getJarDir().getFile(), "mapped." + mappingsVersion + ".jar" );
        if ( ! finalMappedJar.exists() ) {
            LogHandler.info("Final mapped jar: " + finalMappedJar + " does not exist, creating!");

            File clMappedJar = new File(finalMappedJar + "-cl");
            File mMappedJar = new File(finalMappedJar + "-m");

            File ss = new File( buildData, "bin/SpecialSource.jar" );
            File ss2 = new File( buildData, "bin/SpecialSource-2.jar" );

            File fernflower = new File(buildData, "bin/fernflower.jar");
            File specialSource = new File(buildData, "bin/SpecialSource.jar");
            File specialSource_2 = new File(buildData, "bin/SpecialSource-2.jar");

            LogHandler.info( "Applying first mappings..." );
            CommandHandler.getCommandIssuer().executeCommand( buildData, "java", "-jar", specialSource_2.getAbsolutePath(), "map", "-i", vanillaJar.getAbsolutePath(), "-m", "mappings/" + versionInfo.getClassMappings(), "-o", clMappedJar.getAbsolutePath() );
            LogHandler.info( clMappedJar.getName() + " created!" );

            LogHandler.info( "Applying second mappings..." );
            CommandHandler.getCommandIssuer().executeCommand( buildData, "java", "-jar", specialSource_2.getAbsolutePath(), "map", "-i", clMappedJar.getAbsolutePath(),
                    "-m", "mappings/" + versionInfo.getMemberMappings(), "-o", mMappedJar.getAbsolutePath());
            LogHandler.info( mMappedJar.getName() + " created!" );

            LogHandler.info( "Applying final mappings..." );
            CommandHandler.getCommandIssuer().executeCommand( buildData, "java", "-jar", specialSource.getAbsolutePath(), "--kill-lvt", "-i", mMappedJar.getAbsolutePath(), "--access-transformer", "mappings/" + versionInfo.getAccessTransforms(),
                    "-m", "mappings/" + versionInfo.getPackageMappings(), "-o", finalMappedJar.getAbsolutePath());
        }

        LogHandler.info( "Installing '" + finalMappedJar.getName() + "' to local repository" );

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
               .setInteractiveMode( false )
               .setBaseDirectory( dirs.getJarDir().getFile() )
               .setGoals( Arrays.asList( goals ) );
        InvocationResult result = install.execute();
        if(result.getExitCode() != 0) {
            LogHandler.error(result.getExecutionException().getMessage());
            options.setFinished();
            return false;
        }

        LogHandler.debug("Decompiling:");
        File decompileDir = new File( dirs.getWorkDir().getFile(), "decompile-" + mappingsVersion );
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

            File fernJar = new File( dirs.getWorkingDir().getFile(), "BuildData/bin/fernflower.jar" );
            LogHandler.info( "Running decompile command" );
            CommandHandler.getCommandIssuer().executeCommand( dirs.getWorkingDir().getFile(), MessageFormat.format( versionInfo.getDecompileCommand(), clazzDir.getAbsolutePath(), decompileDir.getAbsolutePath() ).split( " " ) );
        }

        LogHandler.info("Applying CraftBukkit Patches");
        File nmsDir = new File(craftBukkit, "src/main/java/net");
        nmsDir.mkdirs();
        if ( nmsDir.exists() ) {
            LogHandler.info("Backing up NMS dir");
            FileUtils.moveDirectory( nmsDir, new File( dirs.getTmpDir().getFile(), "nms.old." + System.currentTimeMillis() ) );
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

        LogHandler.info( "Moving " + tmpNms.getName() + " to " + nmsDir.getAbsolutePath() );
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
            MavenCommandBuilder mvnBuild = MavenCommandBuilder.builder().setInteractiveMode( false );
            LogHandler.info("Compiling Bukkit");
            mvnBuild.setBaseDirectory( bukkit ).setGoals( Arrays.asList( "install" ) ).execute();
            if ( options.isGenDoc() ) {
                mvnBuild.setBaseDirectory( bukkit ).setGoals( Arrays.asList( "javadoc:jar" ) ).execute();
            }
            if ( options.isGenSrc() ) {
                mvnBuild.setBaseDirectory( bukkit ).setGoals( Arrays.asList( "source:jar" ) ).execute();
            }
            LogHandler.info("Compiling CraftBukkit");
            mvnBuild.setBaseDirectory( craftBukkit ).setGoals( Arrays.asList( "install" ) ).execute();
        }

        try {
            LogHandler.info( "Starting applyPatches." );
            File spigotDir = new File( dirs.getWorkingDir().getFile(), "Spigot" );
            File script = new File( spigotDir, "applyPatches.sh" );
            CommandHandler.getCommandIssuer().executeCommand( spigotDir, applyPatchesShell, "applyPatches.sh" );
            LogHandler.info( "*** Spigot patches applied!" );

            if ( ! options.isSkipCompile() ) {
                LogHandler.info( "Compiling Spigot & Spigot-API" );
                MavenCommandBuilder spigotBuilder = MavenCommandBuilder.builder()
                                                                       .setBaseDirectory( new File( dirs.getWorkingDir().getFile(), "Spigot" ) )
                                                                       .setInteractiveMode( false )
                                                                       .setGoals( Arrays.asList( "clean", "install" ) );
                spigotBuilder.execute().getExitCode();
            }
        } catch ( Exception ex ) {
            LogHandler.error( "Error compiling Spigot. Please check the wiki for FAQs." );
            LogHandler.error( "If this does not resolve your issue then please pastebin the entire console when seeking support." );
            ex.printStackTrace();
            options.setFinished();
            return false;
        }


        for ( int i = 0; i < 5; i++ ) {
            LogHandler.info( " " );
        }

        final String version = versionInfo.getMinecraftVersion();
        if ( !options.isSkipCompile() ) {
            projectPool.submit( () -> {
                LogHandler.info( "Success! Everything compiled successfully. Copying final .jar files now." );
                LogHandler.debug( "OutputDirectories: " + options.getOutputDirectories() );
                final File bukkitSourceDir = new File( dirs.getWorkingDir().getFile(), "Bukkit/target" );
                final File craftSourceDir = new File( dirs.getWorkingDir().getFile(), "CraftBukkit/target" );
                final File spigotSourceDir = new File( dirs.getWorkingDir().getFile(), "Spigot/Spigot-Server/target" );
                final Predicate<String> isValidJar = new Predicate<String>() {
                    @Override
                    public boolean test(String str) {
                        return str.contains( version ) &&
                                       ! str.startsWith( "original" ) &&
                                       ! str.contains( "-shaded" ) &&
                                       ! str.contains( "-remapped" ) &&
                                       str.endsWith( ".jar" );
                    }
                };
                for ( String outputDir : options.getOutputDirectories() ) {
                    try {
                        FileUtil.copyJar( craftSourceDir, new File( outputDir ), "craftbukkit-" + version + ".jar", isValidJar );
                        FileUtil.copyJar( spigotSourceDir, new File( outputDir ), "spigot-" + version + ".jar", isValidJar );
                        if ( options.isGenSrc() ) {
                            FileUtil.copyJar( bukkitSourceDir, new File( outputDir ), "bukkit-" + version + "-sources.jar", (str) -> isValidJar.test( str ) && str.contains( "-sources" ) );
                        }
                        if ( options.isGenDoc() ) {
                            FileUtil.copyJar( bukkitSourceDir, new File( outputDir ), "bukkit-" + version + "-javadoc.jar", (str) -> isValidJar.test( str ) && str.contains( "-javadoc" ) );
                        }
                    } catch ( IOException e ) {
                        e.printStackTrace();
                        break;
                    }
                }
                FileUtil.deleteFilesInDirectory( craftSourceDir, (str) -> str.endsWith( ".jar" ) );
                FileUtil.deleteFilesInDirectory( spigotSourceDir, (str) -> str.endsWith( ".jar" ) );
                if ( options.isGenSrc() ) {
                    FileUtil.deleteFilesInDirectory( bukkitSourceDir, (str) -> isValidJar.test( str ) && str.contains( "-sources" ) );
                }
                if ( options.isGenDoc() ) {
                    FileUtil.deleteFilesInDirectory( bukkitSourceDir, (str) -> isValidJar.test( str ) && str.contains( "-javadoc" ) );
                }
                return true;
            } ).get();

        }
        LogHandler.info( "BuildToolsSuite has finished!" );
        watch = watch.stop();
        long seconds = watch.elapsed( TimeUnit.SECONDS );
        String formatted = String.format( "%d:%02d", seconds / 60, seconds % 60 );
        LogHandler.info( "It took " + formatted + " to complete this build." );
        projectPool.getService().shutdown();
        options.setFinished();
        return ! options.isRunning();
    }

    private void printOptions(BuildTools options) {
        LogHandler.info( "BuildToolsSuite Options: " );
        LogHandler.info("Disable Certificate Check: " + options.isDisableCertificateCheck());
        LogHandler.info("Don't Update: " + options.isDontUpdate());
        LogHandler.info("Skip Compile: " + options.isSkipCompile());
        LogHandler.info("Generate Sources: " + options.isGenSrc());
        LogHandler.info("Generate Docs: " + options.isGenDoc());
        LogHandler.info( "Version: " + options.getVersion() );
        for ( String dir : options.getOutputDirectories() ) {
            LogHandler.info("Output Dir: " + dir);
        }
    }

}
