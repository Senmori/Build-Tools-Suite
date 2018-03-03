package net.senmori.btsuite.version;

import com.google.common.collect.Maps;
import javafx.application.Platform;
import javafx.concurrent.Task;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.gui.Console;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;

public class VersionImporter extends Task<Map<Version, BuildInfo>> {

    private final Console console;
    private final String url;
    public VersionImporter(String url, Console console) {
        this.url = url;
        this.console = console;
    }

    @Override
    protected Map<Version, BuildInfo> call() throws Exception {
        Elements links = getVersionDocument(url).getElementsByTag("a");
        Map<Version, BuildInfo> map = Maps.newHashMap();
        for(Element element : links) {
            String text = element.wholeText(); // 1.12.2.json
            String versionText = text.replaceAll(".json", ""); // 1.12.2
            if(!versionText.contains(".")) {
                continue; // ignore any version strings that are not in the X.XX.XX format
            }
        }
        return map;
    }

    private void appendMessage(String message) {
        Platform.runLater(() -> console.appendText(message));
    }

    private Document getVersionDocument(String link) {
        try {
            Document doc = Jsoup.connect(link).timeout(5 * 1000).get();
            return doc;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Elements getLinks(Document doc) {
        return doc.getElementsByTag("a");
    }
}
