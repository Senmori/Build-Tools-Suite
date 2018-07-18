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

package net.senmori.btsuite.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.buildtools.VersionInfo;
import net.senmori.btsuite.storage.annotations.Exclude;
import net.senmori.btsuite.storage.annotations.Section;
import net.senmori.btsuite.storage.annotations.SerializedValue;
import net.senmori.btsuite.util.FileUtil;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * The settings files contains the required links to tools required by Build Tools.
 * As well, it will store the location of directories used by BTS.
 */
@Getter
public final class BuildToolsSettings {
    private static BuildToolsSettings INSTANCE = null;

    public static BuildToolsSettings getInstance() {
        if ( INSTANCE == null ) {
            throw new IllegalStateException( "Cannot retrieve BuildToolsSettings before it has been created!" );
        }
        return INSTANCE;
    }

    public static BuildToolsSettings create() {
        return create( Maps.newHashMap(), Maps.newHashMap(), Lists.newLinkedList() );
    }

    public static BuildToolsSettings create(Map<String, String> map, Map<String, Directory> directories, List<String> recentOutputDirectories) {
        if ( INSTANCE == null ) {
            INSTANCE = new BuildToolsSettings( map, directories, recentOutputDirectories );
        }
        return INSTANCE;
    }

    @Section( SectionKey.DIRECTORIES )
    private BuildToolsSettings.Directories directories;

    /**
     * The url to where the archive of spigot versions is.
     *
     * Current value: https://hub.spigotmc.org/versions/
     */
    @Section( SectionKey.URL )
    @SerializedName( ConfigurationKey.Name.SPIGOT_VERSION_LINK )
    private final String versionLink;


    /**
     * The url to where Spigot hosts the latest working version valueOf PortableGit.
     * <br>
     * Current value: https://static.spigotmc.org/git/PortableGit-{0}-{1}-bit.7z.exe
     */
    @Section( SectionKey.URL )
    @SerializedName( ConfigurationKey.Name.GIT_INSTALLER_LINK )
    @SerializedValue(ConfigurationKey.Value.DEFAULT_GIT_INSTALLER_LINK)
    private final String gitInstallerLink;

    /**
     * The url to where Spigot hosts a maven installation folder.
     * <br>
     * Current value: https://static.spigotmc.org/maven/apache-maven-{0}-bin.zip
     */
    @Section( SectionKey.URL )
    @SerializedName( ConfigurationKey.Name.MAVEN_INSTALLER_LINK )
    @SerializedValue( ConfigurationKey.Value.DEFAULT_MAVEN_INSTALLER_LINK )
    private final String mvnInstallerLink;

    /**
     * The url to the Spigot Stash.
     * <br>
     * Current value: https://hub.spigotmc.org/stash/scm/spigot/
     */
    @Section( SectionKey.URL )
    @SerializedName( ConfigurationKey.Name.STASH_REPO_LINK )
    private final String stashRepoLink; // append with 'bukkit.git', 'craftbukkit.git', etc.

    /**
     * The url to the updated method of downloading server jars.
     * <br>
     * Current value: https://launchermeta.mojang.com/mc/game/version_manifest.json
     */
    @Section( SectionKey.URL )
    @SerializedName( ConfigurationKey.Name.MC_JAR_DOWNLOAD_LINK )
    private final String minecraftVersionManifestURL;

    /**
     * The PortableGit version
     * <br>
     * Current value: 2.15.0
     */
    @Section( SectionKey.VERSIONS )
    @SerializedName( ConfigurationKey.Name.PORTABLE_GIT_VERSION )
    private final String gitVersion;

    /**
     * The default version of Spigot.
     * <br>
     * Current value: 1.12.2
     */
    @Section( SectionKey.VERSIONS )
    @SerializedName( ConfigurationKey.Name.DEFAULT_SPIGOT_VERSION )
    private final String defaultVersion;

    /**
     * The default version of maven that BuildToolsSuite uses.
     * <br>
     * Current value: 3.5.0
     */
    @Section( SectionKey.VERSIONS )
    @SerializedName( ConfigurationKey.Name.MAVEN_VERSION )
    private final String mavenVersion;

    /**
     * The most recently used output directories.<br>
     * The output will automatically be put in {@link Main#WORKING_DIR} if no other options exist.
     */
    @Section( SectionKey.OUTPUT_DIRS )
    private final List<String> recentOutputDirectories = Lists.newLinkedList();

    public BuildToolsSettings() {
        this( Maps.newHashMap(), Maps.newHashMap(), Lists.newLinkedList() );
    }

