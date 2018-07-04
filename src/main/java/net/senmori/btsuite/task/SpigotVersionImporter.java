package net.senmori.btsuite.task;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.Settings;
import net.senmori.btsuite.VersionString;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.util.FileDownloader;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.LogHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

public class SpigotVersionImporter  {
    private static final Gson GSON = new Gson();
    private static final Pattern JSON_PATTERN = Pattern.compile(".json");

    public static Map<VersionString, BuildInfo> getVersions(String url) throws Exception {
        final Settings.Directories dirs = Main.getSettings().getDirectories();
        File versionFile = new File(dirs.getVersionsDir(), "versions.html");
        if ( !versionFile.exists() ) {
            versionFile.createNewFile();
            versionFile = FileDownloader.downloadWrapped(url, versionFile);
            LogHandler.debug(" Downloaded " + versionFile);
        }
        Elements links = Jsoup.parse(versionFile, StandardCharsets.UTF_8.name()).getElementsByTag("a");
        Map<VersionString, BuildInfo> map = Maps.newHashMap();
        for ( Element element : links ) {
            if ( element.wholeText().startsWith("..") ) // ignore non-version links
                continue;
            String text = element.wholeText(); // 1.12.2.json
            String versionText = JSON_PATTERN.matcher(text).replaceAll(""); // 1.12.2
            if ( !VersionString.isVersionNumber(versionText) ) {
                continue;
            }
            VersionString version = VersionString.valueOf(versionText);
            String versionUrl = url + text; // ../work/versions/1.12.2.json
            File verFile = new File(dirs.getVersionsDir(), text);
            if ( !verFile.exists() ) {
                verFile.createNewFile();
                verFile = FileDownloader.downloadWrapped(versionUrl, verFile);
            }
            JsonReader reader = new JsonReader(new FileReader(verFile));
            BuildInfo buildInfo = GSON.fromJson(reader, BuildInfo.class);
            map.put(version, buildInfo);
        }
        LogHandler.info("Loaded " + map.keySet().size() + " Spigot versions.");
        return map;
    }

    private void createDir(File file) {
        if ( ! FileUtil.isDirectory(file) ) {
            file.mkdir();
        }
    }

    private void waitForFuture(Future<?> future) {
        do {
            ;
        } while ( ! future.isDone() );
    }
}
