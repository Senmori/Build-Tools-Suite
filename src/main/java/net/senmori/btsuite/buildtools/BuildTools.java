package net.senmori.btsuite.buildtools;

import com.google.common.collect.Lists;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Data;
import net.senmori.btsuite.Builder;
import net.senmori.btsuite.WindowTab;
import net.senmori.btsuite.controllers.BuildTabController;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.util.LogHandler;

import java.util.List;

@Data
public final class BuildTools implements Runnable {
    private final BuildTabController controller;

    private BooleanProperty runningProperty = new SimpleBooleanProperty( false );
    BuildToolsSettings buildToolsSettings = BuildToolsSettings.getInstance();
    private boolean disableCertificateCheck = false;
    private boolean dontUpdate = false;
    private boolean skipCompile = false;
    private boolean genSrc = false;
    private boolean genDoc = false;
    private String version = BuildToolsSettings.getInstance().getDefaultVersion();
    private List<String> outputDirectories = Lists.newArrayList();


    public BuildTools(BuildTabController controller) {
        this.controller = controller;
    }

    public void setOutputDirectories(List<String> directories) {
        this.outputDirectories.clear();
        this.outputDirectories.addAll(directories);
    }

    public boolean isRunning() {
        return runningProperty.get();
    }

    public void setRunning(boolean value) {
        runningProperty.set( value );
    }

    @Override
    public void run() {
        setRunning( true );
        Builder.setActiveTab(WindowTab.CONSOLE);
        LogHandler.debug( "Starting BuildToolsSuite" );
        BuildToolsProject task = new BuildToolsProject( this, buildToolsSettings );
        try {
            task.call();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public void setFinished() {
        setRunning( true );
    }
}
