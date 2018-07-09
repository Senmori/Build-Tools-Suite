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
