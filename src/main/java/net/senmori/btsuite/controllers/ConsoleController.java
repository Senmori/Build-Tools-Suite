package net.senmori.btsuite.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.gui.Console;

import java.net.URL;
import java.util.ResourceBundle;

public class ConsoleController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="consoleTextArea"
    private TextArea consoleTextArea;

    @FXML
    void initialize() {
        assert consoleTextArea != null : "fx:id=\"consoleTextArea\" was not injected: check your FXML file 'console.fxml'.";

        Main.setConsole(new Console(consoleTextArea));
    }
}
