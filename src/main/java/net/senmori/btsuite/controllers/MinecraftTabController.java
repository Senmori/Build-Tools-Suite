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

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import net.senmori.btsuite.Builder;
import net.senmori.btsuite.WindowTab;
import net.senmori.btsuite.minecraft.MinecraftVersion;
import net.senmori.btsuite.minecraft.ReleaseType;
import net.senmori.btsuite.minecraft.VersionManifest;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.task.FileDownloadTask;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MinecraftTabController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button downloadServerBtn;

    @FXML
    private ComboBox<String> releaseTypeComboBox;

    @FXML
    private ComboBox<String> versionComboBox;

    @FXML
    private TextField releaseDateTextField;

    @FXML
    private TextField SHA1TextField;

    @FXML
    private CheckBox updateMCVersionsCheckBox;

    @FXML
    private Button updateVersionsBtn;

    Map<ReleaseType, ObservableList<String>> versionMap = new HashMap<>();
    SimpleObjectProperty<ReleaseType> currentReleaseTypeProperty = new SimpleObjectProperty<>( ReleaseType.RELEASE );
    SimpleObjectProperty<MinecraftVersion> currentVersionProperty = new SimpleObjectProperty<>( null );

    @FXML
    void onDownloadServerBtn(ActionEvent event) {
        if ( currentVersionProperty.get() != null ) {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle( "Choose Destination:" );
            chooser.setInitialDirectory( Builder.WORKING_DIR.getFile() );
            File directory = chooser.showDialog( Builder.getWindow() );
            File serverFile = new File( directory, "minecraft_server-" + currentVersionProperty.get().getVersion() + ".jar" );
            try {
                File file = TaskPools.submit( new FileDownloadTask( currentVersionProperty.get().getServerDownloadURL(), serverFile ) ).get();
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            } catch ( ExecutionException e ) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void onReleaseTypeCombo(ActionEvent event) {
        currentReleaseTypeProperty.set( ReleaseType.getByFormattedName( releaseTypeComboBox.getSelectionModel().getSelectedItem() ) );
    }

    @FXML
    void onVersionComboBox(ActionEvent event) {
        currentVersionProperty.set( VersionManifest.getInstance().getVersion( versionComboBox.getSelectionModel().getSelectedItem() ) );
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
        VersionManifest.getInstance().invalidateCache();
        try {
            Builder.setActiveTab( WindowTab.CONSOLE );
            boolean init = VersionManifest.getInstance().init();
        } catch ( ExecutionException e ) {
            e.printStackTrace();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }
        updateMCVersionsCheckBox.setSelected( false );
        initSettings();
    }

    @FXML
    void onUpdateVersions(ActionEvent event) {

    }

    @FXML
    void initialize() {
        try {
            boolean init = VersionManifest.getInstance().init();
        } catch ( ExecutionException e ) {
            e.printStackTrace();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }

        updateVersionsBtn.managedProperty().bind( updateVersionsBtn.visibleProperty() );
        updateVersionsBtn.setVisible( false );

        updateVersionsBtn.visibleProperty().bind( updateMCVersionsCheckBox.selectedProperty() );

        downloadServerBtn.disableProperty().bind( Bindings.isNull( currentVersionProperty ) );
        for ( ReleaseType type : ReleaseType.values() ) {
            ObservableList<String> list = FXCollections.observableArrayList( VersionManifest.getInstance().getByReleaseType( type ).stream().map( MinecraftVersion::getVersion ).collect( Collectors.toList() ) );
            versionMap.put( type, list );
        }
        // set initial values

        List<String> list = Stream.of( ReleaseType.values() ).map( ReleaseType::getFormattedName ).collect( Collectors.toList() );
        releaseTypeComboBox.setItems( FXCollections.observableArrayList( list ) );
        releaseTypeComboBox.getSelectionModel().selectedItemProperty().addListener( ( (observable, oldValue, newValue) -> {
            if ( newValue == null ) {
                releaseTypeComboBox.getSelectionModel().select( ReleaseType.RELEASE.getFormattedName() );
            } else {
                currentReleaseTypeProperty.set( ReleaseType.getByFormattedName( newValue ) );
            }
            versionComboBox.setItems( versionMap.get( ReleaseType.getByFormattedName( newValue ) ) );
            versionComboBox.getSelectionModel().select( 0 );
            currentVersionProperty.set( VersionManifest.getInstance().getVersion( versionComboBox.getSelectionModel().getSelectedItem() ) );
        } ) );

        versionComboBox.getSelectionModel().selectedItemProperty().addListener( ( (observable, oldValue, newValue) -> {
            if ( oldValue != null && newValue == null ) { // removing the value
                releaseDateTextField.clear();
                SHA1TextField.clear();
                currentVersionProperty.set( null );
            }
            if ( oldValue != null && newValue != null || ( oldValue == null && newValue != null ) ) { // setting the value
                // i.e. Tue, 3 Jun 2008 11:05:30 GMT
                MinecraftVersion version = VersionManifest.getInstance().getVersion( newValue );
                currentVersionProperty.set( version );
                releaseDateTextField.setText( version.getReleaseDate().toString() );
                SHA1TextField.setText( version.getSHA_1() );
            }
        } ) );

        releaseTypeComboBox.getSelectionModel().select( ReleaseType.RELEASE.getFormattedName() );
        versionComboBox.setItems( versionMap.get( ReleaseType.RELEASE ) );
        versionComboBox.getSelectionModel().select( 0 );
        currentVersionProperty.set( VersionManifest.getInstance().getVersion( versionComboBox.getSelectionModel().getSelectedItem() ) );
    }

    private void initSettings() {
        releaseTypeComboBox.getSelectionModel().select( ReleaseType.RELEASE.getFormattedName() );
        versionComboBox.setItems( versionMap.get( ReleaseType.RELEASE ) );
        versionComboBox.getSelectionModel().select( 0 );
        currentVersionProperty.set( VersionManifest.getInstance().getVersion( versionComboBox.getSelectionModel().getSelectedItem() ) );
    }
}
