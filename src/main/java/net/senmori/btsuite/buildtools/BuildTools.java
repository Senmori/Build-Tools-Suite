package net.senmori.btsuite.buildtools;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.senmori.btsuite.version.Version;

import java.io.File;
import java.util.List;
import java.util.Map;

public final class BuildTools {

    public static File portableGitDir = null;

    public boolean disableCertificateCheck = false;
    public boolean dontUpdate = false;
    public boolean skipCompile = false;
    public boolean genSrc = false;
    public boolean genDoc = false;

    public String version = "latest";

    List<String> outputDirectories = Lists.newArrayList();

    Map<Version, BuildInfo> versionMap = Maps.newHashMap();

    public void setVersionMap(Map<Version, BuildInfo> newMap) {
        this.versionMap.clear();
        this.versionMap.putAll(newMap);
    }

    public void setOutputDirectories(List<String> directories) {
        this.outputDirectories.clear();
        this.outputDirectories.addAll(directories);
    }
}
