package net.senmori.btsuite.task;

import lombok.Cleanup;
import net.senmori.btsuite.util.LogHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;

public class FileDownloader implements Callable<File> {
    private final String url;
    private final File target;

    public FileDownloader(String url, File target) {
        this.url = url;
        this.target = target;
    }

    @Override
    public File call() throws Exception {
        URL con = new URL(url);
        @Cleanup InputStream stream = con.openStream();
        @Cleanup FileOutputStream fos = new FileOutputStream(target);
        @Cleanup ReadableByteChannel bis = Channels.newChannel(stream);
        long bytes = fos.getChannel().transferFrom(bis, 0L, Long.MAX_VALUE);
        LogHandler.debug("Downloaded " + humanReadableBytes(bytes, false) + " into " + target);
        return target;
    }

    public static String humanReadableBytes(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
