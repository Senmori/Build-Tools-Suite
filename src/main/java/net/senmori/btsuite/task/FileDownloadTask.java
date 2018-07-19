/*
 * Copyright (c) 2018, Senmori. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package net.senmori.btsuite.task;

import javafx.concurrent.Task;
import lombok.Cleanup;
import net.senmori.btsuite.Console;
import net.senmori.btsuite.storage.Directory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class FileDownloadTask extends Task<Directory> {
    private final String url;
    private final Directory target;
    private final Console console;

    public FileDownloadTask(String url, Directory target) {
        this( url, target, null );
    }

    public FileDownloadTask(String url, Directory target, Console console) {
        this.url = url;
        this.target = target;
        this.console = console;
    }

    @Override
    public Directory call() throws Exception {
        File targetFile = target.getFile();
        try {
            URL link = new URL( url );
            URLConnection connection = link.openConnection();
            int totalSize = connection.getContentLength();
            if ( totalSize < 0 ) {
                // can't determine file size, just download it
                return fosDownload( connection );
            }
            InputStream inputStream = connection.getInputStream();
            BufferedInputStream in = new BufferedInputStream( inputStream );
            FileOutputStream fos = new FileOutputStream( targetFile );

            int count = 0;
            byte[] data = new byte[1024];
            double sumCount = 0.0D;

            while ( ( count = in.read( data, 0, 1024 ) ) != -1 ) {
                fos.write( data, 0, count );

                sumCount += count;
                if ( ( totalSize > 0 ) && ( console != null ) ) {
                    int percent = ( int ) ( ( sumCount / ( double ) totalSize ) * 100.0D );
                    console.setOptionalText( "Downloading: " + percent + '%' );
                }
            }
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return target;
    }

    private Directory fosDownload(URLConnection connection) {
        File targetFile = target.getFile();
        try {
            @Cleanup InputStream stream = connection.getInputStream();
            @Cleanup FileOutputStream fos = new FileOutputStream( targetFile );
            @Cleanup ReadableByteChannel rbc = Channels.newChannel( stream );
            fos.getChannel().transferFrom( rbc, 0L, Long.MAX_VALUE );
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return target;
    }
}
