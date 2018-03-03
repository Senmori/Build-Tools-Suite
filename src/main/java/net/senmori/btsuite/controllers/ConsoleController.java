/**
 * Sample Skeleton for 'console.fxml' Controller Class
 */

package net.senmori.btsuite.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.collect.Lists;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import net.senmori.btsuite.gui.Console;
import net.senmori.btsuite.settings.Settings;
import net.senmori.btsuite.version.Version;
import net.senmori.btsuite.version.VersionImporter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ConsoleController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="consoleTextArea"
    private TextArea consoleTextArea;

    private Console console;

    @FXML
    void initialize() {
        assert consoleTextArea != null : "fx:id=\"consoleTextArea\" was not injected: check your FXML file 'console.fxml'.";

        console = new Console(consoleTextArea);

        VersionImporter task = new VersionImporter(Settings.versionLink, console);
        task.setOnSucceeded((handler) -> printElements(task.getValue()));
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(task);

        pool.shutdown();
    }

    private void printElements(Elements elements) {
        List<Version> allElements = Lists.newArrayList();
        for(Element element : elements) {
            String text = element.wholeText();
            try {
                Version ver = new Version(text.replaceAll(".json", ""));
                if(!ver.getVersionString().contains(".")) {
                    continue;
                }
                allElements.add(ver);
                console.appendText("Parsed " + ver.getVersionString() + " version!\n");
            } catch(IllegalArgumentException e) {
                continue;
            }
        }
        System.out.println("=-=-=-=-=-=-=-=-=-=");
    }
}
