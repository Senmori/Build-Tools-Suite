package net.senmori.btsuite.api;

import java.io.File;

public interface IProcessRunner {

    int runProcess(File workDir, String... command) throws Exception;
}
