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
import net.senmori.btsuite.Builder;
import net.senmori.btsuite.WindowTab;
import net.senmori.btsuite.log.LoggerStream;
import net.senmori.btsuite.log.TextAreaLogHandler;
import net.senmori.btsuite.util.JFxUtils;
import net.senmori.btsuite.util.PasteUtil;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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

    @FXML
    void initialize() {
        Builder.getInstance().setController( WindowTab.CONSOLE, this );
        LogManager.getLogManager().reset(); // remove all handlers
        Logger rootLogger = LogManager.getLogManager().getLogger("");

        Console.getInstance().setConsole( consoleTextArea );
        Console.getInstance().setProgressBar( consoleProgressBar );
        Console.getInstance().setProgessTextField( consoleProgressTextID );
        Console.getInstance().setOptionalTextField( consoleOptionalText );
        rootLogger.addHandler( new TextAreaLogHandler() );
        LoggerStream.setOutAndErrToLog();

        consoleClearChatBtn.disableProperty().bind( consoleTextArea.textProperty().isEmpty() );
        consoleMakePasteBtn.disableProperty().bind( consoleClearChatBtn.disableProperty() );

        consoleProgressTextID.visibleProperty().bind( Bindings.isNotEmpty( consoleProgressTextID.textProperty() ) );
        consoleOptionalText.visibleProperty().bind( Bindings.isNotEmpty( consoleOptionalText.textProperty() ) );
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
