package net.senmori.btsuite.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class FileUtil {

    public static void copyJar(String path, final String jarPrefix, File outJar) throws Exception {
        File[] files = new File(path).listFiles((dir, name) -> name.startsWith(jarPrefix) && name.endsWith(".jar"));

        if ( !outJar.getParentFile().isDirectory() )
            if ( !outJar.getParentFile().mkdir() )
                return; // access denied

        if ( files == null || files.length == 0 )
            return;

        for ( File file : files ) {
            LogHandler.info("Copying " + file.getName() + " to " + outJar.getAbsolutePath());
            Files.copy(file.toPath(), outJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LogHandler.info("  - Saved as " + outJar);
        }
    }

    public static boolean isDirectory(File file) {
        return file != null && file.isDirectory();
    }

    public static boolean isNonEmptyDirectory(File dir) {
        if ( dir != null && dir.exists() && dir.isDirectory() ) {
            File[] files = dir.listFiles();
            return files != null && files.length > 0;
        } else {
            return false;
        }
    }

    public static void deleteDirectory(File dir) {
        for ( File file : dir.listFiles() ) {
            if ( file.isDirectory() ) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
        dir.delete();
    }
}
