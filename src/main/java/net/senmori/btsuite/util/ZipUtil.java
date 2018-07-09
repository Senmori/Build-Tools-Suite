/*
 * Copyright (c) $year, $user. BuildToolsSuite. All rights reserved.
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
        InputStream is = null;
        OutputStream out = null;
        try {
            for ( Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();

                if ( filter != null ) {
                    if ( ! filter.apply(entry.getName()) ) {
                        continue;
                    }
                }

                File outFile = new File(targetFolder, entry.getName());

                if ( entry.isDirectory() ) {
                    outFile.mkdirs();
                    continue;
                }

                if ( outFile.getParentFile() != null ) {
                    outFile.getParentFile().mkdirs();
                }

                is = zip.getInputStream(entry);
                out = new FileOutputStream(outFile);
                try {
                    ByteStreams.copy(is, out);
                } finally {
                    is.close();
                    out.close();
                }
                LogHandler.info("Extracted: " + outFile);
            }
        } finally {
            zip.close();
        }
    }
}
