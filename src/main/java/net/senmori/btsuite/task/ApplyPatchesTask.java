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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import difflib.DiffUtils;
import difflib.Patch;
import javafx.concurrent.Task;
import net.senmori.btsuite.Console;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * This task applies patches located in {@link #patchesSource} to files located in {@link #sourceFileDir}
 * and the resulting output file will be created/updated in {@link #outputDirectory}
 */
public class ApplyPatchesTask extends Task<Integer> {

    /**
     * Where the .patch files are located
     */
    private final File patchesSource;
    /**
     * Where the files to be patched are located
     */
    private final File sourceFileDir;
    /**
     * Where to put the output files
     */
    private final File outputDirectory;
    private final Console console;

    public ApplyPatchesTask(File patchesSource, File sourceFileDir, File outputDirectory, Console console) {
        this.patchesSource = patchesSource;
        this.sourceFileDir = sourceFileDir;
        this.outputDirectory = outputDirectory;
        this.console = console;
    }

    @Override
    protected Integer call() throws Exception {

        int applied = 0;
        File[] patches = patchesSource.listFiles( ( (dir, name) -> name.endsWith( ".patch" ) ) );
        for ( File patchFile : patches ) {

            String targetFile = "net/minecraft/server/" + patchFile.getName().replace( ".patch", ".java" );

            File clean = new File( sourceFileDir, targetFile );
            File t = new File( outputDirectory.getParentFile(), targetFile );
            t.getParentFile().mkdirs();

            console.setOptionalText( "Patching with " + patchFile.getName() );

            List<String> readFile = Files.readLines( patchFile, Charsets.UTF_8 );

            // Manually append prelude if it is not found in the first few lines.
            boolean preludeFound = false;
            for ( int i = 0; i < Math.min( 3, readFile.size() ); i++ ) {
                if ( readFile.get( i ).startsWith( "+++" ) ) {
                    preludeFound = true;
                    break;
                }
            }
            if ( !preludeFound ) {
                readFile.add( 0, "+++" );
            }

            Patch parsedPatch = DiffUtils.parseUnifiedDiff( readFile );
            List<?> modifiedLines = DiffUtils.patch( com.google.common.io.Files.readLines( clean, Charsets.UTF_8 ), parsedPatch );

            BufferedWriter bw = new BufferedWriter( new FileWriter( t ) );
            for ( String line : ( List<String> ) modifiedLines ) {
                bw.write( line );
                bw.newLine();
            }
            bw.close();

            applied++;
        }
        return applied;
    }
}
