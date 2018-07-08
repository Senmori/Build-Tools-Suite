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
