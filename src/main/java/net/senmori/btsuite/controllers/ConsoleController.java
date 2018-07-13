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

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.Getter;
import net.senmori.btsuite.Console;
import net.senmori.btsuite.log.LoggerStream;
import net.senmori.btsuite.log.TextAreaLogHandler;
import net.senmori.btsuite.util.JFxUtils;
import net.senmori.btsuite.util.PasteUtil;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@Getter
public class ConsoleController {

    @FXML
    private ResourceBundle resources;
    @FXML
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
    private Text consoleProgressTextID;
    @FXML
    private ProgressBar consoleProgressBar;
    @FXML
    private Text consoleOptionalText;

    private final Console console;

    public ConsoleController(Console console) {
        this.console = console;
    }

    @FXML
    void initialize() {
        LogManager.getLogManager().reset(); // remove all handlers
        Logger rootLogger = LogManager.getLogManager().getLogger( "" );

        rootLogger.addHandler( new TextAreaLogHandler( console ) );
        LoggerStream.setOutAndErrToLog();

        // Don't let users clear chat / make pastes when there is no chat
        consoleClearChatBtn.disableProperty().bind( consoleTextArea.textProperty().isEmpty() );
        consoleMakePasteBtn.disableProperty().bind( consoleClearChatBtn.disableProperty() );

        consoleTextArea.textProperty().bindBidirectional( console.getConsoleTextArea().textProperty() );
        consoleTextArea.textProperty().addListener( (observable, oldValue, newValue) -> {
            // scroll down when text is added
            consoleTextArea.appendText( "" );
            console.getConsoleTextArea().appendText( "" );
        } );
        console.getConsoleTextArea().textProperty().addListener( (observable, oldValue, newValue) -> {
            consoleTextArea.appendText( "" );
            console.getConsoleTextArea().appendText( "" );
        } );

        // Don't show text if the text is empty
        consoleProgressTextID.visibleProperty().bind( Bindings.isNotEmpty( consoleProgressTextID.textProperty() ) );
        consoleOptionalText.visibleProperty().bind( Bindings.isNotEmpty( consoleOptionalText.textProperty() ) );

        // bind console text to Console's text
        consoleProgressTextID.textProperty().bind( console.getProgressTextField().textProperty() );
        consoleOptionalText.textProperty().bind( console.getOptionalTextField().textProperty() );

        // bind console progress bar to Console's progress bar
        consoleProgressBar.progressProperty().bind( console.getProgressBar().progressProperty() );
        consoleProgressBar.visibleProperty().bind( console.getProgressBar().visibleProperty() );
    }

    @FXML
    void onClearChatBtn(ActionEvent event) {
        console.clearConsole();
    }

    @FXML
    void onMakePasteBtn(ActionEvent event) {
        String url = PasteUtil.post( console.getText() );
        PasteUtil.copyStringToClipboard( url );
        JFxUtils.createAlert( "Hastebin Log Created", "The Hastebin url has been copied to your clipboard!", url );
    }

}
