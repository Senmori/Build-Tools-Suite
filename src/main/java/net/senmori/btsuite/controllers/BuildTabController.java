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
import javafx.collections.ObservableList;
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
import net.senmori.btsuite.Builder;
import net.senmori.btsuite.VersionString;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.buildtools.BuildTools;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.task.SpigotVersionImporter;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.LogHandler;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BuildTabController {

    private BuildToolsSettings buildToolsSettings = BuildToolsSettings.getInstance();
    private BuildTools buildTools;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location valueOf the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="flagBox"
    private VBox flagBox;

    @FXML // fx:id="certCheck"
    private CheckBox certCheck;

    @FXML // fx:id="dontUpdate"
    private CheckBox dontUpdate;

    @FXML // fx:id="skipCompile"
    private CheckBox skipCompile;

    @FXML // fx:id="genSrc"
    private CheckBox genSrc;

    @FXML // fx:id="genDoc"
    private CheckBox genDoc;

    @FXML // fx:id="runBuildToolsBtn"
    private Button runBuildToolsBtn;

    @FXML // fx:id="outputAnchorPane"
    private AnchorPane outputAnchorPane;

    @FXML // fx:id="addOutputDirBtn"
    private Button addOutputDirBtn;

    @FXML // fx:id="delOutputBtn"
    private Button delOutputBtn;

    @FXML // fx:id="outputDirListView"
    private ListView<String> outputDirListView;

    @FXML // fx:id="choiceComboBox"
    private ComboBox<String> choiceComboBox;

    @FXML
    private CheckBox buildInvalidateCache;

    @FXML
    void onInvalidateCacheBtn(ActionEvent event) {
        buildTools.setInvalidateCache( this.buildInvalidateCache.isSelected() );
    }

    @FXML
    void onCertCheckClicked(ActionEvent event) {
        buildTools.setDisableCertificateCheck(this.certCheck.isSelected());
    }

    @FXML
    void onDontUpdateClicked(ActionEvent event) {
        buildTools.setDontUpdate(this.dontUpdate.isSelected());
    }

    @FXML
    void onSkipCompileClicked(ActionEvent event) {
        buildTools.setSkipCompile(this.skipCompile.isSelected());
    }

    @FXML
    void onGenSrcClicked(ActionEvent event) {
        buildTools.setGenSrc(this.genSrc.isSelected());
    }

    @FXML
    void onGenDocClicked(ActionEvent event) {
        buildTools.setGenDoc(this.genDoc.isSelected());
    }

    @FXML
    void onAddOutputDirClicked(ActionEvent event) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory( buildToolsSettings.getDirectories().getWorkingDir().getFile() );
        dirChooser.setTitle("Add output directory");
        File output = dirChooser.showDialog(Builder.getWindow());
        if ( FileUtil.isDirectory(output) ) {
            this.outputDirListView.getItems().add(output.getAbsolutePath());
            BuildToolsSettings.getInstance().getRecentOutputDirectories().add( output.getAbsolutePath() );
        }
    }

    @FXML
    void onDelOutputDirClicked(ActionEvent event) {
        ObservableList<String> selected = this.outputDirListView.getSelectionModel().getSelectedItems();
        ObservableList<String> all = this.outputDirListView.getItems();
        all.removeAll(selected);
        this.outputDirListView.setItems(all);
        BuildToolsSettings.getInstance().getRecentOutputDirectories().clear();
        BuildToolsSettings.getInstance().getRecentOutputDirectories().addAll( all );
        if ( this.outputDirListView.getItems().isEmpty() ) {
            outputDirListView.getItems().add( Builder.WORKING_DIR.getFile().getAbsolutePath() );
            BuildToolsSettings.getInstance().getRecentOutputDirectories().add( Builder.WORKING_DIR.getFile().getAbsolutePath() );
        }
        if ( outputDirListView.getItems().size() == 1 ) {
            if ( outputDirListView.getItems().get( 0 ).equalsIgnoreCase( Builder.WORKING_DIR.getFile().getAbsolutePath() ) ) {
                delOutputBtn.setDisable( true );
                return;
            }
        }
        delOutputBtn.setDisable( false );
    }

    @FXML
    void onRunBuildToolsClicked() {
        if ( !buildTools.isRunning() ) {
            if ( choiceComboBox.getSelectionModel().getSelectedItem() == null ) {
                buildTools.setVersion( buildToolsSettings.getDefaultVersion() );
            } else {
                buildTools.setVersion(choiceComboBox.getSelectionModel().getSelectedItem().toLowerCase());
            }
            buildTools.setOutputDirectories( outputDirListView.getItems() );
            TaskPools.submit(() -> buildTools.run());
        }
    }

    @FXML
    void initialize() {
        buildTools = new BuildTools( this );
        outputDirListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // populate directory list
        outputDirListView.getItems().addAll( BuildToolsSettings.getInstance().getRecentOutputDirectories() );
        if ( outputDirListView.getItems().isEmpty() ) {
            outputDirListView.getItems().add( Builder.WORKING_DIR.getFile().getAbsolutePath() );
        }

        choiceComboBox.setVisibleRowCount(10);
        runBuildToolsBtn.disableProperty().bind( buildTools.getRunningProperty() );

        SpigotVersionImporter importer = new SpigotVersionImporter( buildToolsSettings.getVersionLink() );
        Future<Map<VersionString, BuildInfo>> future = TaskPools.submit(importer);
        Map<VersionString, BuildInfo> versionMap = null;
        try {
            versionMap = future.get();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        } catch ( ExecutionException e ) {
            e.printStackTrace();
        }


        if(versionMap != null) {
            handleVersionMap(versionMap);
        } else {
            LogHandler.warn("Error importing version map.");
        }
    }

    public void onBuildToolsFinished(BuildTools tool) {
        runBuildToolsBtn.setDisable(false);
    }

    private void handleVersionMap(Map<VersionString, BuildInfo> map) {
        choiceComboBox.getItems().clear();
        List<VersionString> versions = Lists.newArrayList(map.keySet());
        versions.sort(VersionString::compareTo);
        versions = Lists.reverse(versions);
        for ( VersionString ver : versions ) {
            this.choiceComboBox.getItems().add(ver.getVersionString());
        }
    }
}
