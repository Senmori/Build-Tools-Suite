package net.senmori.btsuite.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import net.senmori.btsuite.gui.BuildToolsConsole;
import net.senmori.btsuite.log.LoggerStream;
import net.senmori.btsuite.log.TextAreaLogHandler;
import net.senmori.btsuite.task.GitConfigurationTask;
import net.senmori.btsuite.task.GitInstaller;
import net.senmori.btsuite.task.MavenInstaller;
import net.senmori.btsuite.util.JFxUtils;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.PasteUtil;

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
    private AnchorPane consoleAnchorPane;

    @FXML
    private VBox consoleVBox;

    @FXML
    private ToolBar consoleToolBar;

    @FXML
    private Button consoleMakePasteBtn;

    @FXML
    private Button consoleClearChatBtn;

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

        consoleClearChatBtn.disableProperty().bind( consoleTextArea.textProperty().isEmpty() );
        consoleMakePasteBtn.disableProperty().bind( consoleClearChatBtn.disableProperty() );

        LogHandler.debug( "Debug mode enabled!" );
    }

    @FXML
    void onClearChatBtn(ActionEvent event) {
        consoleTextArea.clear();
    }

    @FXML
    void onMakePasteBtn(ActionEvent event) {
        String url = PasteUtil.post( consoleTextArea.getText() );
        PasteUtil.copyStringToClipboard( url );
        JFxUtils.createAlert( "Hastebin Log Created", "The Hastebin url has been copied to your clipboard!", url );
    }

}
