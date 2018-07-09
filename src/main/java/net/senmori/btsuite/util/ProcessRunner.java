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

import com.google.common.collect.ObjectArrays;
import net.senmori.btsuite.storage.BuildToolsSettings;

import java.io.File;
import java.util.Arrays;

@Deprecated
public class ProcessRunner {

    private static final BuildToolsSettings.Directories dirs = BuildToolsSettings.getInstance().getDirectories();

    public static int runProcess(String... command) throws Exception {
        return runProcess( dirs.getWorkingDir().getFile(), command );
    }

    public static int runProcess(File workDir, String... command) throws Exception {
        if ( dirs.getPortableGitDir() != null ) {
            if ( "bash".equalsIgnoreCase( command[0] ) ) {
                command[0] = "git-bash";
            }
            String[] shim = { "cmd.exe", "/C" };
            command = ObjectArrays.concat( shim, command, String.class );
        }
        return runProcess0( workDir, command );
    }

    public static int runProcessRaw(File workDir, String... command) throws Exception {
        return runProcess0(workDir, command);
    }

    private static int runProcess0(File workDir, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory( workDir );
        pb.environment().put("JAVA_HOME", System.getProperty("java.home"));
        if ( ! pb.environment().containsKey( "MAVEN_OPTS" ) ) {
            pb.environment().put("MAVEN_OPTS", "-Xmx1024M");
        }
        if ( dirs.getPortableGitDir() != null ) {
            String pathEnv = pb.environment().get("path");
            if ( pathEnv == null ) {
                //try 'Path'
                pathEnv = pb.environment().get("Path");
                if ( pathEnv == null ) {
                    throw new IllegalStateException("Cannot find path variable!");
                }
            }

            String path = pb.environment().get(pathEnv);
            path += ';' + dirs.getPortableGitDir().getFile().getAbsolutePath();
            path += ';' + new File( dirs.getPortableGitDir().getFile(), "bin" ).getAbsolutePath();
            pb.environment().put(pathEnv, path);
        }
        final Process ps = pb.start();

        new Thread(new StreamCapturer(ps.getInputStream(), System.out)).start();
        new Thread(new StreamCapturer(ps.getErrorStream(), System.err)).start();

        int status = ps.waitFor();

        if ( status != 0 ) {
            throw new RuntimeException("Error running command, return status !=0: " + Arrays.toString(command));
        }

        return status;
    }
}
