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

import com.google.common.collect.Maps;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import net.senmori.btsuite.Console;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.WindowTab;
import net.senmori.btsuite.buildtools.BuildTools;
import net.senmori.btsuite.minecraft.MinecraftVersion;
import net.senmori.btsuite.minecraft.ReleaseType;
import net.senmori.btsuite.minecraft.VersionManifest;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.task.FileDownloadTask;
import net.senmori.btsuite.task.GitConfigurationTask;
import net.senmori.btsuite.task.GitInstaller;
import net.senmori.btsuite.task.ImportMinecraftVersionTask;
import net.senmori.btsuite.task.MavenInstaller;
import net.senmori.btsuite.util.LogHandler;
import org.apache.commons.io.FileDeleteStrategy;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;

public class MinecraftTabController {

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private Button downloadServerBtn;
    @FXML
    private ComboBox<ReleaseType> releaseTypeComboBox;
    @FXML
    private ComboBox<MinecraftVersion> versionComboBox;
    @FXML
    private TextField releaseDateTextField;
    @FXML
    private TextField SHA1TextField;
    @FXML
    private CheckBox updateMCVersionsCheckBox;
    @FXML
    private Button updateVersionsBtn;

    private final Console console;
    private final VersionManifest versionManifest;
    private final BuildTools buildTools;
    private Map<ReleaseType, ObservableList<MinecraftVersion>> versionMap = Maps.newHashMap();
    private SimpleObjectProperty<ReleaseType> currentReleaseTypeProperty = new SimpleObjectProperty<>( ReleaseType.RELEASE );
    private SimpleObjectProperty<MinecraftVersion> currentVersionProperty = new SimpleObjectProperty<>( this, "CurrentMinecraftVersion", null );
    private BooleanProperty importVersionsProxy = new SimpleBooleanProperty( this, "importVersionsProxy", false );

    public MinecraftTabController(Console console, VersionManifest versionManifest, BuildTools buildTools) {
        this.console = console;
        this.versionManifest = versionManifest;
        this.buildTools = buildTools;
    }


    @FXML
    void initialize() {
        updateVersionsBtn.managedProperty().bind( updateVersionsBtn.visibleProperty() );
        updateVersionsBtn.visibleProperty().bind( updateMCVersionsCheckBox.selectedProperty() );

        downloadServerBtn.disableProperty().bind( Bindings.isNull( currentVersionProperty ) );
        importVersionsProxy.bindBidirectional( versionManifest.getImportVersionsProxyProperty() );
        importVersionsProxy.addListener( (observable, oldValue, newValue) -> {
            if ( newValue == true ) {
                updateVersionsBtn.fire();
            }
            importVersionsProxy.set( false );
        } );

        currentReleaseTypeProperty.bind( releaseTypeComboBox.getSelectionModel().selectedItemProperty() ); // bind selected ReleaseType to our object
        releaseTypeComboBox.getSelectionModel().selectedItemProperty().addListener( ( (observable, oldValue, newValue) -> {
            if ( currentReleaseTypeProperty.get() == null ) {
                releaseTypeComboBox.getSelectionModel().clearSelection();
                versionComboBox.getItems().clear();
            } else {
                versionComboBox.setItems( versionMap.get( newValue ) );
                versionComboBox.getSelectionModel().select( 0 );
            }
        } ) );
        releaseTypeComboBox.setConverter( new StringConverter<ReleaseType>() {
            @Override
            public String toString(ReleaseType object) {
                return ( object == null ) ? ReleaseType.RELEASE.getFormattedName() : object.getFormattedName();
            }

            @Override
            public ReleaseType fromString(String string) {
                return ( string == null || string.trim().isEmpty() ) ? ReleaseType.RELEASE : ReleaseType.getByFormattedName( string );
            }
        } );

        currentVersionProperty.bind( versionComboBox.getSelectionModel().selectedItemProperty() ); // bind current selected item to our object
        versionComboBox.getSelectionModel().selectedItemProperty().addListener( ( (observable, oldValue, newValue) -> {
            if ( currentVersionProperty.get() == null ) { // removing the value
                releaseDateTextField.clear();
                SHA1TextField.clear();
            } else {
                // i.e. Tue, 3 Jun 2008 11:05:30 GMT
                releaseDateTextField.setText( currentVersionProperty.get().getReleaseDate().toString() );
                SHA1TextField.setText( currentVersionProperty.get().getSHA_1() );
            }
        } ) );

        GitInstaller git = new GitInstaller( buildTools.getSettings() );
        git.setOnSucceeded( (worker) -> {
            TaskPools.submit( new GitConfigurationTask( buildTools.getSettings() ) );
        } );
        TaskPools.submit( git );
        TaskPools.submit( new MavenInstaller( buildTools.getSettings() ) );
        importVersions();
    }


