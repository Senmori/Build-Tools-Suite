package net.senmori.btsuite.task;

import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.Directory;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.TaskUtil;
import net.senmori.btsuite.util.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class MavenInstaller implements Callable<File> {

    private final BuildToolsSettings buildToolsSettings = BuildToolsSettings.getInstance();
    private final BuildToolsSettings.Directories dirs = buildToolsSettings.getDirectories();

    public MavenInstaller() {

    }

    public static boolean install() {
        try {
            return TaskPools.submit( () -> new MavenInstaller().call() ).get() != null;
        } catch ( InterruptedException e ) {
            e.printStackTrace();
            return false;
        } catch ( ExecutionException e ) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public File call() {
        LogHandler.debug("Checking for Maven install location.");
        if ( isInstalled() ) {
            File mvn = new File(System.getenv("M2_HOME"));
            LogHandler.info("Maven is installed at " + mvn);
            dirs.setMvnDir( new Directory( mvn.getParent(), mvn.getName() ) );
            return dirs.getMvnDir().getFile();
        }

        File maven = new File("apache-maven-3.5.0");

        if ( !maven.exists() ) {
            LogHandler.info("Maven does not exist, downloading. Please wait.");

            File mvnTemp = new File("mvn.zip");
            mvnTemp.deleteOnExit();

            try {
                String url = buildToolsSettings.getMvnInstallerLink();
                mvnTemp = TaskUtil.asyncDownloadFile(url, mvnTemp);
                ZipUtil.unzip(mvnTemp, new File("."));
                LogHandler.info(maven.getName() + " installed to " + mvnTemp.getPath());
                dirs.setMvnDir( new Directory( mvnTemp.getParent(), mvnTemp.getName() ) );
            } catch ( IOException e ) {
                e.printStackTrace();
                return null;
            }
        }
        return dirs.getMvnDir().getFile();
    }

    private boolean isInstalled() {
        String m2Home = System.getenv("M2_HOME");
        return m2Home != null && new File(m2Home).exists();
    }
}
