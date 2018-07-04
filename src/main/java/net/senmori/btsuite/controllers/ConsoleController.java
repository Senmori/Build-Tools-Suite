package net.senmori.btsuite.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.gui.Console;
import net.senmori.btsuite.gui.LoggerStream;
import net.senmori.btsuite.gui.TextAreaAppender;
import net.senmori.btsuite.util.LogHandler;

import java.net.URL;
import java.util.ResourceBundle;

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
        TextAreaAppender.setTextArea(consoleTextArea);
        LoggerStream.setOutAndErrToLog();
        //Main.setConsole(new Console(consoleTextArea));
    }
}
