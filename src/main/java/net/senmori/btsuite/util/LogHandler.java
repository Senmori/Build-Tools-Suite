package net.senmori.btsuite.util;

import javafx.application.Platform;
import lombok.extern.log4j.Log4j2;
import net.senmori.btsuite.Main;
import org.apache.logging.log4j.Level;

@Log4j2
public class LogHandler {

    private static void log(Level level, String message) {
        Platform.runLater(() -> log.log(level, message));
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void debug(String message) {
        if( Main.isDebugEnabled() ) {
            log(Level.DEBUG, message);
        }
    }

    public static void warn(String message) {
        log(Level.WARN, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }
}
