/*
 * Copyright (c) 2018, Senmori. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package net.senmori.btsuite.buildtools;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import lombok.Getter;
import net.senmori.btsuite.Console;
import net.senmori.btsuite.minecraft.VersionManifest;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.Directory;
import net.senmori.btsuite.util.LogHandler;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Controls all functionality related to the building of Spigot jars.
 */
public class BuildTools {


    @Getter
    private final Directory workingDirectory;
    @Getter
    private final BuildToolsSettings settings;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final BooleanProperty initializedProperty = new SimpleBooleanProperty( this, "initialized", false );
    private final BooleanProperty runBuildToolsProperty = new SimpleBooleanProperty( this, "runBuildTools", false );
    private final BooleanProperty reimportVersions = new SimpleBooleanProperty( this, "reimportVersions", false );

    private final BooleanProperty runningProperty = new SimpleBooleanProperty( this, "running", false );
    private final BooleanProperty disableCertificateCheck = new SimpleBooleanProperty( this, "disableCertificateCheck", false );
    private final BooleanProperty dontUpdateProperty = new SimpleBooleanProperty( this, "dontUpdateProperty", false );
    private final BooleanProperty skipCompileProperty = new SimpleBooleanProperty( this, "skipCompileProperty", false );
    private final BooleanProperty genSourceProperty = new SimpleBooleanProperty( this, "genSourceProperty", false );
    private final BooleanProperty genDocumentationProperty = new SimpleBooleanProperty( this, "genDocumentationProperty", false );
    private final BooleanProperty invalidateCacheProperty = new SimpleBooleanProperty( this, "invalidateCacheProperty", false );

    public final BooleanProperty updateVersionsProperty = new SimpleBooleanProperty( this, "updateVersions", false );

    private final StringProperty versionProperty;
    private final ObservableSet<String> outputDirectories;

    @Getter
    private final Console console;
    @Getter
    private final VersionManifest versionManifest;


    public BuildTools(Directory workingDirectory, BuildToolsSettings settings, VersionManifest versionManifest, Console console) {
        this.workingDirectory = workingDirectory;
        this.settings = settings;
        this.versionManifest = versionManifest;
        this.console = console;

        versionProperty = new SimpleStringProperty( this, "version", settings.getDefaultVersion() );
        versionProperty.set( this.settings.getDefaultVersion() );
        outputDirectories = FXCollections.observableSet( workingDirectory.getFile().getAbsolutePath() );

        // must always have at least one output directory
        outputDirectories.addListener( new SetChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
                if ( change.wasRemoved() && change.getSet().isEmpty() ) {
                    outputDirectories.add( workingDirectory.getFile().getAbsolutePath() );
                }
            }
        } );
        // default to the specified default version
        versionProperty.addListener( (observable, oldValue, newValue) -> {
            if ( ( newValue == null ) || newValue.trim().isEmpty() ) {
                versionProperty.set( this.settings.getDefaultVersion() );
            }
        } );
    }

    public void invalidateCache() {
        Platform.runLater( () -> {
            this.initializedProperty.set( false );
        } );

    }

    private void delete(File file) {
        if ( file.isDirectory() ) {
            for ( File in : file.listFiles() ) {
                if ( in.isDirectory() ) {
                    delete( in );
                } else {
                    console.setOptionalText( FilenameUtils.getBaseName( in.getName() ) );
                    try {
                        FileDeleteStrategy.FORCE.delete( in );
                    } catch ( NullPointerException | IOException e ) {
                        LogHandler.error( "** Could not delete " + in.getPath() + " **" );
                    }
                }
            }
        }
        console.setOptionalText( "Deleting " + FilenameUtils.getBaseName( file.getName() ) );
        try {
            FileDeleteStrategy.FORCE.delete( file );
        } catch ( NullPointerException | IOException e ) {
            LogHandler.error( "** Could not delete " + file.getPath() + " **" );
        }
    }


    public BooleanProperty getInitializedProperty() {
        return initializedProperty;
    }

    public void setInitialized(boolean value) {
        initializedProperty.set( value );
    }

    public boolean isInitialized() {
        return initializedProperty.get();
    }

    public BooleanProperty getRunBuildToolsProperty() {
        return runBuildToolsProperty;
    }

    public void runBuildTools() {
        runBuildToolsProperty.set( true );
    }

    public BooleanProperty getReimportVersions() {
        return reimportVersions;
    }

    public void importVersions() {
        reimportVersions.set( true );
    }

    public BooleanProperty getRunningProperty() {
        return runningProperty;
    }

    public void setRunning(boolean running) {
        this.runningProperty.set( running );
    }

    public boolean isRunning() {
        return runningProperty.get();
    }

    public BooleanProperty getDisableCertificateCheckProperty() {
        return disableCertificateCheck;
    }

    public void setDisableCertificateCheck(boolean value) {
        disableCertificateCheck.set( value );
    }

    public boolean isDisableCertificateCheck() {
        return disableCertificateCheck.get();
    }

    public BooleanProperty getDontUpdateProperty() {
        return dontUpdateProperty;
    }

    public void setDontUpdate(boolean value) {
        dontUpdateProperty.setValue( value );
    }

    public boolean isDontUpdate() {
        return dontUpdateProperty.get();
    }

    public BooleanProperty getSkipCompileProperty() {
        return skipCompileProperty;
    }

    public void setSkipCompile(boolean value) {
        skipCompileProperty.set( value );
    }

    public boolean isSkipCompile() {
        return skipCompileProperty.get();
    }

    public BooleanProperty getGenSourceProperty() {
        return genSourceProperty;
    }

    public void setGenSource(boolean value) {
        genSourceProperty.set( value );
    }

    public boolean isGenSource() {
        return genSourceProperty.get();
    }

    public BooleanProperty getGenDocumentationProperty() {
        return genDocumentationProperty;
    }

    public void setGenDocumentation(boolean value) {
        genDocumentationProperty.set( value );
    }

    public boolean isGenDocumentation() {
        return genDocumentationProperty.get();
    }

    public BooleanProperty getInvalidateCacheProperty() {
        return invalidateCacheProperty;
    }

    public void setInvalidateCache(boolean value) {
        invalidateCacheProperty.set( value );
    }

    public boolean isInvalidateCache() {
        return invalidateCacheProperty.get();
    }

    public BooleanProperty getUpdateVersionsProperty() {
        return updateVersionsProperty;
    }

    public void setUpdateVersions(boolean value) {
        updateVersionsProperty.set( value );
    }

    public boolean isUpdateVersions() {
        return updateVersionsProperty.get();
    }

    public StringProperty getVersionProperty() {
        return versionProperty;
    }

    public void setVersion(String version) {
        versionProperty.setValue( version );
    }

    public String getVersion() {
        return versionProperty.get();
    }

    public ObservableSet<String> getOutputDirectories() {
        return outputDirectories;
    }

    public boolean addOutputDirectory(String directory) {
        return outputDirectories.add( directory );
    }

    public boolean addOutputDirectory(File directory) {
        return outputDirectories.add( directory.getAbsolutePath() );
    }

    public boolean addOutputDirectory(Directory directory) {
        return outputDirectories.add( directory.getFile().getAbsolutePath() );
    }

    public void clearDirectories() {
        outputDirectories.clear();
    }
}
