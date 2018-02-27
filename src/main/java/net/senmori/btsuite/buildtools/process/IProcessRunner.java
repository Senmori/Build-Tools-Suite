package net.senmori.btsuite.buildtools.process;

import java.io.File;

public interface IProcessRunner {

    int runProcess(File workDir, String... command) throws Exception;
}
