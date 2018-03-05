package net.senmori.btsuite.task;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.concurrent.Task;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.gui.Console;
import net.senmori.btsuite.settings.Settings;
import net.senmori.btsuite.util.Downloader;
import net.senmori.btsuite.version.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;

public class VersionImporter extends Task<Map<Version, BuildInfo>> {
    private static final Gson GSON = new Gson();

    private final String url;
    public VersionImporter(String url) {
        this.url = url;
    }

    @Override
    protected Map<Version, BuildInfo> call() throws Exception {
        Elements links = getVersionDocument(url).getElementsByTag("a");
        Map<Version, BuildInfo> map = Maps.newHashMap();
        for(Element element : links) {
            if(element.wholeText().startsWith("..")) // ignore non-version links
                continue;
            String text = element.wholeText(); // 1.12.2.json
            String versionText = text.replaceAll(".json", ""); // 1.12.2
            if(!versionText.contains(".")) //TODO: add filters so we can include the obscure versions
                continue;
            Version version = new Version(versionText);
            String json = Downloader.get(url + text);
            BuildInfo buildInfo = GSON.fromJson(json, BuildInfo.class);
            map.put(version, buildInfo);
            System.out.println("Imported version \'" + versionText + "\'.");
        }
        return map;
    }

    private Document getVersionDocument(String link) {
        try {
            return Jsoup.connect(link).timeout(5 * 1000).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
