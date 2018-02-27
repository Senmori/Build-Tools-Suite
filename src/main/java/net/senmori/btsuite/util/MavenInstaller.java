package net.senmori.btsuite.util;

import java.io.File;
import java.io.IOException;

public final class MavenInstaller {

    public static boolean isMavenInstalled() {
        String m2Home = System.getenv("M2_HOME");
        return m2Home != null && new File(m2Home).exists();
    }

    public static void installMaven() {
        if(!isMavenInstalled()) {
            File maven = new File("apache-maven-3.5.0");

            if(!maven.exists()) {

                System.out.println("Maven does not exist, downloading. Please wait.");

                File mvTemp = new File("mvn.zip");
                mvTemp.deleteOnExit();

                try {
                    Downloader.download("https://static.spigotmc.org/maven/apache-maven-3.5.0-bin.zip", mvTemp);
                    ZipUtil.unzip(mvTemp, new File("."), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
