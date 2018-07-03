package net.senmori.btsuite.util;

public class SystemChecker {

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static boolean isAutocrlf() {
        return ! "\n".equalsIgnoreCase(System.getProperty("line.separator"));
    }
}
