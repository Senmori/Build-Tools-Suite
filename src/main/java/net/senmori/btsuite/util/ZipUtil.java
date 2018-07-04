package net.senmori.btsuite.util;

import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ZipUtil {

    public static void unzip(File zipFile, File targetFolder) throws IOException {
        unzip(zipFile, targetFolder, null);
    }

    public static void unzip(File zipFile, File targetFolder, com.google.common.base.Predicate<String> filter) throws IOException {
        targetFolder.mkdir();
        ZipFile zip = new ZipFile(zipFile);
        LogHandler.info("Unzipping " + zip.getName());
        try {
            for ( Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();

                if ( filter != null ) {
                    if ( ! filter.apply(entry.getName()) ) {
                        continue;
                    }
                }

                File outFile = new File(targetFolder, entry.getName());
                LogHandler.debug("Extracting " + outFile.getName());

                if ( entry.isDirectory() ) {
                    outFile.mkdirs();
                    continue;
                }

                if ( outFile.getParentFile() != null ) {
                    outFile.getParentFile().mkdirs();
                }

                InputStream is = zip.getInputStream(entry);
                OutputStream out = new FileOutputStream(outFile);
                try {
                    ByteStreams.copy(is, out);
                } finally {
                    is.close();
                    out.close();
                }
                LogHandler.debug("Extracted: " + outFile);
            }
            LogHandler.info("Finished unzipping " + zip.getName());
        } finally {
            zip.close();
        }
    }
}