    public BuildToolsSettings(Map<String, String> map, Map<String, Directory> directories, List<String> recentOutputDirectories) {
        this.directories = new Directories( directories );

        // versions
        this.defaultVersion = map.getOrDefault( ConfigurationKey.Name.DEFAULT_SPIGOT_VERSION, "1.12.2" );
        this.mavenVersion = map.getOrDefault( ConfigurationKey.Name.MAVEN_VERSION, "3.5.0" );
        // git
        this.gitVersion = map.getOrDefault( ConfigurationKey.Name.PORTABLE_GIT_VERSION, "2.15.0" );
        this.recentOutputDirectories.addAll( recentOutputDirectories );

        // urls
        String formatMvn = map.getOrDefault( ConfigurationKey.Name.MAVEN_INSTALLER_LINK, "https://static.spigotmc.org/maven/apache-maven-{0}-bin.zip" );
        this.mvnInstallerLink = MessageFormat.format( formatMvn, this.mavenVersion );

        String formatGit = map.getOrDefault( ConfigurationKey.Name.GIT_INSTALLER_LINK, "https://static.spigotmc.org/git/PortableGit-{0}-{1}-bit.7z.exe" );
        String arch = ( System.getProperty( "os.arch" ).endsWith( "64" ) ? "64" : "32" );
        this.gitInstallerLink = MessageFormat.format( formatGit, gitVersion, arch );

        this.versionLink = map.getOrDefault( ConfigurationKey.Name.SPIGOT_VERSION_LINK, "https://hub.spigotmc.org/versions/" );
        this.stashRepoLink = map.getOrDefault( ConfigurationKey.Name.STASH_REPO_LINK, "https://hub.spigotmc.org/stash/scm/spigot/" );
        //this.s3DownloadLink = map.getOrDefault( "s3DownloadLink", "https://s3.amazonaws.com/Minecraft.Download/versions/%1$s/minecraft_server.%1$s.jar" );
        this.minecraftVersionManifestURL = map.getOrDefault( ConfigurationKey.Name.MC_JAR_DOWNLOAD_LINK, "https://launchermeta.mojang.com/mc/game/version_manifest.json" );
    }


    /**
     * Get the default {@link VersionInfo} if none were found elsewhere.
     * This will not set the decompile command.
     * @return a new instance valueOf {@link VersionInfo}
     */
    public VersionInfo getBukkitVersionInfo(String version) {
        String bukkitAT = "bukkit-" + version + ".at";
        String bukkitCL = "bukkit-" + version + "-cl.csrg";
        String bukkitPKG = "bukkit-" + version + "-members.csrg";
        String mcHash = " package.srg";
        String decompCmd = null;
        return new VersionInfo(version, bukkitAT, bukkitCL, bukkitPKG, mcHash, decompCmd);
    }

    /**
     * Inner class that holds all directories related to the BuildToolsSuite program.
     */
    @Getter
    public static class Directories {
        /**
         * The directory where BuildToolsSuite is working from.
         * Cannot be changed.
         */
        @Setter( AccessLevel.NONE )
        @Exclude
        private final Directory workingDir = Main.WORKING_DIR;

        /**
         * The location where most valueOf the work done by BuildToolsSuite is performed.
         */
        @SerializedName( ConfigurationKey.Name.WORK_DIR )
        private final Directory workDir;

        /**
         * Temp working directory that gets deleted after the program closes.
         */
        @SerializedName( ConfigurationKey.Name.TEMP_DIR )
        private final Directory tmpDir;


        /**
         * The location all version files will be downloaded to for caching.
         */
        @SerializedName( ConfigurationKey.Name.VERSIONS_DIR )
        private final Directory versionsDir;

        /**
         * The location all jar files will be downloaded to for caching.
         */
        @SerializedName( ConfigurationKey.Name.JAR_DIR )
        private final Directory jarDir;

        //
        //  All directories below here can be set to a different value, although I don't recommend it ;)
        //

        /**
         * The location valueOf where Maven is installed.
         */
        @Setter
        @SerializedName( ConfigurationKey.Name.MAVEN_DIR )
        private Directory mvnDir;

        /**
         * The location valueOf where Git, or PortableGit, is installed.
         */
        @Setter
        @SerializedName( ConfigurationKey.Name.PORTABLE_GIT_DIR )
        private Directory portableGitDir;

        private Directories(Map<String, Directory> map) {
            workDir = map.getOrDefault( ConfigurationKey.Name.WORK_DIR, new Directory( workingDir, "work" ) );
            tmpDir = map.getOrDefault( ConfigurationKey.Name.TEMP_DIR, new Directory( workingDir, "tmp" ) );
            versionsDir = map.getOrDefault( ConfigurationKey.Name.VERSIONS_DIR, new Directory( workDir, "versions" ) );
            jarDir = map.getOrDefault( ConfigurationKey.Name.JAR_DIR, new Directory( workingDir, "jars" ) );
            mvnDir = map.getOrDefault( ConfigurationKey.Name.MAVEN_DIR, new Directory( workingDir, "maven" ) );
            portableGitDir = map.getOrDefault( ConfigurationKey.Name.PORTABLE_GIT_DIR, new Directory( workingDir, "PortableGit" ) );
            init();
        }

        public void init() {
            workingDir.getFile().mkdir(); // Required & immutable.

            workDir.getFile().mkdir();

            if ( tmpDir.getFile().exists() ) {
                FileUtil.deleteDirectory( tmpDir.getFile() );
            }
            tmpDir.getFile().mkdir();
            tmpDir.getFile().deleteOnExit();

            versionsDir.getFile().mkdir();
            jarDir.getFile().mkdir();
        }

    }
}
