package net.senmori.btsuite.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import net.senmori.btsuite.log.LoggerStream;
import net.senmori.btsuite.log.TextAreaLogHandler;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ConsoleController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location valueOf the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="consoleTextArea"
    private TextArea consoleTextArea;

    @FXML
    void initialize() {
        assert consoleTextArea != null;
        LogManager.getLogManager().reset(); // remove all handlers
        Logger rootLogger = LogManager.getLogManager().getLogger("");

        rootLogger.addHandler(new TextAreaLogHandler());
        TextAreaLogHandler.setTextArea(consoleTextArea);
        LoggerStream.setOutAndErrToLog();
    }
}
