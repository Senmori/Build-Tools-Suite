package net.senmori.btsuite.util;

import net.senmori.btsuite.buildtools.BuildTools;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

public class ProcessRunner {

    public static int runProcess(File workDir, String... command) throws Exception {
        if(BuildTools.portableGitDir != null) {
            if( "bash".equalsIgnoreCase( command[0] ) ) {
                command[0] = "git-bash";
            }
            String[] shim = new String[] {
                    "cmd.exe", "/C"
            };
            command = Stream.of(shim, command).flatMap(Stream::of).toArray(String[]::new); // concat both arrays into one
        }
        return runProcess0(workDir, command);
    }

    private static int runProcess0(File workDir, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder( command );
        pb.directory( workDir );
        pb.environment().put( "JAVA_HOME", System.getProperty( "java.home" ) );
        if ( !pb.environment().containsKey( "MAVEN_OPTS" ) )
        {
            pb.environment().put( "MAVEN_OPTS", "-Xmx1024M" );
        }
        if (BuildTools.portableGitDir != null )
        {
            String pathEnv = null;
            for ( String key : pb.environment().keySet() )
            {
                if ( key.equalsIgnoreCase( "path" ) )
                {
                    pathEnv = key;
                }
            }
            if ( pathEnv == null )
            {
                throw new IllegalStateException( "Could not find path variable!" );
            }

            String path = pb.environment().get( pathEnv );
            path += ";" + BuildTools.portableGitDir.getAbsolutePath();
            path += ";" + new File( BuildTools.portableGitDir, "bin" ).getAbsolutePath();
            pb.environment().put( pathEnv, path );
        }

        final Process ps = pb.start();

        new Thread( new StreamCapturer( ps.getInputStream(), System.out ) ).start();
        new Thread( new StreamCapturer( ps.getErrorStream(), System.err ) ).start();

        int status = ps.waitFor();

        if ( status != 0 )
        {
            throw new RuntimeException( "Error running command, return status !=0: " + Arrays.toString( command ) );
        }

        return status;
    }
}
