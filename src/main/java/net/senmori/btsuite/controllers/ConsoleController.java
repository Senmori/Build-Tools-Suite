package net.senmori.btsuite.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import net.senmori.btsuite.gui.BuildToolsConsole;
import net.senmori.btsuite.log.LoggerStream;
import net.senmori.btsuite.log.TextAreaLogHandler;
import net.senmori.btsuite.task.GitConfigurationTask;
import net.senmori.btsuite.task.GitInstaller;
import net.senmori.btsuite.task.MavenInstaller;
import net.senmori.btsuite.util.LogHandler;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ConsoleController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
    private TextArea consoleTextArea;

    @FXML
    void initialize() {
        assert consoleTextArea != null;
        LogManager.getLogManager().reset(); // remove all handlers
        Logger rootLogger = LogManager.getLogManager().getLogger("");

        TextAreaLogHandler.setConsole( new BuildToolsConsole( consoleTextArea ) );
        rootLogger.addHandler(new TextAreaLogHandler());
        LoggerStream.setOutAndErrToLog();

        boolean git = GitInstaller.install();
        boolean mvn = MavenInstaller.install();

        GitConfigurationTask.runTask();

        LogHandler.debug( "Debug mode enabled!" );
    }
}
