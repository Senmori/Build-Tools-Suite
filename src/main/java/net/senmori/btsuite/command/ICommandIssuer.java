package net.senmori.btsuite.command;

import java.io.File;

public interface ICommandIssuer {

    /**
     * Execute a command, returning a {@link Process}.
     *
     * @param workDir the directory to issue the command in
     * @param command the command to run
     *
     * @return the {@link Process} the command started
     */
    Process issue(File workDir, String... command);

    /**
     * Execute a command.<br>
     * On Windows systems, this will redirect the output of the command to System.out and System.err.
     * If you want to capture the output, you will need to redirect those streams.
     *
     * @param workDir the directory to issue the command in
     * @param command the command to run
     */
    void executeCommand(File workDir, String... command);
}
