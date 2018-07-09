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

package net.senmori.btsuite.command;

import com.google.common.collect.ObjectArrays;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.util.StreamCapturer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class WindowsCommandIssuer implements ICommandIssuer {
    private static final BuildToolsSettings.Directories dirs = BuildToolsSettings.getInstance().getDirectories();

    @Override
    public Process issue(File workDir, String... command) {
        return createProcess( workDir, command );
    }

    @Override
    public void executeCommand(File workDir, String... command) {
        Process process = createProcess( workDir, command );

        new Thread( new StreamCapturer( process.getInputStream(), System.out ) ).start();
        new Thread( new StreamCapturer( process.getErrorStream(), System.err ) ).start();

        int status = 0;
        try {
            status = process.waitFor();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }

        if ( status != 0 ) {
            throw new RuntimeException( "Error running command, return status !=0: " + Arrays.toString( command ) );
        }
    }


    private Process createProcess(File workDir, String... command) {
        command = shim( command );
        ProcessBuilder pb = new ProcessBuilder( command );
        pb.directory( workDir );
        pb.environment().put( "JAVA_HOME", System.getProperty( "java.home" ) );
        if ( ! pb.environment().containsKey( "MAVEN_OPTS" ) ) {
            pb.environment().put( "MAVEN_OPTS", "-Xmx1024M" );
        }
        if ( dirs.getPortableGitDir() != null ) {
            String pathEnv = null;
            for ( String key : pb.environment().keySet() ) {

                if ( key.equalsIgnoreCase( "path" ) ) {
                    pathEnv = key;
                }
            }
            if ( pathEnv == null ) {
                throw new IllegalStateException( "Could not find path variable." );
            }

            String path = pb.environment().get( pathEnv );
            path += ';' + dirs.getPortableGitDir().getFile().getAbsolutePath();
            path += ';' + new File( dirs.getPortableGitDir().getFile(), "bin" ).getAbsolutePath();
            pb.environment().put( pathEnv, path );
        }
        try {
            return pb.start();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        throw new RuntimeException( "Error creating Process for command " + Arrays.toString( command ) );
    }

    private String[] shim(String... command) {
        if ( dirs.getPortableGitDir() != null ) {
            if ( "bash".equals( command[0] ) ) {
                command[0] = "git-bash";
            }
            String[] shim = { "cmd.exe", "/C" };
            command = ObjectArrays.concat( shim, command, String.class );
        }
        return command;
    }
}
