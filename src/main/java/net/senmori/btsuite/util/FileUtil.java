package net.senmori.btsuite.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class FileUtil {

    public static void copyJar(String path, final String jarPrefix, File outJar) throws Exception {
        File[] files = new File(path).listFiles((dir, name) -> name.startsWith(jarPrefix) && name.endsWith(".jar"));

        if(!outJar.getParentFile().isDirectory()) {
            outJar.getParentFile().mkdirs();
        }

        for(File file : files) {
            System.out.println("Copying " + file.getName() + " to " +  outJar.getAbsolutePath());
            Files.copy(file.toPath(),  outJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  - Saved as " + outJar);
        }
    }
}
