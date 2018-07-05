package net.senmori.btsuite.task;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.senmori.btsuite.Builder;
import net.senmori.btsuite.Settings;
import net.senmori.btsuite.VersionString;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.util.LogHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

public class SpigotVersionImporter implements Callable<Map<VersionString, BuildInfo>> {
    private static final Gson GSON = new Gson();
    private static final Pattern JSON_PATTERN = Pattern.compile(".json");
    private static final Settings SETTINGS = Builder.getSettings();
    private static final Settings.Directories DIRS = SETTINGS.getDirectories();

    private final String url;

    public SpigotVersionImporter(String url) {
        this.url = url;
    }

    @Override
    public Map<VersionString, BuildInfo> call() throws Exception {
        File versionFile = new File(DIRS.getVersionsDir(), "versions.html");
        if ( !versionFile.exists() ) {
            versionFile.createNewFile();
            versionFile = TaskPools.submit(new FileDownloader(url, versionFile)).get(); // block
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
            File verFile = new File(DIRS.getVersionsDir(), text);
            if ( !verFile.exists() ) {
                verFile.createNewFile();
                verFile = TaskPools.submit(new FileDownloader(versionUrl, verFile)).get(); // block
            }
            JsonReader reader = new JsonReader(new FileReader(verFile));
            BuildInfo buildInfo = GSON.fromJson(reader, BuildInfo.class);
            map.put(version, buildInfo);
        }
        LogHandler.info("Loaded " + map.keySet().size() + " Spigot versions.");
        return map;
    }
}
