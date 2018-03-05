package net.senmori.btsuite.buildtools;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.version.Version;

import java.io.File;
import java.util.List;
import java.util.Map;

@Data
public final class BuildTools {
    private boolean disableCertificateCheck = false;
    private boolean dontUpdate = false;
    private boolean skipCompile = false;
    private boolean genSrc = false;
    private boolean genDoc = false;

    private String version = "latest";

    private List<String> outputDirectories = Lists.newArrayList();

    private Map<Version, BuildInfo> versionMap = Maps.newHashMap();

    public void setVersionMap(Map<Version, BuildInfo> newMap) {
        this.versionMap.clear();
        this.versionMap.putAll(newMap);
    }

    public void setOutputDirectories(List<String> directories) {
        this.outputDirectories.clear();
        this.outputDirectories.addAll(directories);
    }

    public void addOutputDirectory(File directory) {
        if(FileUtil.isDirectory(directory))
            outputDirectories.add(directory.getAbsolutePath());
    }
}
