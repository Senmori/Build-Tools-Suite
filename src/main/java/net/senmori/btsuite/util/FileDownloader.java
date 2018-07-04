package net.senmori.btsuite.util;

import lombok.Cleanup;
import lombok.extern.apachecommons.CommonsLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class FileDownloader {

    public static File download(String url, File target) throws IOException {
        URL con = new URL(url);
        @Cleanup InputStream stream = con.openStream();
        @Cleanup FileOutputStream fos = new FileOutputStream(target);
        @Cleanup ReadableByteChannel bis = Channels.newChannel(stream);
        long bytes = fos.getChannel().transferFrom(bis, 0L, Long.MAX_VALUE);
        LogHandler.debug("Downloaded " + humanReadableBytes(bytes, false) + " into " + target);
        return target;
    }

    public static File downloadWrapped(String url, File target) {
        try {
            return download(url, target);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return null;
    }

    private static String humanReadableBytes(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
