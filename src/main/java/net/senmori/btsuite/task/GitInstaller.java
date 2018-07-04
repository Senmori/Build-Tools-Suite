package net.senmori.btsuite.task;

import javafx.application.Platform;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.Settings;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.ProcessRunner;
import net.senmori.btsuite.util.SystemChecker;
import net.senmori.btsuite.util.TaskUtil;

import java.io.File;

public class GitInstaller  {

    public static void install() {
        final Settings settings = Main.getSettings();
        final Settings.Directories dirs = settings.getDirectories();
        // check for normall git installation
        try {
            LogHandler.debug("Checking for Git install location.");
            ProcessRunner.runProcess(dirs.getWorkingDir(), "git", "--version");
        } catch ( Exception e ) {
            LogHandler.debug("Git not found. Trying to install PortableGit");
            doInstall();
        }
    }

    private static void doInstall() {
        final Settings settings = Main.getSettings();
        final Settings.Directories dirs = settings.getDirectories();
        try {
            if ( SystemChecker.isWindows() ) {
                File gitExe = new File(dirs.getPortableGitDir(), Main.getSettings().getGitName());
                File portableGitInstall = new File(dirs.getPortableGitDir(), "PortableGit");

                if ( portableGitInstall.exists() && portableGitInstall.isDirectory() ) {
                    dirs.setPortableGitDir(portableGitInstall);
                    LogHandler.info("Found PortableGit already installed at " + portableGitInstall);
                    return;
                }

                if ( !gitExe.exists() ) {
                    gitExe.mkdirs();
                    LogHandler.warn("*** Could not find PortableGit executable, downloading. ***");
                    gitExe = TaskUtil.asyncDownloadFile(Main.newChain(), settings.getGitInstallerLink(), gitExe);
                }
                if ( !FileUtil.isDirectory(portableGitInstall) ) {
                    portableGitInstall.mkdirs();
                    // yes to all, silent, don't install.  Only -y seems to work
                    // ProcessRunner appends information we don't need
                    Runtime.getRuntime().exec(gitExe.getPath(), new String[] {"-y", "-gm2", "-nr"}, gitExe.getParentFile());

                    LogHandler.warn("*** Please note this is a beta feature, so if it does not work please also try a manual install valueOf git from https://git-for-windows.github.io/ ***");
                    dirs.setPortableGitDir(portableGitInstall);
                    LogHandler.info("Successfully installed PortableGit to " + dirs.getPortableGitDir());
                }
            } else { // end if windows check
                LogHandler.error("Invalid Architecture!");
                return; // Invalid Architecture
            }            LogHandler.info("Git installations success!");
            return;
        } catch ( Exception e ) {
            LogHandler.error("Failed to install git!");
            Platform.exit();
            return;
        }
    }
}
