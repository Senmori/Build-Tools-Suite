package net.senmori.btsuite.log;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import net.senmori.btsuite.gui.BuildToolsConsole;
import net.senmori.btsuite.util.format.TextAreaFormatter;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;


public final class TextAreaLogHandler extends Handler {

    private static BuildToolsConsole console;
    private static TextArea textArea;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();

    @Override
    public void publish(LogRecord event) {
        readLock.lock();

        Level level = event.getLevel();
        String message = event.getMessage() + "\n";

        TextAreaFormatter formatter = console.getFormatter( level );
        Text text = formatter.format( level, message );

        // append log text to TextArea
        try {
            Platform.runLater(() -> {
                try {
                    if (textArea != null) {
                        textArea.appendText( message );
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

    public static void setConsole(BuildToolsConsole console) {
        TextAreaLogHandler.console = console;
        TextAreaLogHandler.textArea = console.getConsole();
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {

    }
}
