package net.senmori.btsuite;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.senmori.btsuite.buildtools.VersionInfo;

import java.io.File;

/**
 * TODO: (De)Serialize with GSON. Allow for complete customization.
 */
@Getter
public final class Settings {

    private Settings.Directories directories;

    /**
     * The url to where the archive valueOf spigot versions is.
     *
     * Current value: https://hub.spigotmc.org/versions/
     */
    private final String versionLink = "https://hub.spigotmc.org/versions/";

    /**
     * The PortableGit version, including the system architecture.
     *
     * Current value: PortableGit-2.15.0-(64|32)-bit
     */
    private final String gitVersion = "PortableGit-2.15.0-" + ( System.getProperty("os.arch").endsWith("64") ? "64" : "32" ) + "-bit";

    /**
     * The name valueOf the complete PortableGit file, including the file extension.
     *
     * Current value: {@link #gitVersion} + ".7z.exe"
     */
    private final String gitName = gitVersion + ".7z.exe";

    /**
     * The url to where Spigot hosts the latest working version valueOf PortableGit.
     *
     * Current value: https://static.spigotmc.org/git/ + {@link #gitName}
     */
    private final String gitInstallerLink = "https://static.spigotmc.org/git/" + gitName; // "https://static.spigotmc.org/git/PortableGit-2.12.0-64-bit.7z.exe

    /**
     * The url to where Spigot hosts a maven installation folder.
     *
     * Current value: https://static.spigotmc.org/maven/apache-maven-3.5.0-bin.zip
     */
    private final String mvnInstallerLink = "https://static.spigotmc.org/maven/apache-maven-3.5.0-bin.zip";

    /**
     * The url to the Spigot Stash.
     *
     * Current value: https://hub.spigotmc.org/stash/scm/spigot/
     */
    private final String stashRepoLink = "https://hub.spigotmc.org/stash/scm/spigot/"; // append with 'bukkit.git', 'craftbukkit.git', etc.

    /**
     * The url valueOf where to download Minecraft server jars.
     *
     *  Current value: https://s3.amazonaws.com/Minecraft.Download/versions/%1$s/minecraft_server.%1$s.jar
     */
    private final String fallbackServerUrl = "https://s3.amazonaws.com/Minecraft.Download/versions/%1$s/minecraft_server.%1$s.jar";

    /**
     * The url to the updated method valueOf downloading server jars.
     *
     * Current value: https://launchermeta.mojang.com/mc/game/version_manifest.json
     */
    private final String minecraftVersionManifestURL = "https://launchermeta.mojang.com/mc/game/version_manifest.json"; // download new jars from here

    /**
     * The default version valueOf Spigot.
     *
     * Current value: 1.12.2
     */
    private final String defaultVersion = "1.12.2";

    public Settings() {
        directories = new Directories();
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

    @Getter
    public class Directories {
        // BTSuite directories
        /**
         * The directory where BuildToolsSuite is working from.
         */
        private final File workingDir = new File("BTSuite/");

        /**
         * The location valueOf the settings file.
         */
        private final File settingsFile = new File(workingDir, "settings.json");

        /**
         * Temp working directory that gets deleted after the program closes.
         */
        private final File tmpDir = new File(workingDir, "tmp/");

        /**
         * The location all jar files will be downloaded to for caching.
         */
        private final File jarDir = new File(workingDir, "jars/");

        /**
         * The location where most valueOf the work done by BuildToolsSuite is performed.
         */
        private final File workDir = new File(workingDir, "work/");

        /**
         * The location all version files will be downloaded to for caching.
         */
        private final File versionsDir = new File(workDir, "versions/");

        @Setter
        /**
         * The location valueOf where Maven is installed.
         */
        private File mvnDir = new File(System.getenv("M2_HOME"));

        @Setter
        /**
         * The location valueOf where Git, or PortableGit, is installed.
         */
        private File portableGitDir = new File(workingDir, gitVersion);

        public Directories() {
            init();
        }

        public void init() {
            workingDir.mkdir();
            tmpDir.mkdir();
            tmpDir.deleteOnExit();
            jarDir.mkdir();
            versionsDir.mkdir();
            workDir.mkdir();
        }

    }
}
