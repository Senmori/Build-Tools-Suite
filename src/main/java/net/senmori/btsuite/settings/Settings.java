package net.senmori.btsuite.settings;

import net.senmori.btsuite.buildtools.util.GitOptions;
import net.senmori.btsuite.buildtools.util.MavenOptions;

public final class Settings {

    public GitOptions gitOptions = new GitOptions();
    public MavenOptions mavenOptions = new MavenOptions();

    public final String versionLink = "https://hub.spigotmc.org/versions/";

    public final String gitVersion = "PortableGit-2.15.0-" + ( System.getProperty( "os.arch" ).endsWith( "64" ) ? "64" : "32" ) + "-bit";
    public final String gitName = gitVersion + ".7z.exe";
    public final String gitInstallerLink = "https://static.spigotmc.org/git/" + gitName; // "https://static.spigotmc.org/git/PortableGit-2.12.0-64-bit.7z.exe

    public final String mvnInstallerLink = "https://static.spigotmc.org/maven/apache-maven-3.5.0-bin.zip";

    public final String stashRepoLink = "https://hub.spigotmc.org/stash/scm/spigot/"; // append with 'bukkit.git', 'craftbukkit.git', etc.
}
