package net.senmori.btsuite.buildtools.process;

import net.senmori.btsuite.util.StreamRedirector;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

import static net.senmori.btsuite.util.ProcessRunner.PORTABLE_GIT_DIR;

public class JavaProcessRunner implements IProcessRunner {

    private final File portableGitDir;

    public JavaProcessRunner(File portableGitDir) {
        this.portableGitDir = portableGitDir;
    }

    @Override
    public int runProcess(File workDir, String... command) throws Exception {
        if(portableGitDir != null) {
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

    private int runProcess0(File workDir, String...command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder( command );
        pb.directory(workDir);
        pb.environment().put("JAVA_HOME", System.getProperty("java.home"));
        if(!pb.environment().containsKey("MAVEN_OPTS")) {
            pb.environment().put("MAVEN_OPTS", "-Xmx1024M"); // TODO: Lets users change maven options
        }

        if(portableGitDir != null) {
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
