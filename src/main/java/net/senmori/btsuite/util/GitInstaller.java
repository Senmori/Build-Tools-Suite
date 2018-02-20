package net.senmori.btsuite.util;

import java.io.File;

public class GitInstaller {

    public static boolean isGitInstalled() {
        try {
            ProcessRunner.runProcess(ProcessRunner.CWD, "sh", "-c", "exit");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void install() {
        String gitVersion = "PortableGit-2.15.0-" + ( System.getProperty("os.arch").endsWith("64") ? "64" : "32" ) + "-bit";

        if(!GitInstaller.isGitInstalled() && ProcessRunner.IS_WINDOWS) {
            ProcessRunner.msysDir = new File(gitVersion, "PortableGit");
            System.out.println("*** Could not find PortableGit installation, downloading. ***");

            String gitName = gitVersion + ".7z.exe";

            File gitInstall = new File(gitVersion, gitName);
            gitInstall.getParentFile().mkdirs();

            if(!gitInstall.exists()) {
                try {
                    Downloader.download("https://static.spigotmc.org/git/" + gitName, gitInstall);
                    ProcessRunner.runProcess(gitInstall.getParentFile(), gitInstall.getAbsolutePath(), "-y", "-gm2", "-nr");
                }catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("*** Using downloaded git " + ProcessRunner.msysDir + " ***");
                System.out.println("*** Please note this is a beta feature, so if it does not work please also try a manual install of git from https://git-for-windows.github.io/ ***");
            }
        } else {
            // not windows, we require
            System.exit(1);
        }

    }
}
