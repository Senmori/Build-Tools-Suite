package net.senmori.btsuite.util;

import net.senmori.btsuite.Main;
import net.senmori.btsuite.buildtools.BuildTools;

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
        String gitVersion = Main.SETTINGS.getGitVersion();

        if(!GitInstaller.isGitInstalled()) {
            if(!SystemChecker.isWindows()) {
                System.exit(1); // can't run build tools on non-windows platform yet!
            }
            BuildTools.mysDir = new File(gitVersion, "PortableGit");
            System.out.println("*** Could not find PortableGit installation, downloading. ***");

            String gitName = Main.SETTINGS.getGitName();

            File gitInstall = new File(gitVersion, gitName);
            gitInstall.getParentFile().mkdirs();

            if(!gitInstall.exists()) {
                try {
                    Downloader.download(Main.SETTINGS.getGitInstallerLink(), gitInstall);
                    ProcessRunner.runProcess(gitInstall.getParentFile(), gitInstall.getAbsolutePath(), "-y", "-gm2", "-nr");
                }catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("*** Using downloaded git " + ProcessRunner.PORTABLE_GIT_DIR + " ***");
                System.out.println("*** Please note this is a beta feature, so if it does not work please also try a manual install of git from https://git-for-windows.github.io/ ***");
            }
        }
    }
}
