/**
 * Sample Skeleton for 'console.fxml' Controller Class
 */

package net.senmori.btsuite.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import net.senmori.btsuite.gui.Console;

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
    }
}
