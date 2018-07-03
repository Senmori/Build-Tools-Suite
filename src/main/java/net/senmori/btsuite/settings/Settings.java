package net.senmori.btsuite.settings;

import lombok.Getter;
import net.senmori.btsuite.buildtools.VersionInfo;

@Getter
public final class Settings {
    public final String versionLink = "https://hub.spigotmc.org/versions/";

    public final String gitVersion = "PortableGit-2.15.0-" + ( System.getProperty("os.arch").endsWith("64") ? "64" : "32" ) + "-bit";
    public final String gitName = gitVersion + ".7z.exe";
    public final String gitInstallerLink = "https://static.spigotmc.org/git/" + gitName; // "https://static.spigotmc.org/git/PortableGit-2.12.0-64-bit.7z.exe

    public final String mvnInstallerLink = "https://static.spigotmc.org/maven/apache-maven-3.5.0-bin.zip";

    public final String stashRepoLink = "https://hub.spigotmc.org/stash/scm/spigot/"; // append with 'bukkit.git', 'craftbukkit.git', etc.

    public final String fallbackServerUrl = "https://s3.amazonaws.com/Minecraft.Download/versions/%1$s/minecraft_server.%1$s.jar";

    public final String defaultVersion = "1.12.2";


    public VersionInfo getDefaultVersionInfo() {
        String defVersion = defaultVersion;
        String bukkitAT = "bukkit-" + defVersion + ".at";
        String bukkitCL = "bukkit-" + defVersion + "-cl.csrg";
        String bukkitPKG = "bukkit-" + defVersion + "-members.csrg";
        String mcHash = " package.srg";
        String decompCmd = null;
        return new VersionInfo(defVersion, bukkitAT, bukkitCL, bukkitPKG, mcHash, decompCmd);
    }
}
