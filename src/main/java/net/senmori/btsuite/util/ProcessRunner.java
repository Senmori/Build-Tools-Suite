package net.senmori.btsuite.util;

import com.google.common.collect.ObjectArrays;
import net.senmori.btsuite.Main;

import java.io.File;
import java.util.Arrays;

public class ProcessRunner {

    public static int runProcess(String... command) throws Exception {
        return runProcess(Main.WORK_DIR, command);
    }

    public static int runProcess(File workDir, String... command) throws Exception {
        if( "bash".equalsIgnoreCase( command[0] ) ) {
            command[0] = "git-bash";
        }
        return runProcess0(workDir, windowsShim(command));
    }

    private static int runProcess0(File workDir, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder( command );
        pb.directory( workDir );
        pb.environment().put( "JAVA_HOME", System.getProperty( "java.home" ) );
        if ( !pb.environment().containsKey( "MAVEN_OPTS" ) ) {
            pb.environment().put( "MAVEN_OPTS", "-Xmx1024M" );
        }
        if (Main.PORTABLE_GIT_DIR != null) {
            String pathEnv = pb.environment().get("path");
            if (pathEnv == null) {
                //try 'Path'
                pathEnv = pb.environment().get("Path");
                if (pathEnv == null) {
                    throw new IllegalStateException("Cannot find path variable!");
                }
            }

            String path = pb.environment().get(pathEnv);
            path += ';' + Main.PORTABLE_GIT_DIR.getAbsolutePath();
            path += ';' + new File(Main.PORTABLE_GIT_DIR, "bin").getAbsolutePath();
            pb.environment().put(pathEnv, path);
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

    private static String[] windowsShim(String[] command) {
        return ObjectArrays.concat(new String[]{"cmd", "/c"}, command, String.class);
    }
}
