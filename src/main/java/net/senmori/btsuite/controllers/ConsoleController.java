package net.senmori.btsuite.controllers;


import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.gui.Console;
import net.senmori.btsuite.settings.Settings;
import net.senmori.btsuite.version.Version;
import net.senmori.btsuite.version.VersionImporter;

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

    private void printElements(Map<Version, BuildInfo> map) {
        for(Map.Entry<Version, BuildInfo> entry : map.entrySet()) {

        }
        System.out.println("=-=-=-=-=-=-=-=-=-=");
    }
}