    @FXML
    void onDownloadServerBtn(ActionEvent event) {
        if ( currentVersionProperty.get() != null ) {
            MinecraftVersion workingVersion = currentVersionProperty.get();
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle( "Choose Destination" );
            chooser.setInitialDirectory( Main.WORKING_DIR.getFile() );
            File directory = chooser.showDialog( Main.getWindow() );
            File serverFile = new File( directory, "minecraft_server-" + workingVersion.getVersion() + ".jar" );

            FileDownloadTask task = new FileDownloadTask( workingVersion.getServerDownloadURL(), serverFile );
            task.setOnSucceeded( (worker) -> {
                try {
                    Runtime.getRuntime().exec( "explorer.exe /select," + task.getValue().getAbsolutePath() );
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            } );
            Main.setActiveTab( WindowTab.CONSOLE );
            LogHandler.info( "Your file is being downloaded to " + directory.getAbsolutePath() + ". A window will open when the download is complete!" );
            TaskPools.submit( task );
        }
    }

    @FXML
    void onReleaseTypeCombo(ActionEvent event) {

    }

    @FXML
    void onVersionComboBox(ActionEvent event) {
        if ( currentVersionProperty.get() != null ) {
            releaseDateTextField.setText( currentVersionProperty.get().getReleaseDate().toString() );
            SHA1TextField.setText( currentVersionProperty.get().getSHA_1() );
        } else {
            releaseDateTextField.clear();
            SHA1TextField.clear();
        }
        releaseDateTextField.requestLayout();
        SHA1TextField.requestLayout();
    }

    @FXML
    void onUpdateVersionBtn(ActionEvent event) {
        Main.setActiveTab( WindowTab.CONSOLE );
        TaskPools.submit( () -> {
            // Invalidate cache, if it exists
            BuildToolsSettings settings = buildTools.getSettings();
            BuildToolsSettings.Directories dirs = settings.getDirectories();
            File versionsDir = new File( dirs.getVersionsDir().getFile(), "minecraft" );
            if ( versionsDir.exists() && versionsDir.isDirectory() ) {
                File manifestFile = new File( versionsDir, "version_manifest.json" );
                if ( manifestFile.exists() && manifestFile.isFile() ) {
                    try {
                        FileDeleteStrategy.FORCE.delete( manifestFile );
                    } catch ( IOException e ) {
                        LogHandler.error( "*** Unable to delete version_manifest file! ***" );
                        return;
                    }
                }
            }
        } );
        versionMap.clear();
        importVersions();
    }

    @FXML
    void onUpdateVersions(ActionEvent event) {

    }


    public Task importVersions() {
        // ensure we're on the main thread
        String url = buildTools.getSettings().getMinecraftVersionManifestURL();
        ImportMinecraftVersionTask task = new ImportMinecraftVersionTask( buildTools );
        task.setOnRunning( (worker) -> {
            console.reset();
            console.setProgressText( "Importing Minecraft Versions" );
        } );
        task.setOnSucceeded( (worker) -> {
            versionMap.clear();
            releaseTypeComboBox.getItems().clear();
            versionComboBox.getItems().clear();
            updateMCVersionsCheckBox.setSelected( false );

            versionManifest.setAvailableVersions( task.getValue() );
            for ( ReleaseType type : ReleaseType.values() ) {
                Collection<MinecraftVersion> list = versionManifest.getVersionsByReleaseType( type );
                int size = list.size();
                ObservableList<MinecraftVersion> observableList = FXCollections.observableArrayList( list );
                versionMap.put( type, observableList );
                LogHandler.info( "Found " + size + " versions for " + type );
            }
            ObservableList<ReleaseType> types = FXCollections.observableArrayList( ReleaseType.values() );
            releaseTypeComboBox.setItems( types );
            versionComboBox.setItems( versionMap.get( currentReleaseTypeProperty.get() ) );
            // versions loaded set initial values
            initialSettings();
            console.reset();
        } );
        task.setOnCancelled( (worker) -> {
            //downloadServerBtn.setDisable( true );
            updateMCVersionsCheckBox.setSelected( true );
            console.reset();
        } );
        task.setOnFailed( (worker) -> {
            //downloadServerBtn.setDisable( true );
            updateMCVersionsCheckBox.setSelected( true );
            console.reset();
        } );

        TaskPools.getSinglePool().submit( task );
        return task;
    }

    private void initialSettings() {
        releaseTypeComboBox.getSelectionModel().select( ReleaseType.RELEASE );
        versionComboBox.setItems( versionMap.get( currentReleaseTypeProperty.get() ) );
        versionComboBox.getSelectionModel().select( 0 ); // also sets our currentVersionProperty
    }
}
