package net.senmori.btsuite.buildtools;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.Settings;
import net.senmori.btsuite.VersionString;
import net.senmori.btsuite.WindowTab;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.LogHandler;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Data
public final class BuildTools implements Runnable {
    boolean running = false;
    Settings settings = Main.getSettings();
    private boolean disableCertificateCheck = false;
    private boolean dontUpdate = false;
    private boolean skipCompile = false;
    private boolean genSrc = false;
    private boolean genDoc = false;
    private String version = "latest";
    private List<String> outputDirectories = Lists.newArrayList();
    private Map<VersionString, BuildInfo> versionMap = Maps.newHashMap();

    public void setVersionMap(Map<VersionString, BuildInfo> newMap) {
        this.versionMap.clear();
        this.versionMap.putAll(newMap);
    }

    public void setOutputDirectories(List<String> directories) {
        this.outputDirectories.clear();
        this.outputDirectories.addAll(directories);
    }

    public void addOutputDirectory(File directory) {
        if ( FileUtil.isDirectory(directory) )
            outputDirectories.add(directory.getAbsolutePath());
    }

    @Override
    public void run() {
        running = true;
        Main.setActiveTab(WindowTab.CONSOLE);
        LogHandler.debug("Starting BuildTools");
        BuildToolsProject task = new BuildToolsProject(this, settings);
        try {
            task.call();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public void setFinished() {
        running = false;
    }
}
