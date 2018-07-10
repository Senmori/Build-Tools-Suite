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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import net.senmori.btsuite.Builder;
import net.senmori.btsuite.minecraft.MinecraftVersion;
import net.senmori.btsuite.minecraft.ReleaseType;
import net.senmori.btsuite.minecraft.VersionManifest;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.task.FileDownloadTask;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
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

    Map<ReleaseType, ObservableList<MinecraftVersion>> versionMap = new HashMap<>();
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
        currentReleaseTypeProperty.set( releaseTypeComboBox.getSelectionModel().getSelectedItem() );
    }

    @FXML
    void onVersionComboBox(ActionEvent event) {
        currentVersionProperty.set( versionComboBox.getSelectionModel().getSelectedItem() );
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
    void initialize() {
        try {
            boolean init = VersionManifest.getInstance().init();
        } catch ( ExecutionException e ) {
            e.printStackTrace();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }

        downloadServerBtn.disableProperty().bind( Bindings.isNull( currentVersionProperty ) );
        for ( ReleaseType type : ReleaseType.values() ) {
            ObservableList<MinecraftVersion> list = FXCollections.observableArrayList( VersionManifest.getInstance().getByReleaseType( type ) );
            versionMap.put( type, list );
        }
        // set initial values

        releaseTypeComboBox.setItems( FXCollections.observableArrayList( ReleaseType.values() ) );
        releaseTypeComboBox.setConverter( new StringConverter<ReleaseType>() {
            @Override
            public String toString(ReleaseType object) {
                return object.getFormattedName();
            }

            @Override
            public ReleaseType fromString(String string) {
                return ReleaseType.getByFormattedName( string );
            }
        } );
        releaseTypeComboBox.getSelectionModel().selectedItemProperty().addListener( ( (observable, oldValue, newValue) -> {
            if ( newValue == null ) {
                releaseTypeComboBox.getSelectionModel().select( ReleaseType.RELEASE );
            } else {
                currentReleaseTypeProperty.set( newValue );
            }
            versionComboBox.setItems( versionMap.get( newValue ) );
            versionComboBox.getSelectionModel().select( 0 );
            currentVersionProperty.set( versionComboBox.getSelectionModel().getSelectedItem() );
        } ) );

        versionComboBox.setConverter( new StringConverter<MinecraftVersion>() {
            @Override
            public String toString(MinecraftVersion object) {
                return object.getVersion();
            }

            @Override
            public MinecraftVersion fromString(String string) {
                return VersionManifest.getInstance().getVersion( string );
            }
        } );

        versionComboBox.getSelectionModel().selectedItemProperty().addListener( ( (observable, oldValue, newValue) -> {
            if ( oldValue != null && newValue == null ) { // removing the value
                releaseDateTextField.clear();
                SHA1TextField.clear();
                currentVersionProperty.set( newValue );
            }
            if ( oldValue != null && newValue != null || ( oldValue == null && newValue != null ) ) { // setting the value
                // i.e. Tue, 3 Jun 2008 11:05:30 GMT
                currentVersionProperty.set( newValue );
                releaseDateTextField.setText( newValue.getReleaseDate().toString() );
                SHA1TextField.setText( newValue.getSHA_1() );
            }
        } ) );

        releaseTypeComboBox.getSelectionModel().select( ReleaseType.RELEASE );
        versionComboBox.setItems( versionMap.get( ReleaseType.RELEASE ) );
        versionComboBox.getSelectionModel().select( 0 );
        currentVersionProperty.set( versionComboBox.getSelectionModel().getSelectedItem() );
    }
}
