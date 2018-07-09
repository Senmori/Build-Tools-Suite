package net.senmori.btsuite.task;

import net.senmori.btsuite.util.LogHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.concurrent.Callable;

public class InvalidateCacheTask implements Callable<Boolean> {

    private final File workingDirectory;

    public InvalidateCacheTask(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public Boolean call() throws Exception {
        LogHandler.info( "Deleting all files and directories in " + workingDirectory.getName() );
        FileUtils.cleanDirectory( workingDirectory );
        LogHandler.info( "Invalidated Cache!" );
        return true;
    }
}
