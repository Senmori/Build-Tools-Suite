package net.senmori.btsuite.task;

import javafx.concurrent.Task;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.util.ZipUtil;

import java.io.File;
import java.io.IOException;

public class MavenInstaller extends Task<File> {
    @Override
    protected File call() throws Exception {
        if ( isInstalled() ) {
            System.out.println("Maven is installed at " + System.getenv("M2_HOME"));
            Main.MVN_DIR = new File(System.getenv("M2_HOME"));
            return Main.MVN_DIR;
        }

        File maven = new File("apache-maven-3.5.0");

        if ( ! maven.exists() ) {
            System.out.println("Maven does not exist, downloading. Please wait.");

            File mvnTemp = new File("mvn.zip");
            mvnTemp.deleteOnExit();

            try {
                Main.TASK_RUNNER.execute(new FileDownloader(Main.getSettings().getMvnInstallerLink(), mvnTemp));
                ZipUtil.unzip(mvnTemp, new File("."));
                System.out.println(maven.getName() + " installed to " + mvnTemp.getPath());
            } catch ( IOException e ) {
                e.printStackTrace();
                return null;
            }
        }
        Main.MVN_DIR = new File(System.getenv("M2_HOME"));
        return Main.MVN_DIR;
    }

    private boolean isInstalled() {
        String m2Home = System.getenv("M2_HOME");
        return m2Home != null && new File(m2Home).exists();
    }
}
