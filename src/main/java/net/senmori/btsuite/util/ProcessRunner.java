package net.senmori.btsuite.util;

import net.senmori.btsuite.Main;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

public class ProcessRunner {

    public static final File CWD = Main.WORK_DIR;
    public static final boolean IS_WINDOWS = System.getProperty("os.name").startsWith("Windows");
    public static final boolean AUTOCRLF = !"\n".equalsIgnoreCase(System.getProperty("line.separator"));

    public static File PORTABLE_GIT_DIR;

    public static int runProcess(File workDir, String... command) throws Exception {
        if(PORTABLE_GIT_DIR != null) {
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

    private static int runProcess0(File workDir, String...command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder( command );
        pb.directory(workDir);
        pb.environment().put("JAVA_HOME", System.getProperty("java.home"));
        if(!pb.environment().containsKey("MAVEN_OPTS")) {
            pb.environment().put("MAVEN_OPTS", "-Xmx1024M"); // TODO: Lets users change maven options
        }

        if(PORTABLE_GIT_DIR != null) {
            String pathEnv = null;
            for(String key : pb.environment().keySet()) {
                if(key.equalsIgnoreCase("path")) {
                    pathEnv = key;
                    break;
                }
            }
            if(pathEnv == null) {
                throw new IllegalStateException("Could not find PATH variable");
            }

            String path = pb.environment().get(pathEnv);
            path += ":" + PORTABLE_GIT_DIR.getAbsolutePath();
            path += ";" + new File(PORTABLE_GIT_DIR, "bin").getAbsolutePath();
            pb.environment().put(pathEnv, path);
        }

        final Process ps = pb.start();
        new Thread( new StreamRedirector(ps.getInputStream() )).start();
        new Thread( new StreamRedirector(ps.getErrorStream() )).start();

        int status = ps.waitFor();

        if(status != 0) {
            throw new RuntimeException("Error running command, returning status != 0: " + Arrays.toString(command));
        }
        return status;
    }

}
