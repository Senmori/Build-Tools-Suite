package net.senmori.btsuite.task;

import javafx.concurrent.Task;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.settings.Settings;
import net.senmori.btsuite.util.ProcessRunner;
import net.senmori.btsuite.util.ZipUtil;

import javax.security.auth.login.FailedLoginException;
import java.io.File;
import java.io.IOException;

public class MavenInstaller extends Task<File> {
    @Override
    protected File call() throws Exception {
        if(isInstalled()) {
            return new File(System.getenv("M2_HOME"));
        }

        File maven = new File("apache-maven-3.5.0");

        if(!maven.exists()) {
            System.out.println("Maven does not exist, downloading. Please wait.");

            File mvnTemp = new File("mvn.zip");
            mvnTemp.deleteOnExit();

            try {
                Main.TASK_RUNNER.execute(new FileDownloader(Main.getSettings().getMvnInstallerLink(), mvnTemp));
                ZipUtil.unzip(mvnTemp, new File("."));
                System.out.println(maven.getName() + " installed to " + mvnTemp.getPath());
            } catch(IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return new File(System.getenv("M2_HOME"));
    }

    private boolean isInstalled() {
        String m2Home = System.getenv("M2_HOME");
        return m2Home != null && new File(m2Home).exists();
    }
}
