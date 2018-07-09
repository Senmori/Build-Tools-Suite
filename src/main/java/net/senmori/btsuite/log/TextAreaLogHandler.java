/*
 * Copyright (c) $year, $user. BuildToolsSuite. All rights reserved.
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

package net.senmori.btsuite.log;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import net.senmori.btsuite.gui.BuildToolsConsole;
import net.senmori.btsuite.util.format.TextAreaFormatter;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public final class TextAreaLogHandler extends Handler {

    private final BuildToolsConsole console;
    private final TextArea textArea;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();

    public TextAreaLogHandler(BuildToolsConsole console) {
        this.console = console;
        this.textArea = console.getConsole();
    }

    @Override
    public void publish(LogRecord event) {
        final String formatted = TextAreaFormatter.DEFAULT_FORMATTER.format( event.getLevel(), event.getMessage() );

        // append log text to TextArea
        readLock.lock();
        try {
            Platform.runLater(() -> {
                try {
                    if (textArea != null) {
                        textArea.appendText( formatted );
                        textArea.selectEnd();
                    }
                } catch ( Throwable t ) {
                    throw new IllegalStateException( "Error writing to console." );
                }
            });
        } catch ( IllegalStateException ex ) {
            ex.printStackTrace();

        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {

    }
}
