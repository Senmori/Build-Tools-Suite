package net.senmori.btsuite.util;

import com.google.common.io.CharStreams;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Downloader {

    public static String get(String url) throws IOException {
        URLConnection con = new URL(url).openConnection();
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        InputStreamReader r = null;
        try {
            r = new InputStreamReader(con.getInputStream());

            return CharStreams.toString(r);
        } finally {
            if ( r != null ) {
                r.close();
            }
        }
    }

    public static File download(String url, File target) throws IOException {
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

    public static File asyncDownload(String url, File target) throws IOException {
        Task<File> task = new Task<File>() {
            @Override
            protected File call() throws Exception {
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
        };
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(task);

        pool.shutdown();
        return task.getValue();
    }
}
