package net.senmori.btsuite.task;

import javafx.concurrent.Task;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.api.IProcessRunner;
import net.senmori.btsuite.buildtools.BuildTools;
import net.senmori.btsuite.settings.Settings;
import net.senmori.btsuite.util.ProcessRunner;
import net.senmori.btsuite.util.SystemChecker;

import java.io.File;
import java.util.Arrays;

public class GitInstaller extends Task<GitInstaller.Response> {

    @Override
    protected GitInstaller.Response call() throws Exception {
        try {
            ProcessRunner.runProcess(Main.WORK_DIR, "git", "--version");
            return Response.ALREADY_INSTALLED;
        } catch(Exception e) {
            if(SystemChecker.isWindows()) {
                BuildTools.portableGitDir = new File(Main.WORK_DIR, Settings.gitVersion);
                File gitInstall = new File(BuildTools.portableGitDir, Settings.gitName);

                if(!BuildTools.portableGitDir.isDirectory()) {
                    System.out.println("*** Could not find PortableGit installation, downloading. ***");
                    gitInstall.getParentFile().mkdirs();

                    if (!gitInstall.exists()) {
                        try {
                            Main.TASK_RUNNER.execute(new FileDownloader(Settings.gitInstallerLink, gitInstall));
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            return Response.INSTALLATION_FAILURE;
                        }
                    }
                    System.out.println("Extracting downloaded git");
                    // yes to all, silent, don't run
                    int code = ProcessRunner.runProcess(BuildTools.portableGitDir, gitInstall.getPath(), "-y", "-gm2", "-nr");
                    System.out.println("*** Using downloaded git " + BuildTools.portableGitDir.getPath() + "/" + Arrays.toString(BuildTools.portableGitDir.list()) + " ***");
                    System.out.println("*** Please note this is a beta feature, so if it does not work please also try a manual install of git from https://git-for-windows.github.io/ ***");
                    if(code != 0)
                        return Response.INSTALLATION_FAILURE;
                } else {
                    System.out.print("Using: ");
                    ProcessRunner.runProcess(BuildTools.portableGitDir, "git", "--version");
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
