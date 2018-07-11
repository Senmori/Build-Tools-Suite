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

import com.google.common.collect.Lists;
import javafx.concurrent.Task;
import net.senmori.btsuite.Builder;
import net.senmori.btsuite.util.LogHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class InvalidateCacheTask extends Task<Boolean> {

    private final File workingDirectory;
    private final Task parent;

    public InvalidateCacheTask(File workingDirectory) {
        this.workingDirectory = workingDirectory;
        this.parent = null;
    }

    @Override
    public Boolean call() throws Exception {
        LogHandler.info( "Deleting all files and directories in " + workingDirectory.getName() );
        // Use our own deletion method instead of apache so we can redirect output to where we want
        if ( ! workingDirectory.exists() ) {
            throw new IllegalArgumentException( workingDirectory + " does not exist." );
        }
        if ( ! workingDirectory.isDirectory() ) {
            throw new IllegalArgumentException( workingDirectory + " is not a directory" );
        }

        final File[] files = workingDirectory.listFiles();
        if ( files == null ) {
            throw new IOException( "Failed to list contents of " + workingDirectory );
        }

        List<File> toDelete = Lists.newLinkedList();
        IOException exception = null;
        for ( final File file : files ) {
            updateMessage( FilenameUtils.getBaseName( file.getName() ) );
            try {
                FileUtils.forceDelete( file );
            } catch ( IOException e ) {
                toDelete.add( file );
            }
        }

        if ( ! toDelete.isEmpty() ) {
            Iterator<File> iter = toDelete.iterator();
            while ( iter.hasNext() ) {
                File next = iter.next();
                FileUtils.deleteQuietly( next );
            }
        }

        // Re-import spigot versions
        Builder.getInstance().getBuildTabController().importVersions().get();
        Builder.getInstance().getMinecraftTabController().importVersions().get();

        LogHandler.info( "Invalidated Cache!" );
        return true;
    }
}
