package net.senmori.btsuite.task;

import net.senmori.btsuite.Builder;
import net.senmori.btsuite.Settings;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.TaskUtil;
import net.senmori.btsuite.util.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

public class MavenInstaller implements Callable<File> {

    private final Settings settings = Builder.getSettings();
    private final Settings.Directories dirs = settings.getDirectories();

    public MavenInstaller() {

    }

    @Override
    public File call() {
        LogHandler.debug("Checking for Maven install location.");
        if ( isInstalled() ) {
            File mvn = new File(System.getenv("M2_HOME"));
            LogHandler.info("Maven is installed at " + mvn);
            dirs.setMvnDir(mvn);
            return dirs.getMvnDir();
        }

        File maven = new File("apache-maven-3.5.0");

        if ( !maven.exists() ) {
            LogHandler.info("Maven does not exist, downloading. Please wait.");

            File mvnTemp = new File("mvn.zip");
            mvnTemp.deleteOnExit();

            try {
                String url = settings.getMvnInstallerLink();
                mvnTemp = TaskUtil.asyncDownloadFile(url, mvnTemp);
                ZipUtil.unzip(mvnTemp, new File("."));
                LogHandler.info(maven.getName() + " installed to " + mvnTemp.getPath());
                dirs.setMvnDir(mvnTemp);
            } catch ( IOException e ) {
                e.printStackTrace();
                return null;
            }
        }
        return dirs.getMvnDir();
    }

    private boolean isInstalled() {
        String m2Home = System.getenv("M2_HOME");
        return m2Home != null && new File(m2Home).exists();
    }
}
