package net.senmori.btsuite.buildtools;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import net.senmori.btsuite.Builder;
import net.senmori.btsuite.VersionString;
import net.senmori.btsuite.WindowTab;
import net.senmori.btsuite.controllers.BuildTabController;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.LogHandler;

import java.io.File;
import java.util.List;
import java.util.Map;

@Data
public final class BuildTools implements Runnable {
    private static BuildTabController controller;

    public static void setController(BuildTabController controller) {
        BuildTools.controller = controller;
    }

    boolean running = false;
    BuildToolsSettings buildToolsSettings = BuildToolsSettings.getInstance();
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
        Builder.setActiveTab(WindowTab.CONSOLE);
        LogHandler.debug("Starting BuildTools");
        BuildToolsProject task = new BuildToolsProject( this, buildToolsSettings );
        try {
            task.call();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public void setFinished() {
        running = false;
        controller.onBuildToolsFinished(this);
    }
}
