package net.senmori.btsuite.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

public class Downloader {

    public static String get(String url) throws IOException {
        URLConnection con = new URL(url).openConnection();
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        BufferedInputStream bis = null;
        ByteArrayOutputStream buf = null;
        try {
            bis = new BufferedInputStream(con.getInputStream());
            buf = new ByteArrayOutputStream();

            int result = bis.read();
            while( result != -1) {
                buf.write((byte)result);
                result = bis.read();
            }
            return buf.toString(StandardCharsets.UTF_8.name());
        } finally {
            if( bis != null) bis.close();
            if( buf != null) buf.close();
        }
    }

    public static File download(String url, File target) throws IOException
    {
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
