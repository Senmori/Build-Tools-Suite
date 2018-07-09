/*
 * Copyright (c) 2018, Senmori. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package net.senmori.btsuite.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import net.senmori.btsuite.Builder;
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
import java.util.logging.Level;
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
        if ( Builder.isDebugEnabled() ) {
            rootLogger.setLevel( Level.CONFIG );
        }

        BuildToolsConsole console = new BuildToolsConsole( consoleTextArea );
        TextAreaLogHandler handler = new TextAreaLogHandler( console );
        rootLogger.addHandler( handler );
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
