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
import net.senmori.btsuite.Builder;
import net.senmori.btsuite.Callback;
import net.senmori.btsuite.WindowTab;
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

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

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

    @FXML
    void onDownloadServerBtn(ActionEvent event) {
        if ( currentVersionProperty.get() != null ) {
            MinecraftVersion workingVersion = currentVersionProperty.get();
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle( "Choose Destination:" );
            chooser.setInitialDirectory( Builder.WORKING_DIR.getFile() );
            File directory = chooser.showDialog( Builder.getWindow() );
            File serverFile = new File( directory, "minecraft_server-" + workingVersion.getVersion() + ".jar" );
            try {
                FileDownloadTask task = new FileDownloadTask( workingVersion.getServerDownloadURL(), serverFile );
                TaskPools.submit( task );
                serverFile = task.get();
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            } catch ( ExecutionException e ) {
                e.printStackTrace();
            }
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
        Builder.setActiveTab( WindowTab.CONSOLE );
        boolean invalidated = VersionManifest.getInstance().invalidateCache();
        if ( ! invalidated ) {
            LogHandler.error( "*** Unable to delete \'version_manifest.json\'! *** " );
            LogHandler.error( "*** Aborting invalidation procedure. ***" );
            return;
        }
        versionMap.clear();
        currentReleaseTypeProperty.set( null );
        currentVersionProperty.set( null );
        importVersions();
    }

    @FXML
    void onUpdateVersions(ActionEvent event) {

    }

    private Map<ReleaseType, ObservableList<MinecraftVersion>> versionMap = Maps.newHashMap();
    private SimpleObjectProperty<ReleaseType> currentReleaseTypeProperty = new SimpleObjectProperty<>( ReleaseType.RELEASE );
    private SimpleObjectProperty<MinecraftVersion> currentVersionProperty = new SimpleObjectProperty<>( this, "CurrentMinecraftVersion", null );

    private final VersionManifest manifest = VersionManifest.getInstance();
    @FXML
    void initialize() {
        updateVersionsBtn.managedProperty().bind( updateVersionsBtn.visibleProperty() );
        updateVersionsBtn.visibleProperty().bind( updateMCVersionsCheckBox.selectedProperty() );

        downloadServerBtn.disableProperty().bind( Bindings.isNull( currentVersionProperty ) );

        currentReleaseTypeProperty.bind( releaseTypeComboBox.getSelectionModel().selectedItemProperty() ); // bind selected ReleaseType to our object
        releaseTypeComboBox.getSelectionModel().selectedItemProperty().addListener( ( (observable, oldValue, newValue) -> {
            if ( currentReleaseTypeProperty.get() == null ) {
                releaseTypeComboBox.getSelectionModel().select( ReleaseType.RELEASE );
                versionComboBox.setItems( FXCollections.emptyObservableList() );
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

        Builder.getInstance().setController( WindowTab.MINECRAFT, this );
        GitInstaller git = new GitInstaller();
        git.setOnSucceeded( (worker) -> {
            TaskPools.execute( new GitConfigurationTask() );
        } );
        TaskPools.execute( git );
        TaskPools.execute( new MavenInstaller() );

        importVersions();
    }

    public Task importVersions() {
        versionMap.clear();
        releaseTypeComboBox.setItems( FXCollections.emptyObservableList() );
        versionComboBox.setItems( FXCollections.emptyObservableList() );
        if ( updateMCVersionsCheckBox.isSelected() ) {
            updateMCVersionsCheckBox.setSelected( false );
        }

        Callback<Collection<MinecraftVersion>> callback = new Callback<Collection<MinecraftVersion>>() {
            @Override
            public void accept(Collection<MinecraftVersion> value) {
                manifest.setAvailableVersions( value );
                for ( ReleaseType type : ReleaseType.values() ) {
                    Collection<MinecraftVersion> list = manifest.getByReleaseType( type );
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
            }
        };

        String url = BuildToolsSettings.getInstance().getMinecraftVersionManifestURL();
        ImportMinecraftVersionTask task = new ImportMinecraftVersionTask( url );
        Console.getInstance().registerTask( task, "Importing Minecraft Versions", callback, true );

        return task;
    }

    private void initialSettings() {
        releaseTypeComboBox.getSelectionModel().select( ReleaseType.RELEASE );
        versionComboBox.setItems( versionMap.get( currentReleaseTypeProperty.get() ) );
        versionComboBox.getSelectionModel().select( 0 ); // also sets our currentVersionProperty
    }
}
