package net.senmori.btsuite.task;

import net.senmori.btsuite.Main;
import net.senmori.btsuite.Settings;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.TaskUtil;
import net.senmori.btsuite.util.ZipUtil;

import java.io.File;
import java.io.IOException;

public class MavenInstaller {
    public static void install() {
        final Settings settings = Main.getSettings();
        final Settings.Directories dirs = settings.getDirectories();

        LogHandler.debug("Checking for Maven install location.");
        if ( isInstalled() ) {
            File mvn = new File(System.getenv("M2_HOME"));
            LogHandler.info("Maven is installed at " + mvn);
            dirs.setMvnDir(mvn);
            return;
        }

        File maven = new File("apache-maven-3.5.0");

        if ( !maven.exists() ) {
            LogHandler.info("Maven does not exist, downloading. Please wait.");

            File mvnTemp = new File("mvn.zip");
            mvnTemp.deleteOnExit();

            try {
                String url = settings.getMvnInstallerLink();
                mvnTemp = TaskUtil.asyncDownloadFile(Main.newChain(), url, mvnTemp);
                ZipUtil.unzip(mvnTemp, new File("."));
                LogHandler.info(maven.getName() + " installed to " + mvnTemp.getPath());
                dirs.setMvnDir(mvnTemp);
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isInstalled() {
        String m2Home = System.getenv("M2_HOME");
        return m2Home != null && new File(m2Home).exists();
    }
}
