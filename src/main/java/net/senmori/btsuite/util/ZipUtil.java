package net.senmori.btsuite.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ZipUtil {

    public static void unzip(File zipFile, File targetFolder) throws IOException {
        unzip(zipFile, targetFolder, null);
    }

    public static void unzip(File zipFile, File targetFolder, Predicate<String> filter) throws IOException {
        targetFolder.mkdir();
        ZipFile zip = new ZipFile(zipFile);

        try {
            for(Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();

                if(filter != null) {
                    if(!filter.test(entry.getName())) {
                        continue;
                    }
                }

                File outFile = new File(targetFolder, entry.getName());

                if(entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }

                if( outFile.getParentFile() != null) {
                    outFile.getParentFile().mkdirs();
                }

                InputStream is = zip.getInputStream(entry);
                OutputStream out = new FileOutputStream(outFile);
                try {
                    is = zip.getInputStream(entry);
                    out = new FileOutputStream(outFile);

                    byte[] buffer = new byte[2048];
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }

                } finally {
                    is.close();
                    out.close();
                }
                System.out.println("Extracted: " + outFile);
            }
        } finally {
            zip.close();
        }
    }
}
