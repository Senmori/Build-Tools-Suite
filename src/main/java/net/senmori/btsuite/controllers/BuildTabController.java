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

package net.senmori.btsuite.controllers;

import com.google.common.collect.Lists;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.WindowTab;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.buildtools.BuildTools;
import net.senmori.btsuite.buildtools.SpigotVersion;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.task.BuildToolsTask;
import net.senmori.btsuite.task.InvalidateCacheTask;
import net.senmori.btsuite.task.SpigotVersionImportTask;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.LogHandler;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class BuildTabController {
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private VBox flagBox;
    @FXML
    private CheckBox certCheck;
    @FXML
    private CheckBox dontUpdate;
    @FXML
    private CheckBox skipCompile;
    @FXML
    private CheckBox genSrc;
    @FXML
    private CheckBox genDoc;
    @FXML
    private Button runBuildToolsBtn;
    @FXML
    private AnchorPane outputAnchorPane;
    @FXML
    private Button addOutputDirBtn;
    @FXML
    private Button delOutputBtn;
    @FXML
    private ListView<String> outputDirListView;
    @FXML
    private ComboBox<String> choiceComboBox;
    @FXML
    private CheckBox buildInvalidateCache;
    @FXML
    private CheckBox updateVersionCheckBox;
    @FXML
    private Button updateVersionsBtn;

    private final BuildTools buildTools;
    private final BuildToolsSettings settings;
    // used as a proxy to run build tools without injecting the controller into BuildTools
    private final BooleanProperty runBuildToolsProxyProperty = new SimpleBooleanProperty( this, "runBuildToolsProxy", false );
    // used as a proxy to re-import versions without injecting the controller into BuildTools
    private final BooleanProperty reimportVersionsProperty = new SimpleBooleanProperty( this, "reimportVersionsProperty", false );

    public BuildTabController(BuildTools tools) {
        this.buildTools = tools;
        this.settings = tools.getSettings();
    }

    @FXML
    void initialize() {
        outputDirListView.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
        choiceComboBox.setVisibleRowCount( 10 );

        updateVersionsBtn.managedProperty().bind( updateVersionsBtn.visibleProperty() );
        updateVersionsBtn.visibleProperty().bind( updateVersionCheckBox.selectedProperty() );

        runBuildToolsProxyProperty.bindBidirectional( buildTools.getRunBuildToolsProperty() );
        reimportVersionsProperty.bindBidirectional( buildTools.getReimportVersions() );

        runBuildToolsProxyProperty.addListener( (observable, oldValue, newValue) -> {
            if ( newValue == true ) {
                // run build tools
                runBuildToolsBtn.fire();
            }
            runBuildToolsProxyProperty.set( false );
        } );
        reimportVersionsProperty.addListener( (observable, oldValue, newValue) -> {
            if ( newValue == true ) {
                updateVersionsBtn.fire();
            }
            reimportVersionsProperty.set( false );
        } );

        runBuildToolsBtn.textProperty().bind( Bindings.when( buildInvalidateCache.selectedProperty() )
                                                      .then( "Invalidate Cache" )
                                                      .otherwise( "Run Build Tools Suite" )
        );
        runBuildToolsBtn.textProperty().addListener( (observable, oldValue, newValue) -> {
            runBuildToolsBtn.getParent().layout();
            runBuildToolsBtn.getParent().applyCss();
        } );

        choiceComboBox.setConverter( new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return ( ( object == null ) || object.trim().isEmpty() ) ? settings.getDefaultVersion() : object.toString();
            }

            @Override
            public String fromString(String string) {
                return ( ( string == null ) || string.trim().isEmpty() ) ? settings.getDefaultVersion() : string;
            }
        } );

        // bind everything to BuildTools

        // Disable run button if Build Tools is currently running, or we are not initialized
        runBuildToolsBtn.disableProperty().bind( buildTools.getRunningProperty() );

        certCheck.selectedProperty().bindBidirectional( buildTools.getDisableCertificateCheckProperty() );
        dontUpdate.selectedProperty().bindBidirectional( buildTools.getDontUpdateProperty() );
        skipCompile.selectedProperty().bindBidirectional( buildTools.getSkipCompileProperty() );
        genSrc.selectedProperty().bindBidirectional( buildTools.getGenSourceProperty() );
        genDoc.selectedProperty().bindBidirectional( buildTools.getGenDocumentationProperty() );
        buildInvalidateCache.selectedProperty().bindBidirectional( buildTools.getInvalidateCacheProperty() );

        updateVersionCheckBox.selectedProperty().bindBidirectional( buildTools.getUpdateVersionsProperty() );
        choiceComboBox.valueProperty().bindBidirectional( buildTools.getVersionProperty() );

        if ( outputDirListView.getItems().isEmpty() ) {
            outputDirListView.getItems().add( buildTools.getWorkingDirectory().getFile().getAbsolutePath() );
        }
        importVersions();
    }


    @FXML
    void onInvalidateCacheBtn(ActionEvent event) {
        buildTools.setInvalidateCache( this.buildInvalidateCache.isSelected() );
    }

    @FXML
    void onCertCheckClicked(ActionEvent event) {
        buildTools.setDisableCertificateCheck( this.certCheck.isSelected() );
    }

    @FXML
    void onDontUpdateClicked(ActionEvent event) {
        buildTools.setDontUpdate( this.dontUpdate.isSelected() );
    }

    @FXML
    void onSkipCompileClicked(ActionEvent event) {
        buildTools.setSkipCompile( this.skipCompile.isSelected() );
    }

    @FXML
    void onGenSrcClicked(ActionEvent event) {
        buildTools.setGenSource( this.genSrc.isSelected() );
    }

    @FXML
    void onGenDocClicked(ActionEvent event) {
        buildTools.setGenDocumentation( this.genDoc.isSelected() );
    }

    @FXML
    void onAddOutputDirClicked(ActionEvent event) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory( settings.getDirectories().getWorkingDir().getFile() );
        dirChooser.setTitle( "Add output directory" );
        File output = dirChooser.showDialog( Main.getWindow() );
        if ( FileUtil.isDirectory( output ) ) {
            this.outputDirListView.getItems().add( output.getAbsolutePath() );
            settings.getRecentOutputDirectories().add( output.getAbsolutePath() );
        }
    }

    @FXML
    void onDelOutputDirClicked(ActionEvent event) {
        ObservableList<String> selected = this.outputDirListView.getSelectionModel().getSelectedItems();
        ObservableList<String> all = this.outputDirListView.getItems();
        all.removeAll( selected );
        this.outputDirListView.setItems( all );
        settings.getRecentOutputDirectories().clear();
        settings.getRecentOutputDirectories().addAll( all );

        if ( outputDirListView.getItems().size() == 1 ) {
            if ( outputDirListView.getItems().get( 0 ).equalsIgnoreCase( buildTools.getWorkingDirectory().getFile().getAbsolutePath() ) ) {
                delOutputBtn.setDisable( true );
                return;
            }
        }
        delOutputBtn.setDisable( false );
    }

    @FXML
    void onUpdateVersionsBtn(ActionEvent event) {
        Main.setActiveTab( WindowTab.CONSOLE );
        invalidateVersions();
    }

    @FXML
    void onRunBuildToolsClicked(ActionEvent event) {
        // Invalidate cache
        if ( buildInvalidateCache.isSelected() ) {
            // invalidate cache and then make them run BTS again to prevent any file errors
            InvalidateCacheTask task = new InvalidateCacheTask( buildTools, buildTools.getVersionManifest() );
            task.setOnRunning( (worker) -> {
                buildTools.setRunning( true );
                buildTools.getConsole().setProgressText( "Invalidating Cache" );
            } );
            task.setOnSucceeded( (worker) -> {
                buildInvalidateCache.setSelected( false );
                buildTools.setRunning( false );
                buildTools.getConsole().reset();
            } );

            task.setOnFailed( (worker) -> {
                buildInvalidateCache.setSelected( false );
                buildTools.setRunning( false );
                buildTools.getConsole().reset();
            } );

            task.setOnCancelled( (worker) -> {
                buildInvalidateCache.setSelected( false );
                buildTools.setRunning( false );
                buildTools.getConsole().reset();
            } );

            Main.setActiveTab( WindowTab.CONSOLE );
            TaskPools.getSinglePool().submit( task );
            return;
        }

        // Run Build Tools
        if ( !buildTools.isRunning() ) {
            if ( choiceComboBox.getSelectionModel().getSelectedItem() == null ) {
                buildTools.setVersion( settings.getDefaultVersion() );
            } else {
                buildTools.setVersion( choiceComboBox.getSelectionModel().getSelectedItem().toLowerCase() );
            }
            buildTools.getOutputDirectories().addAll( outputDirListView.getItems() );

            BuildToolsTask task = new BuildToolsTask( buildTools );
            task.setOnRunning( (worker) -> {
                buildTools.setRunning( true );
                buildTools.getConsole().reset();
                buildTools.getConsole().setProgressText( "Progress" );
            } );
            task.setOnSucceeded( (worker) -> {
                long seconds = task.getValue();
                String formatted = String.format( "%d:%02d", seconds / 60L, seconds % 60L );
                LogHandler.info( "It took " + formatted + " to complete this build." );
                LogHandler.info( "BuildToolsSuite has finished!" );
                buildTools.setRunning( false );
                buildTools.getConsole().reset();
            } );
            task.setOnCancelled( (worker) -> {
                buildTools.setRunning( false );
                buildTools.getConsole().reset();
                LogHandler.error( "BuildToolsTask was cancelled!" );
                LogHandler.error( worker.getSource().getException().getLocalizedMessage() );
            } );
            task.setOnFailed( (worker) -> {
                buildTools.setRunning( false );
                buildTools.getConsole().reset();
                LogHandler.error( "BuildToolsTask failed!" );
                LogHandler.error( worker.getSource().getException().getLocalizedMessage() );
            } );


            Main.setActiveTab( WindowTab.CONSOLE );
            TaskPools.getSinglePool().submit( task );
        }
    }


    public void invalidateVersions() {
        // ensure we're on the main thread, in case this is called from somewhere else
        Platform.runLater( () -> {
            updateVersionCheckBox.setSelected( false );
        } );

        buildTools.getConsole().reset();
        TaskPools.submit( () -> {

            BuildToolsSettings.Directories dirs = settings.getDirectories();
            File versionsDir = new File( dirs.getVersionsDir().getFile(), "spigot" );
            LogHandler.info( "Deleting " + versionsDir + '.' );
            //FileUtil.deleteFilesInDirectory( versionsDir, (name) -> name.contains( ".json" ) );
            //buildTools.getConsole().reset();
            buildTools.getConsole().setOptionalText( "Re-importing spigot versions..." );
            LogHandler.info( "Re-importing spigot versions..." );
            importVersions();
        } );
    }

    public Task importVersions() {
        SpigotVersionImportTask task = new SpigotVersionImportTask( buildTools );
        task.setOnRunning( (worker) -> {
            buildTools.getConsole().reset();
            buildTools.getConsole().setProgressText( "Importing Spigot Versions" );
        } );
        task.setOnSucceeded( (worker) -> {
            Map<SpigotVersion, BuildInfo> map = task.getValue();
            handleVersionMap( map );
            LogHandler.info( "Loaded " + map.keySet().size() + " Spigot versions." );
            buildInvalidateCache.setSelected( false );
            buildTools.setRunning( false );
            buildTools.getConsole().reset();
        } );

        task.setOnFailed( (worker) -> {
            LogHandler.info( "Failed SpigotImportVersions task." );
            LogHandler.info( worker.getEventType().toString() + ": " + worker.getSource().getException().getMessage() );
            updateVersionCheckBox.setSelected( false );
            buildTools.setRunning( false );
            buildTools.getConsole().reset();
        } );
        task.setOnCancelled( (worker) -> {
            LogHandler.info( "Import Spigot Versions task cancelled." );
            updateVersionCheckBox.setSelected( false );
            buildTools.setRunning( false );
            buildTools.getConsole().reset();
        } );
        TaskPools.getSinglePool().submit( task );
        return task;
    }

    private boolean handleVersionMap(Map<SpigotVersion, BuildInfo> map) {
        choiceComboBox.getItems().clear();
        List<SpigotVersion> versions = Lists.newArrayList( map.keySet() );
        versions.sort( SpigotVersion::compareTo );
        versions = Lists.reverse( versions );
        for ( SpigotVersion ver : versions ) {
            this.choiceComboBox.getItems().add( ver.getVersionString() );
        }
        choiceComboBox.getSelectionModel().select( 0 ); // select newest version
        return true;
    }
}
