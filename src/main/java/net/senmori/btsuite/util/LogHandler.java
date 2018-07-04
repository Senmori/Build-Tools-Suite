package net.senmori.btsuite.util;

import net.senmori.btsuite.Main;

public class LogHandler {

    private static void log(String prefix, String message) {
        System.out.println(prefix + " " + message);
    }

    public static void info(String message) {
        log("[I]", message);
    }

    public static void debug(String message) {
        if( Main.isDebugEnabled() ) {
            log("[DBG]", message);
        }
    }

    public static void warn(String message) {
        log("[W]", message);
    }

    public static void error(String message) {
        log("[ERROR]", message);
    }
}
