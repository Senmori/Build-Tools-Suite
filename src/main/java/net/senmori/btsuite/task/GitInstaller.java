package net.senmori.btsuite.task;

import net.senmori.btsuite.Builder;
import net.senmori.btsuite.Settings;
import net.senmori.btsuite.command.CommandHandler;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.SystemChecker;
import net.senmori.btsuite.util.TaskUtil;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class GitInstaller implements Callable<Boolean> {

    private final Settings settings = Builder.getSettings();
    private final Settings.Directories dirs = settings.getDirectories();

    public GitInstaller() {
    }

    public static boolean install() {
        try {
            return TaskPools.submit( () -> new GitInstaller().call() ).get();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
            return false;
        } catch ( ExecutionException e ) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean call() {
        // check for normal git installation
        try {
            LogHandler.debug("Checking for Git install location.");
            CommandHandler.getCommandIssuer().executeCommand( dirs.getWorkingDir(), "sh", "-c", "exit" );
            return true;
        } catch ( Exception e ) {
            LogHandler.info( "Git not found. Trying to install PortableGit" );
        }
        return doInstall();
    }

    private boolean doInstall() {
        try {
            if ( SystemChecker.isWindows() ) {
                File gitExe = new File(dirs.getPortableGitDir(), settings.getGitName());
                File portableGitInstall = new File(dirs.getPortableGitDir(), "PortableGit");

                if ( portableGitInstall.exists() && portableGitInstall.isDirectory() ) {
                    dirs.setPortableGitDir(portableGitInstall);
                    LogHandler.info("Found PortableGit already installed at " + portableGitInstall);
                    return true;
                }

                if ( !gitExe.exists() ) {
                    gitExe.mkdirs();
                    LogHandler.warn("*** Could not find PortableGit executable, downloading. ***");
                    gitExe = TaskUtil.asyncDownloadFile(settings.getGitInstallerLink(), gitExe);
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
                return false; // Invalid Architecture
            }            LogHandler.info("Git installations success!");
            return true;
        } catch ( Exception e ) {
            LogHandler.error("Failed to install git!");
            return false;
        }
    }
}
