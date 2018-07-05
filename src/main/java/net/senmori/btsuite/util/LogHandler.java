package net.senmori.btsuite.util;

import javafx.application.Platform;
import net.senmori.btsuite.Builder;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogHandler {
    public static final Logger log = Logger.getLogger(LogHandler.class.getName());

    private static void log(Level level, String message) {
        Platform.runLater(() -> log.log(level, message));
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void debug(String message) {
        if( Builder.isDebugEnabled() ) {
            log(Level.FINE, message);
        }
    }

    public static void warn(String message) {
        log(Level.WARNING, message);
    }

    public static void error(String message) {
        log(Level.SEVERE, message);
    }
}
