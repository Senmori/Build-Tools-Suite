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

package net.senmori.btsuite.task;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import javafx.concurrent.Task;
import net.senmori.btsuite.Console;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.buildtools.BuildTools;
import net.senmori.btsuite.buildtools.VersionInfo;
import net.senmori.btsuite.command.CommandHandler;
import net.senmori.btsuite.command.ICommandIssuer;
import net.senmori.btsuite.minecraft.VersionManifest;
import net.senmori.btsuite.pool.TaskPool;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.Directory;
import net.senmori.btsuite.storage.SettingsFactory;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.HashChecker;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.builders.MavenCommandBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public final class BuildToolsTask extends Task<Long> {
    private String applyPatchesShell = "sh";

    private final BuildTools options;
    private final BuildToolsSettings settings;
    private final BuildToolsSettings.Directories dirs;
    private final Console console;

    private final TaskPool projectPool = TaskPools.createSingleTaskPool();

    public BuildToolsTask(BuildTools options) {
        this.options = options;
        this.settings = options.getSettings();
        this.dirs = settings.getDirectories();
        this.console = options.getConsole();
    }

    @Override
    protected Long call() throws Exception {
        Stopwatch watch = Stopwatch.createStarted();
        printOptions( options );

        if ( options.isInvalidateCache() ) {
            VersionManifest manifest = options.getVersionManifest();
            InvalidateCacheTask task = new InvalidateCacheTask( options, manifest );
            projectPool.submit( task );
            task.get();
        }
        console.setOptionalText( "" );

        LogHandler.info( "Cloning Bukkit..." );
        Directory bukkit = new Directory( dirs.getWorkingDir(), "Bukkit" );
        if ( !bukkit.getFile().exists() ) {
            bukkit.getFile().mkdirs();
            String repo = settings.getStashRepoLink() + "bukkit.git";
            GitCloneTask task = new GitCloneTask( repo, bukkit, console );
            projectPool.submit( task );
            task.get();
        } else {
            LogHandler.info( "Found Bukkit repository at " + bukkit );
        }
        console.setOptionalText( "" );

        LogHandler.info( "Cloning CraftBukkit..." );
        Directory craftBukkit = new Directory( dirs.getWorkingDir(), "CraftBukkit" );
        if ( !craftBukkit.getFile().exists() ) {
            craftBukkit.getFile().mkdirs();
            String repo = settings.getStashRepoLink() + "craftbukkit.git";
            GitCloneTask task = new GitCloneTask( repo, craftBukkit, console );
            projectPool.submit( task );
            task.get();
        } else {
            LogHandler.info( "Found CraftBukkit repository at " + craftBukkit );
        }
        console.setOptionalText( "" );

        LogHandler.info( "Cloning Spigot..." );
        Directory spigot = new Directory( dirs.getWorkingDir(), "Spigot" );
        if ( !spigot.getFile().exists() ) {
            spigot.getFile().mkdirs();
            String repo = settings.getStashRepoLink() + "spigot.git";
            GitCloneTask task = new GitCloneTask( repo, spigot, console );
            projectPool.submit( task );
            task.get();
        } else {
            LogHandler.info( "Found Spigot repository at " + spigot );
        }
        console.setOptionalText( "" );

        LogHandler.info( "Cloning BuildData..." );
        Directory buildData = new Directory( dirs.getWorkingDir(), "BuildData" );
        if ( !buildData.getFile().exists() ) {
            buildData.getFile().mkdirs();
            String repo = settings.getStashRepoLink() + "builddata.git";
            GitCloneTask task = new GitCloneTask( repo, buildData, console );
            projectPool.submit( task );
            task.get();
        } else {
            LogHandler.info( "Found BuildData repository at " + buildData );
        }
        console.setOptionalText( "" );

        dirs.getMvnDir().getFile().mkdirs();
        Directory mvnBinDir = new Directory( dirs.getMvnDir().getFile(), "/bin/mvn" );
        String mvn = mvnBinDir.toString();

        BuildInfo buildInfo = BuildInfo.getDefaultImpl();

        Git buildGit = Git.open( buildData.getFile() );
        Git craftBukkitGit = Git.open( craftBukkit.getFile() );
        if ( !options.isDontUpdate() ) {
            dirs.getVersionsDir().getFile().mkdirs();
            String askedVersion = options.getVersion();
            LogHandler.info( "Attempting to build version: " + askedVersion );

            String versionText = askedVersion + ".json";
            Directory versionsDirectory = new Directory( dirs.getVersionsDir(), "spigot" );
            Directory versionInfo = new Directory( versionsDirectory, versionText );
            if ( !versionInfo.exists() ) {
                String url = settings.getVersionLink() + askedVersion + ".json";
                FileDownloadTask dlTask = new FileDownloadTask( url, versionInfo, console );
                projectPool.submit( dlTask );
                dlTask.get();
                LogHandler.info( "Downloaded " + askedVersion + "\'s version file." );
            } else {
                LogHandler.info( "Found version " + askedVersion );
            }
            FileReader versionFileReader = new FileReader( versionInfo.getFile() );
            buildInfo = SettingsFactory.getGson().fromJson( versionFileReader, BuildInfo.class );
            console.setOptionalText( "" );

            // BuildData
            LogHandler.info( "Pulling updates from BuildData remote..." );
            GitPullTask buildDataTask = new GitPullTask( buildGit, buildInfo.getRefs().getBuildData(), console );
            projectPool.submit( buildDataTask );
            buildDataTask.get();
            console.setOptionalText( "" );

            // Bukkit
            Git bukkitGit = Git.open( bukkit.getFile() );

            LogHandler.info( "Pulling updates from Bukkit remote..." );
            GitPullTask bukkitTask = new GitPullTask( bukkitGit, buildInfo.getRefs().getBukkit(), console );
            projectPool.submit( bukkitTask );
            bukkitTask.get();
            clearRefs( bukkitGit, bukkit );
            console.setOptionalText( "" );

            // CraftBukkit
            LogHandler.info( "Pulling updates from CraftBukkit remote..." );
            GitPullTask craftBukkitTask = new GitPullTask( craftBukkitGit, buildInfo.getRefs().getCraftBukkit(), console );
            projectPool.submit( craftBukkitTask );
            craftBukkitTask.get();
            console.setOptionalText( "" );

            // Spigot
            Git spigotGit = Git.open( spigot.getFile() );

            LogHandler.info( "Pulling updates from BuildData remote..." );
            GitPullTask spigotTask = new GitPullTask( spigotGit, buildInfo.getRefs().getSpigot(), console );
            projectPool.submit( spigotTask );
            spigotTask.get();
        }
        console.setOptionalText( "" );

        Directory infoFile = new Directory( dirs.getWorkingDir(), "BuildData/info.json" );
        infoFile.getFile().mkdirs();
        FileReader infoReader = new FileReader( infoFile.getFile() );
        VersionInfo versionInfo = SettingsFactory.getGson().fromJson( infoReader, VersionInfo.class );
        // Default to latest version
        if ( versionInfo == null ) {
            LogHandler.info( "Could not generate version. Using default ( " + settings.getDefaultVersion() + " )." );
            versionInfo = settings.getBukkitVersionInfo( settings.getDefaultVersion() );
        }
        LogHandler.info( "Attempting to build Minecraft with details: " + versionInfo );

        String jarName = "minecraft_server." + versionInfo.getMinecraftVersion() + ".jar";
        dirs.getJarDir().getFile().mkdirs();
        Directory vanillaJar = new Directory( dirs.getJarDir(), jarName );
        if ( !vanillaJar.exists() || !HashChecker.checkHash( vanillaJar.getFile(), versionInfo ) ) {
            LogHandler.info( "Downloading Minecraft Server Version \'" + versionInfo.getMinecraftVersion() + "\' jar." );
            String url = options.getVersionManifest().getVersion( versionInfo.getMinecraftVersion() ).getServerDownloadURL();
            FileDownloadTask jarDLTask = new FileDownloadTask( url, vanillaJar, console );
            projectPool.submit( jarDLTask );
            jarDLTask.get();
        }
        console.setOptionalText( "" );

        if ( !HashChecker.checkHash( vanillaJar.getFile(), versionInfo ) ) {
            LogHandler.error( "**** Could not download clean Minecraft jar, giving up. ****" );
            options.setRunning( false );
            cancel( true );
            return -1L;
        }

        Iterable<RevCommit> mappings = buildGit.log()
                                               .addPath( "mappings/" + versionInfo.getAccessTransforms() )
                                               .addPath( "mappings/" + versionInfo.getClassMappings() )
                                               .addPath( "mappings/" + versionInfo.getMemberMappings() )
                                               .addPath( "mappings/" + versionInfo.getPackageMappings() )
                                               .setMaxCount( 1 ).call();

        Hasher mappingsHash = Hashing.md5().newHasher();
        for ( RevCommit rev : mappings ) {
            mappingsHash.putString( rev.getName(), Charsets.UTF_8 );
        }
        String mappingsVersion = mappingsHash.hash().toString().substring( 24 ); // Last 8 chars

        LogHandler.info( "Attempting to create mapped jar." );
        File finalMappedJar = new File( dirs.getJarDir().getFile(), "mapped." + mappingsVersion + ".jar" );
        if ( !finalMappedJar.exists() ) {
            LogHandler.info( "Final mapped jar: " + finalMappedJar + " does not exist, creating!" );

            File clMappedJar = new File( finalMappedJar + "-cl" );
            File mMappedJar = new File( finalMappedJar + "-m" );

            Directory specialSource = new Directory( buildData, "bin/SpecialSource.jar" );
            Directory specialSource2 = new Directory( buildData, "bin/SpecialSource-2.jar" );

            String ssPath = specialSource.toString();
            String ss2Path = specialSource2.toString();
            String vanillaJarPath = vanillaJar.toString();

            LogHandler.info( "Applying first mappings..." );
            CommandHandler.getCommandIssuer().executeCommand( buildData.getFile(), "java", "-jar", ss2Path, "map", "-i", vanillaJarPath, "-m", "mappings/" + versionInfo.getClassMappings(), "-o", clMappedJar.getAbsolutePath() );
            LogHandler.info( clMappedJar.getName() + " created!" );

            LogHandler.info( "Applying second mappings..." );
            CommandHandler.getCommandIssuer().executeCommand( buildData.getFile(), "java", "-jar", ss2Path, "map", "-i", clMappedJar.getAbsolutePath(),
                    "-m", "mappings/" + versionInfo.getMemberMappings(), "-o", mMappedJar.getAbsolutePath() );
            LogHandler.info( mMappedJar.getName() + " created!" );

            LogHandler.info( "Applying final mappings..." );
            CommandHandler.getCommandIssuer().executeCommand( buildData.getFile(), "java", "-jar", ssPath, "--kill-lvt", "-i", mMappedJar.getAbsolutePath(), "--access-transformer", "mappings/" + versionInfo.getAccessTransforms(),
                    "-m", "mappings/" + versionInfo.getPackageMappings(), "-o", finalMappedJar.getAbsolutePath() );
        } else {
            LogHandler.info( "Mapped jar for mappings version \'" + mappingsVersion + "\' already exists!" );
        }
        console.setOptionalText( "" );

        LogHandler.info( "Installing \'" + finalMappedJar.getName() + "\' to local repository" );

        String[] goals = new String[] { "sh", mvn,
                "install:install-file", "-Dfile=" + finalMappedJar.getAbsolutePath(),
                "-Dpackaging=jar", "-DgroupId=org.spigotmc",
                "-DartifactId=minecraft-server", "-Dversion=" + versionInfo.getMinecraftVersion() + "-SNAPSHOT"
        };
        ICommandIssuer command = CommandHandler.getCommandIssuer();
        command.executeCommand( dirs.getWorkingDir().getFile(), goals );

        LogHandler.info( "Decompiling: " );
        Directory decompileDir = new Directory( dirs.getWorkDir(), "decompile-" + mappingsVersion );
        if ( !decompileDir.exists() ) {
            decompileDir.getFile().mkdir();

            LogHandler.info( "Extracting " + finalMappedJar.getName() + " into " + decompileDir.toString() );
            Directory clazzDir = new Directory( decompileDir, "classes" );
            clazzDir.getFile().mkdir();
            LogHandler.debug( "Unzipping " + finalMappedJar.getName() + " into " + clazzDir );

            ExtractFilesTask task = new ExtractFilesTask( finalMappedJar, clazzDir.getFile(), console, (str) -> str.startsWith( "net/minecraft/server/" ) );
            task.call();
            console.setOptionalText( "" );

            if ( versionInfo.getDecompileCommand() == null ) {
                LogHandler.info( "Set decompile command for VersionInfo ( " + versionInfo.getMinecraftVersion() + " )" );
                versionInfo.setDecompileCommand( "java -jar BuildData/bin/fernflower.jar -dgs=1 -hdc=0 -rbr=0 -asc=1 -udv=0 {0} {1}" );
            }

            // call fernflower - all this is so we can capture the output and redirect it accordingly
            LogHandler.info( "Running decompile command..." );
            ICommandIssuer handler = CommandHandler.getCommandIssuer();
            Process process = handler.issue( dirs.getWorkingDir().getFile(), MessageFormat.format( versionInfo.getDecompileCommand(), clazzDir.toString(), decompileDir.toString() ).split( " " ) );
            BufferedReader reader = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
            String line = "";
            int numDecompiled = 0;
            while ( ( line = reader.readLine() ) != null ) {
                if ( line.startsWith( ".." ) ) {
                    // it's a ...done message, ignore it
                }
                String[] split = line.split( ":" );
                if ( split.length > 1 ) {
                    console.setOptionalText( split[ 1 ] );
                    numDecompiled++;
                } else {
                    console.setOptionalText( line );
                    numDecompiled++;
                }
            }

            LogHandler.info( "Finished decompiling " + numDecompiled + "classes in " + FilenameUtils.getBaseName( clazzDir.getFile().getName() ) );
        } else {
            // check to see if the directory has net/minecraft/server
            if ( decompileDir.isDirectory() && decompileDir.getFile().listFiles().length > 0 ) {
                LogHandler.info( "Decompile directory " + decompileDir + " already exists!" );
            } else {
                LogHandler.error( "*** Decompile directory is not valid. Please delete it, or try invalidating your cache! ***" );
                options.setRunning( false );
                cancel( true );
                return -1L;
            }
        }
        // end decompileDir check
        console.setOptionalText( "" );

        LogHandler.info( "Applying CraftBukkit Patches" );
        Directory nmsDir = new Directory( craftBukkit, "src/main/java/net/" );
        if ( nmsDir.exists() ) {
            dirs.getTmpDir().getFile().mkdirs();
            LogHandler.info( "Backing up NMS dir" );
            FileUtils.moveDirectory( nmsDir.getFile(), new File( dirs.getTmpDir().getFile(), "nms.old." + System.currentTimeMillis() ) );
        }

        LogHandler.info( "Starting NMS patches..." );
        Directory patchDir = new Directory( craftBukkit, "nms-patches" );
        /*
         * Apply patches from 'patchDir' to the files in 'decompileDir' and put the resulting file in 'nmsDir'
         */
        LogHandler.info( "Patch File Dir: " + patchDir.getPath() );
        LogHandler.info( "Source File Dir: " + decompileDir.getPath() );
        LogHandler.info( "Output File Dir: " + nmsDir.getPath() );
        ApplyPatchesTask applyPatchesTask = new ApplyPatchesTask( patchDir.getFile(), decompileDir.getFile(), nmsDir.getFile(), console );
        projectPool.submit( applyPatchesTask );
        int patchesApplied = applyPatchesTask.get();
        File[] patchDirFiles = patchDir.getFile().listFiles();
        int totalPatches = ( patchDirFiles == null || patchDirFiles.length < 1 ) ? 0 : patchDirFiles.length;

        Directory cbNMSDir = new Directory( nmsDir, "minecraft/server/" );
        if ( cbNMSDir.exists() ) {
            LogHandler.info( "Applied " + patchesApplied + "/" + totalPatches + " patches." );
        }
        console.setOptionalText( "" );
        LogHandler.info( "Patching complete!" );

        Directory tmpNms = new Directory( craftBukkit, "tmp-nms" );
        FileUtils.copyDirectory( nmsDir.getFile(), tmpNms.getFile() );

        LogHandler.info( "Deleting \'patched\' branch..." );
        craftBukkitGit.branchDelete().setBranchNames( "patched" ).setForce( true ).call();

        LogHandler.info( "Create new \'patched\' branch..." );
        craftBukkitGit.checkout().setCreateBranch( true ).setForce( true ).setName( "patched" ).call();

        LogHandler.info( "Add all files that match \'src/main/java/net\' pattern..." );
        craftBukkitGit.add().addFilepattern( "src/main/java/net/" ).call();

        LogHandler.info( "Setting commit message." );
        craftBukkitGit.commit().setMessage( "CraftBukkit $ " + new Date() ).call();

        LogHandler.info( "Checking out " + buildInfo.getRefs().getCraftBukkit() );
        craftBukkitGit.checkout().setName( buildInfo.getRefs().getCraftBukkit() ).call();

        LogHandler.info( "Moving " + tmpNms.getFile().getName() + " to " + nmsDir.toString() );
        FileUtils.moveDirectory( tmpNms.getFile(), nmsDir.getFile() );

        LogHandler.info( "Cloning SpigotAPI" );
        Directory spigotApi = new Directory( spigot, "Bukkit" );
        if ( !spigotApi.exists() ) {
            String url = "file://" + bukkit.getFile().getAbsolutePath();
            GitCloneTask task = new GitCloneTask( url, spigotApi, console );
            projectPool.submit( task );
            task.get();
        }
        console.setOptionalText( "" );

        LogHandler.info( "Cloning SpigotServer" );
        Directory spigotServer = new Directory( spigot, "CraftBukkit" );
        if ( !spigotServer.exists() ) {
            String url = "file://" + craftBukkit.toString();
            GitCloneTask task = new GitCloneTask( url, spigotServer, console );
            projectPool.submit( task );
            task.get();
        }
        console.setOptionalText( "" );

        if ( !options.isSkipCompile() ) {
            MavenCommandBuilder mvnBuild = MavenCommandBuilder.builder().setInteractiveMode( false );
            LogHandler.info( "### Compiling Bukkit ###" );
            mvnBuild.setBaseDirectory( bukkit.getFile() ).setGoals( Arrays.asList( "install" ) ).execute();
            if ( options.isGenDocumentation() ) {
                mvnBuild.setBaseDirectory( bukkit.getFile() ).setGoals( Arrays.asList( "javadoc:jar" ) ).execute();
            }
            if ( options.isGenSource() ) {
                mvnBuild.setBaseDirectory( bukkit.getFile() ).setGoals( Arrays.asList( "source:jar" ) ).execute();
            }
            LogHandler.info( "### Compiling CraftBukkit ###" );
            mvnBuild.setBaseDirectory( craftBukkit.getFile() ).setGoals( Arrays.asList( "install" ) ).execute();
        }
        console.setOptionalText( "" );

        try {
            LogHandler.info( "Starting applyPatches for Spigot" );
            Directory spigotDir = new Directory( dirs.getWorkingDir(), "Spigot" );
            CommandHandler.getCommandIssuer().executeCommand( spigotDir.getFile(), applyPatchesShell, "applyPatches.sh" );
            ;

            LogHandler.info( "*** Spigot patches applied!" );

            if ( !options.isSkipCompile() ) {
                LogHandler.info( "### Compiling Spigot & Spigot-API ###" );
                MavenCommandBuilder spigotBuilder = MavenCommandBuilder.builder()
                        .setBaseDirectory( spigotDir.getFile() )
                                                                       .setInteractiveMode( false )
                                                                       .setGoals( Arrays.asList( "clean", "install" ) );
                spigotBuilder.execute().getExitCode();
            }
        } catch ( Exception ex ) {
            LogHandler.error( "Error compiling Spigot. Please check the wiki for FAQs." );
            LogHandler.error( "If this does not resolve your issue then please pastebin the entire console when seeking support." );
            ex.printStackTrace();
            options.setRunning( false );
            cancel( true );
            return -1L;
        }

        // Remove references
        clearRefs( craftBukkitGit, craftBukkit );
        clearRefs( buildGit, buildData );
        spigot.clearReference();
        bukkit.clearReference();
        craftBukkit.clearReference();
        buildData.clearReference();

        for ( int i = 0; i < 5; i++ ) {
            LogHandler.info( " " );
        }

        String version = versionInfo.getMinecraftVersion();
        if ( !options.isSkipCompile() ) {
            projectPool.submit( () -> {
                LogHandler.info( "Success! Everything compiled successfully. Copying final .jar files now." );
                LogHandler.debug( "OutputDirectories: " + options.getOutputDirectories() );
                File bukkitSourceDir = new File( dirs.getWorkingDir().getFile(), "Bukkit/target" );
                File craftSourceDir = new File( dirs.getWorkingDir().getFile(), "CraftBukkit/target" );
                File spigotSourceDir = new File( dirs.getWorkingDir().getFile(), "Spigot/Spigot-Server/target" );
                Predicate<String> isValidJar = (str) -> str.contains( version ) && str.endsWith( ".jar" );
                for ( String outputDir : options.getOutputDirectories() ) {
                    try {
                        FileUtil.copyJar( craftSourceDir, new File( outputDir ), "craftbukkit-" + version + ".jar", isValidJar );
                        FileUtil.copyJar( spigotSourceDir, new File( outputDir ), "spigot-" + version + ".jar", isValidJar );

                        if ( options.isGenSource() ) {
                            FileUtil.copyJar( bukkitSourceDir, new File( outputDir ), "bukkit-" + version + "-sources.jar", (str) -> isValidJar.test( str ) && str.contains( "-sources" ) );
                        }
                        if ( options.isGenDocumentation() ) {
                            FileUtil.copyJar( bukkitSourceDir, new File( outputDir ), "bukkit-" + version + "-javadoc.jar", (str) -> isValidJar.test( str ) && str.contains( "-javadoc" ) );
                        }

                        if ( options.isGenSource() ) {
                            FileUtil.deleteFilesInDirectory( bukkitSourceDir, (str) -> isValidJar.test( str ) && str.contains( "-sources" ) );
                        }
                        if ( options.isGenDocumentation() ) {
                            FileUtil.deleteFilesInDirectory( bukkitSourceDir, (str) -> isValidJar.test( str ) && str.contains( "-javadoc" ) );
                        }
                        FileUtil.deleteFilesInDirectory( craftSourceDir, (str) -> str.endsWith( ".jar" ) );
                        FileUtil.deleteFilesInDirectory( spigotSourceDir, (str) -> str.endsWith( ".jar" ) );

                    } catch ( IOException e ) {
                        e.printStackTrace();
                        break;

                    } finally { // Remove references so we can delete them later
                        bukkitSourceDir = null;
                        craftSourceDir = null;
                        spigotSourceDir = null;
                    }
                }

                return true;
            } ).get();
        }
        // remove references
        console.setOptionalText( "" );

        watch = watch.stop();
        long seconds = watch.elapsed( TimeUnit.SECONDS );
        projectPool.getService().shutdown();
        return seconds;
    }

    private void printOptions(BuildTools options) {
        LogHandler.info( "BuildToolsSuite Options: " );
        LogHandler.info( "Disable Certificate Check: " + options.isDisableCertificateCheck() );
        LogHandler.info( "Don't Update: " + options.isDontUpdate() );
        LogHandler.info( "Skip Compile: " + options.isSkipCompile() );
        LogHandler.info( "Generate Sources: " + options.isGenSource() );
        LogHandler.info( "Generate Docs: " + options.isGenDocumentation() );
        LogHandler.info( "Version: " + options.getVersion() );
        for ( String dir : options.getOutputDirectories() ) {
            LogHandler.info( "Output Directory: " + dir );
        }
    }

    private void redirectToConsole(Process process, Console console) {
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );

        try {
            String line = "";
            while ( ( line = reader.readLine() ) != null ) {
                console.setOptionalText( line );
            }
        } catch ( IOException e ) {

        }
    }

    private void clearTaskRefs(Git repo, Directory target, Task task) {
        task.setOnCancelled( (event) -> {
            clearRefs( repo, target );
        } );
        task.setOnFailed( (event) -> {
            clearRefs( repo, target );
        } );
        task.setOnSucceeded( (event) -> {
            clearRefs( repo, target );
        } );

    }

    private void clearRefs(Git repo, Directory target) {
        repo.close();
        target.clearReference();
    }
}
