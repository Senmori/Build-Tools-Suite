package net.senmori.btsuite.util;

import com.google.common.collect.ObjectArrays;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.Settings;

import java.io.File;
import java.util.Arrays;

public class ProcessRunner {

    private static final Settings.Directories dirs = Main.getSettings().getDirectories();

    public static int runProcess(String... command) throws Exception {
        return runProcess(dirs.getWorkingDir(), command);
    }

    public static int runProcess(File workDir, String... command) throws Exception {
        if ( "bash".equalsIgnoreCase(command[0]) ) {
            command[0] = "git-bash";
        }
        return runProcess0(workDir, windowsShim(command));
    }

    public static int exec(String... command) throws Exception {
        return new ProcessBuilder(command).start().waitFor();
    }

    private static int runProcess0(File workDir, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir);
        pb.environment().put("JAVA_HOME", System.getProperty("java.home"));
        if ( ! pb.environment().containsKey("MAVEN_OPTS") ) {
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
            path += ';' + dirs.getPortableGitDir().getAbsolutePath();
            path += ';' + new File(dirs.getPortableGitDir(), "bin").getAbsolutePath();
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

    private static String[] windowsShim(String[] command) {
        return ObjectArrays.concat(new String[] {"cmd", "/c"}, command, String.class);
    }
}
