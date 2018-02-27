package net.senmori.btsuite.buildtools;

import net.senmori.btsuite.Controller;

import java.util.ArrayList;
import java.util.List;

public class ProjectBuilder {


    public boolean genDocs = false;
    public boolean genSrc = false;
    public boolean skipCompile = false;
    public boolean dontUpdate = false;
    public boolean disableCertCheck = false;

    public String version = "latest";

    public List<String> outputDir = new ArrayList<>();

    private Controller controller;

    public ProjectBuilder(Controller controller) {
        this.controller = controller;
    }


    public boolean isGenDocs() {
        return genDocs;
    }

    public void setGenDocs(boolean genDocs) {
        this.genDocs = genDocs;
    }

    public boolean isGenSrc() {
        return genSrc;
    }

    public void setGenSrc(boolean genSrc) {
        this.genSrc = genSrc;
    }

    public boolean isSkipCompile() {
        return skipCompile;
    }

    public void setSkipCompile(boolean skipCompile) {
        this.skipCompile = skipCompile;
    }

    public boolean isDontUpdate() {
        return dontUpdate;
    }

    public void setDontUpdate(boolean dontUpdate) {
        this.dontUpdate = dontUpdate;
    }

    public boolean isDisableCertCheck() {
        return disableCertCheck;
    }

    public void setDisableCertCheck(boolean disableCertCheck) {
        this.disableCertCheck = disableCertCheck;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(List<String> outputDir) {
        this.outputDir = outputDir;
    }
}
