package net.senmori.btsuite.task;

import javafx.concurrent.Task;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.ProcessRunner;
import net.senmori.btsuite.util.SystemChecker;

import java.io.File;
import java.util.concurrent.Future;

public class GitInstaller extends Task<GitInstaller.Response> {

    @Override
    protected GitInstaller.Response call() throws Exception {
        // check for normall git installation
        try {
            ProcessRunner.runProcess(Main.WORK_DIR, "git", "--version");
            return Response.ALREADY_INSTALLED;
        } catch ( Exception e ) {
        }
        return doInstall();
    }

    private Response doInstall() {
        try {
            if ( SystemChecker.isWindows() ) {
                File gitExe = new File(Main.PORTABLE_GIT_DIR, Main.getSettings().getGitName());
                File portableGitInstall = new File(Main.PORTABLE_GIT_DIR, "PortableGit");

                if ( portableGitInstall.exists() && portableGitInstall.isDirectory() ) {
                    Main.PORTABLE_GIT_DIR = portableGitInstall;
                    System.out.println("Found PortableGit already installed at " + portableGitInstall);
                    return Response.ALREADY_INSTALLED;
                }

                if ( ! gitExe.exists() ) {
                    gitExe.mkdirs();
                    System.out.println("*** Could not find PortableGit executable, downloading. ***");
                    try {
                        Future<File> task = Main.TASK_RUNNER.submit(new FileDownloader(Main.getSettings().getGitInstallerLink(), gitExe));
                        do {

                        } while ( ! task.isDone() );
                        gitExe = task.get();
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        return Response.INSTALLATION_FAILURE;
                    }
                }
                if ( ! FileUtil.isDirectory(portableGitInstall) ) {
                    portableGitInstall.mkdirs();
                    // yes to all, silent, don't run.  Only -y seems to work
                    Runtime.getRuntime().exec(gitExe.getPath(), new String[] {"-y", "-gm2", "-nr"}, gitExe.getParentFile());

                    System.out.println("*** Please note this is a beta feature, so if it does not work please also try a manual install of git from https://git-for-windows.github.io/ ***");
                    Main.PORTABLE_GIT_DIR = portableGitInstall;
                    gitExe.delete();
                    System.out.println("Successfully installed PortableGit to " + Main.PORTABLE_GIT_DIR);
                }
            } else { // end if windows check
                return GitInstaller.Response.INVALID_ARCHITECTURE;
            }
            return Response.INSTALLATION_SUCCESS;
        } catch ( Exception e ) {
            return Response.INSTALLATION_FAILURE;
        }
    }

    public enum Response {
        INVALID_ARCHITECTURE,
        ALREADY_INSTALLED,
        INSTALLATION_FAILURE,
        INSTALLATION_SUCCESS;
    }
}
