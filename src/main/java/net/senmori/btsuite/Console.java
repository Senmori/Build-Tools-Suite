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

package net.senmori.btsuite;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

/**
 * This class handles the output of text to the console.
 *
 * All set operations are run on the JavaFX application thread via {@link Platform#runLater(Runnable)}.
 */
public class Console {

    private final TextArea textArea = new TextArea();
    private final ProgressBar progressBar = new ProgressBar();
    private final Text progressText = new Text();
    private final Text optionalText = new Text();

    public Console() {
        this.textArea.setEditable( false ); // just in case
        this.textArea.setWrapText( true ); // just in case

        progressText.visibleProperty().bind( Bindings.isNotEmpty( progressText.textProperty() ) );
        optionalText.visibleProperty().bind( Bindings.isNotEmpty( optionalText.textProperty() ) );

        progressBar.visibleProperty().bindBidirectional( progressText.visibleProperty() );
    }

    public void clearConsole() {
        Platform.runLater( () -> {
            textArea.clear();
        } );
    }

    /**
     * Reset all nodes on the console. <br>
     * This will unbind the progress bar's progress to -1.0D.
     * This will reset the {@link #getProgressText()} to empty string.
     * This will reset the {@link #getOptionalText()} to empty string.
     *
     * Finally, all bindings will be removed.
     * This method will be run on the JavaFX application thread.
     */
    public void reset() {
        Platform.runLater( () -> {
            progressBar.progressProperty().unbind();
            progressBar.setProgress( -1.0D );
            progressText.textProperty().unbind();
            progressText.setText( "" );
            optionalText.textProperty().unbind();
            optionalText.setText( "" );
            progressBar.setVisible( false );
        } );
    }

    public TextArea getConsoleTextArea() {
        return textArea;
    }

    public void append(String text) {
        Platform.runLater( () -> {
            textArea.appendText( text );
            textArea.selectEnd();
        } );
    }

    public String getText() {
        return textArea.getText();
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgress(double progress) {
        Platform.runLater( () -> progressBar.setProgress( progress ) );
    }

    public double getProgress() {
        return progressBar.getProgress();
    }

    public Text getProgressTextField() {
        return progressText;
    }

    public void setProgressText(String text) {
        Platform.runLater( () -> {
                    progressText.setText( text );
                    textArea.selectEnd();
                }
        );
    }

    public String getProgressText() {
        return progressText.getText();
    }

    public Text getOptionalTextField() {
        return optionalText;
    }

    public void setOptionalText(String text) {
        Platform.runLater( () -> {
                    optionalText.setText( text );
                    textArea.selectEnd();
                }
        );
    }

    public String getOptionalText() {
        return optionalText.getText();
    }
}
