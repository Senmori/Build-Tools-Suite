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

import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import net.senmori.btsuite.Callback;
import net.senmori.btsuite.pool.TaskPool;
import net.senmori.btsuite.pool.TaskPools;

public class Console {
    private static final Console INSTANCE = new Console();

    public static Console getInstance() {
        return INSTANCE;
    }

    private Console() {
    }

    private TextArea console;
    private ProgressBar progressBar;
    private Text textField;
    private Text optionalTextField;

    protected void setConsole(TextArea textArea) {
        this.console = textArea;
    }

    protected void setProgressBar(ProgressBar bar) {
        this.progressBar = bar;
    }

    protected void setProgessTextField(Text textField) {
        this.textField = textField;
    }

    protected void setOptionalTextField(Text optionalTextField) {
        this.optionalTextField = optionalTextField;
    }

    /**
     * Get this console's progress bar.
     *
     * @return the progress bar
     */
    public ProgressBar getProgressBar() {
        resetProgress();
        return progressBar;
    }

    private TaskPool pool = TaskPools.createSingleTaskPool();

    public void newTask(Task task, String progressText, Callback callback) {
        task.setOnRunning( (worker) -> {
            reset();
            textField.setText( progressText );
            progressBar.setVisible( true );
            optionalTextField.textProperty().bind( task.messageProperty() );
            progressBar.progressProperty().bind( task.progressProperty() );
        } );
        task.setOnCancelled( (worker) -> {
            reset();
        } );
        task.setOnFailed( (worker) -> {
            reset();
        } );
        task.setOnSucceeded( (worker) -> {
            reset();
            callback.accept( task.getValue() );
        } );

        pool.submit( task );
    }

    /**
     * Reset the progress bar and it's accompanying text.
     */
    public void reset() {
        resetProgress();
        textField.textProperty().unbind();
        textField.setText( "" );
        optionalTextField.textProperty().unbind();
        optionalTextField.setText( "" );
        progressBar.setVisible( false );
    }

    /**
     * Set this progress bar's progress to {@code -1.0D} and
     * removes any bindings it might have.
     */
    public void resetProgress() {
        this.progressBar.progressProperty().unbind();
        this.progressBar.setProgress( - 1.0D ); // indeterminate
    }

    /**
     * Clear the console of all text.
     */
    public void clearConsole() {
        this.console.clear();
    }

    public TextArea getConsole() {
        return console;
    }
}
