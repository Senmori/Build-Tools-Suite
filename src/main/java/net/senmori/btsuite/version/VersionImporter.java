package net.senmori.btsuite.version;

import javafx.application.Platform;
import javafx.concurrent.Task;
import net.senmori.btsuite.gui.Console;
import net.senmori.btsuite.settings.Settings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class VersionImporter extends Task<Elements> {

    private final Console console;
    private final String url;
    public VersionImporter(String url, Console console) {
        this.url = url;
        this.console = console;
    }

    @Override
    public Elements call() {
        Document doc = getVersionDocument(url);
        return getVersionDocument(url).getElementsByTag("a");
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
