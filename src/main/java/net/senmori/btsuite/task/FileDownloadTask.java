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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloadTask extends Task<File> {
    private final String url;
    private final File target;
    private final String finalName;

    public FileDownloadTask(String url, File target) {
        this( url, target, null );
    }

    public FileDownloadTask(String url, File target, String fileName) {
        this.url = url;
        this.target = target;
        this.finalName = fileName;
    }

    @Override
    public File call() throws Exception {
        int totalSize = - 1;
        URLConnection connection = new URL( url ).openConnection();
        InputStream stream = connection.getInputStream();
        totalSize = connection.getContentLength(); // in bytes

        BufferedInputStream buffInStream = new BufferedInputStream( stream );
        FileOutputStream fOut = new FileOutputStream( target );

        int count;
        byte[] buffer = new byte[1024];
        while ( ( count = buffInStream.read( buffer, 0, buffer.length ) ) != - 1 ) {
            updateMessage( "Downloading: " + count + "/" + totalSize );
            fOut.write( buffer, 0, count );
        }
        if ( finalName != null && ! finalName.trim().isEmpty() ) {
            target.renameTo( new File( target.getParent(), finalName ) );
        }
        return target;
    }
}
