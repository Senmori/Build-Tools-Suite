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
import net.senmori.btsuite.buildtools.BuildTools;
import net.senmori.btsuite.minecraft.VersionManifest;
import net.senmori.btsuite.util.LogHandler;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

public class InvalidateCacheTask extends Task<Boolean> {

    private final BuildTools buildTools;
    private final VersionManifest manifest;

    public InvalidateCacheTask(BuildTools buildTools, VersionManifest manifest) {
        this.buildTools = buildTools;
        this.manifest = manifest;
    }

    @Override
    public Boolean call() throws Exception {
        File workingDirectory = buildTools.getWorkingDirectory().getFile();
        LogHandler.info( "Deleting all files and directories in " + workingDirectory.getName() );
        // Use our own deletion method instead of apache so we can redirect output to where we want
        if ( !workingDirectory.exists() ) {
            throw new IllegalArgumentException( workingDirectory + " does not exist." );
        }
        if ( !workingDirectory.isDirectory() ) {
            throw new IllegalArgumentException( workingDirectory + " is not a directory" );
        }

        File[] files = workingDirectory.listFiles();
        if ( ( files == null ) || ( files.length < 1 ) ) {
            throw new IOException( "Failed to list contents of " + workingDirectory );
        }

        Stream.of( files ).forEach( this::delete );

        // Re-import spigot and minecraft versions
        buildTools.importVersions();
        manifest.importVersions();

        return true;
    }

    private void delete(File file) {
        if ( file.isDirectory() ) {
            File[] files = file.listFiles();
            if ( ( files == null ) || ( files.length < 1 ) ) {
                return;
            }
            for ( File in : files ) {
                if ( in.isDirectory() ) {
                    delete( in );
                } else {
                    buildTools.getConsole().setOptionalText( FilenameUtils.getBaseName( in.getName() ) );
                    try {
                        FileDeleteStrategy.FORCE.delete( in );
                    } catch ( IOException e ) {

                    }
                }
            }
        }
        buildTools.getConsole().setOptionalText( "Deleting " + FilenameUtils.getBaseName( file.getName() ) );
        try {
            FileDeleteStrategy.FORCE.delete( file );
        } catch ( IOException e ) {

        }
    }
}
