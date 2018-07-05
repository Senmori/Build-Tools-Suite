package net.senmori.btsuite.log;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;


public final class TextAreaLogHandler extends Handler {

    private static TextArea textArea;


    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();

    @Override
    public void publish(LogRecord event) {
        readLock.lock();

        Level level = event.getLevel();
        String message = event.getMessage() + "\n";

        //TODO: Implement coloring for certain levels

        // append log text to TextArea
        try {
            Platform.runLater(() -> {
                try {
                    if (textArea != null) {
                        if ( textArea.getText().isEmpty() ) {
                            textArea.setText(message);
                        } else {
                            textArea.selectEnd();
                            textArea.insertText(textArea.getText().length(), message);
                        }
                    }
                } catch (final Throwable t) {
                    throw t;
                }
            });
        } catch (final IllegalStateException ex) {
            ex.printStackTrace();

        } finally {
            readLock.unlock();
        }
    }

    public static void setTextArea(TextArea textArea) {
        TextAreaLogHandler.textArea = textArea;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {

    }
}
