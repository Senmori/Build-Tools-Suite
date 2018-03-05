package net.senmori.btsuite.util;

import java.io.File;

public class SystemChecker {

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static boolean isAutocrlf() {
        return !"\n".equalsIgnoreCase(System.getProperty("line.separator"));
    }

    public static boolean isDirectory(File file) {
        return file != null && file.isDirectory();
    }

    public static boolean isNonEmptyDirectory(File dir) {
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            return files != null && files.length != 0;
        } else {
            return false;
        }
    }
}
