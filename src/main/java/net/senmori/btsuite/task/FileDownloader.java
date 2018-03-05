package net.senmori.btsuite.task;

import javafx.concurrent.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class FileDownloader extends Task<File> {

    private final String url;
    private final File target;
    public FileDownloader(String url, File target) {
        this.url = url;
        this.target = target;
    }

    @Override
    public File call() throws Exception {
        URL con = new URL(url);
        InputStream stream = con.openStream();
        ReadableByteChannel bis = Channels.newChannel(stream);
        FileOutputStream fos = new FileOutputStream(target);

        fos.getChannel().transferFrom(bis, 0, Long.MAX_VALUE);

        bis.close();
        fos.close();
        stream.close();
        return target;
    }
}
