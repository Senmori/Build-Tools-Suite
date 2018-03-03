package net.senmori.btsuite.version;

import net.senmori.btsuite.Main;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class VersionImporter {


    public File getVersionFile(String link) {
        File workFile = new File(Main.WORK_DIR, "version.html");
        try {
            Document doc = Jsoup.parse(workFile, StandardCharsets.UTF_8.name(), Main.SETTINGS.versionLink);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
