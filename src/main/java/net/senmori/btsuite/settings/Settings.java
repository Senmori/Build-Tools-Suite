package net.senmori.btsuite.settings;

import net.senmori.btsuite.buildtools.util.GitOptions;
import net.senmori.btsuite.buildtools.util.MavenOptions;

public final class Settings {

    private GitOptions gitOptions = new GitOptions();
    private MavenOptions mavenOptions = new MavenOptions();

    private String versionLink = "https://hub.spigotmc.org/versions/";

    private String gitVersion = "PortableGit-2.15.0-" + ( System.getProperty( "os.arch" ).endsWith( "64" ) ? "64" : "32" ) + "-bit";
    private String gitName = gitVersion + ".7z.exe";
    private String gitInstallerLink = "https://static.spigotmc.org/git/" + gitName; // "https://static.spigotmc.org/git/PortableGit-2.12.0-64-bit.7z.exe

    private String mvnInstallerLink = "https://static.spigotmc.org/maven/apache-maven-3.5.0-bin.zip";

    private String stashRepoLink = "https://hub.spigotmc.org/stash/scm/spigot/"; // append with 'bukkit.git', 'craftbukkit.git', etc.

    public GitOptions getGitOptions() {
        return gitOptions;
    }

    public void setGitOptions(GitOptions options) {
        this.gitOptions = gitOptions;
    }

    public MavenOptions getMavenOptions() {
        return mavenOptions;
    }

    public void setMavenOptions(MavenOptions options) {
        this.mavenOptions = options;
    }

    public String getGitVersion() {
        return gitVersion;
    }

    public void setGitVersion(String gitVersion) {
        this.gitVersion = gitVersion;
    }

    public String getGitName() {
        return gitName;
    }

    public void setGitName(String gitName) {
        this.gitName = gitName;
    }

    public String getGitInstallerLink() {
        return gitInstallerLink;
    }

    public void setGitInstallerLink(String gitInstallerLink) {
        this.gitInstallerLink = gitInstallerLink;
    }

    public String getMvnInstallerLink() {
        return mvnInstallerLink;
    }

    public void setMvnInstallerLink(String mvnInstallerLink) {
        this.mvnInstallerLink = mvnInstallerLink;
    }

    public String getStashRepoLink() {
        return stashRepoLink;
    }

    public void setStashRepoLink(String stashRepoLink) {
        this.stashRepoLink = stashRepoLink;
    }
}
