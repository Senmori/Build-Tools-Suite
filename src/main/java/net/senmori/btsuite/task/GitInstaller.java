package net.senmori.btsuite.task;

import javafx.concurrent.Task;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.settings.Settings;
import net.senmori.btsuite.util.ProcessRunner;
import net.senmori.btsuite.util.SystemChecker;

import java.io.File;

public class GitInstaller extends Task<GitInstaller.Response> {

    @Override
    protected GitInstaller.Response call() throws Exception {
        try {
            ProcessRunner.runProcess("git", "--version");
            return Response.ALREADY_INSTALLED;
        } catch(Exception e) {
            if(SystemChecker.isWindows()) {
                File gitInstall = new File(Main.PORTABLE_GIT_DIR, Main.getSettings().getGitName());

                if(!Main.PORTABLE_GIT_DIR.isDirectory()) {
                    System.out.println("*** Could not find PortableGit installation, downloading. ***");
                    gitInstall.getParentFile().mkdirs();

                    if (!gitInstall.exists()) {
                        try {
                            Main.TASK_RUNNER.execute(new FileDownloader(Main.getSettings().getGitInstallerLink(), gitInstall));
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            return Response.INSTALLATION_FAILURE;
                        }
                    }
                    System.out.println("Extracting downloaded git");
                                                                                                                   // yes to all, silent, don't run
                    int code = ProcessRunner.runProcess(gitInstall.getParentFile(), gitInstall.getPath(), "-y", "-gm2", "-nr");
                    System.out.println("*** Please note this is a beta feature, so if it does not work please also try a manual install of git from https://git-for-windows.github.io/ ***");
                    if(code != 0)
                        return Response.INSTALLATION_FAILURE;
                } else {
                    ProcessRunner.runProcess("git", "--version");
                }
            } else {
                return Response.INVALID_ARCHITECTURE;
            }
        }
        return Response.INSTALLATION_SUCCESS;
    }

    public enum Response {
        INVALID_ARCHITECTURE,
        ALREADY_INSTALLED,
        INSTALLATION_FAILURE,
        INSTALLATION_SUCCESS;
    }
}
