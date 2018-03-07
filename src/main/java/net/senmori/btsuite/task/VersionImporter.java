package net.senmori.btsuite.task;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import javafx.application.Platform;
import javafx.concurrent.Task;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.gui.Console;
import net.senmori.btsuite.settings.Settings;
import net.senmori.btsuite.util.Downloader;
import net.senmori.btsuite.version.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class VersionImporter extends Task<Map<Version, BuildInfo>> {
    private static final Gson GSON = new Gson();

    private final String url;
    private final ExecutorService threadPool;
    public VersionImporter(String url, ExecutorService pool) {
        this.url = url;
        this.threadPool = pool;
    }

    @Override
    protected Map<Version, BuildInfo> call() throws Exception {
        File versionFile = new File(Main.TMP_DIR, "versions.html");
        if(!versionFile.exists()) {
            versionFile.createNewFile();
            Future<File> future = threadPool.submit(new FileDownloader(url, versionFile), versionFile);
            if(future.isDone()) {
                versionFile = future.get();
            }
        }
        Elements links = Jsoup.parse(versionFile, StandardCharsets.UTF_8.name()).getElementsByTag("a");
        Map<Version, BuildInfo> map = Maps.newHashMap();
        for(Element element : links) {
            if(element.wholeText().startsWith("..")) // ignore non-version links
                continue;
            String text = element.wholeText(); // 1.12.2.json
            String versionText = text.replaceAll(".json", ""); // 1.12.2
            if(!Version.isVersionNumber(versionText))
                continue;
            Version version = new Version(versionText);
            String versionUrl = url + text; // .../versions/1.12.2.json
            File verFile = new File(Main.TMP_DIR, text);
            if(!verFile.exists()) {
                verFile.createNewFile();
                Future<File> future = threadPool.submit(new FileDownloader(versionUrl, versionFile), versionFile);
                if(future.isDone()) {
                    versionFile = future.get();
                }
            }
            JsonReader reader = new JsonReader(new FileReader(verFile));
            BuildInfo buildInfo = GSON.fromJson(reader, BuildInfo.class);
            map.put(version, buildInfo);
        }
        return map;
    }
}