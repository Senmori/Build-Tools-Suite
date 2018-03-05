package net.senmori.btsuite.controllers;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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
import net.senmori.btsuite.Main;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.buildtools.BuildTools;
import net.senmori.btsuite.version.Version;
import net.senmori.btsuite.version.VersionComparator;

public class BuildTabController {

    private BuildTools buildTools;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
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
    void onCertCheckClicked(ActionEvent event) {
        buildTools.disableCertificateCheck = this.certCheck.isSelected();
    }

    @FXML
    void onDontUpdateClicked(ActionEvent event) {
        buildTools.dontUpdate = this.dontUpdate.isSelected();
    }

    @FXML
    void onSkipCompileClicked(ActionEvent event) {
        buildTools.skipCompile = this.skipCompile.isSelected();
    }

    @FXML
    void onGenSrcClicked(ActionEvent event) {
        buildTools.genSrc = this.genSrc.isSelected();
    }

    @FXML
    void onGenDocClicked(ActionEvent event) {
        buildTools.genDoc = this.genDoc.isSelected();
    }

    @FXML
    void onAddOutputDirClicked(ActionEvent event) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory(Main.WORK_DIR);
        dirChooser.setTitle("Add output directory");
        File output = dirChooser.showDialog(Main.getWindow());
        if(output != null && output.isDirectory()) {
            String absPath = output.getAbsolutePath();
            this.outputDirListView.getItems().add(absPath);
            this.delOutputBtn.setDisable(false);
        }
    }

    @FXML
    void onDelOutputDirClicked(ActionEvent event) {
        ObservableList<String> selected = this.outputDirListView.getSelectionModel().getSelectedItems();
        ObservableList<String> all = this.outputDirListView.getItems();
        all.removeAll(selected);
        this.outputDirListView.setItems(all);
        if(this.outputDirListView.getItems().isEmpty()) {
            this.delOutputBtn.setDisable(true);
        }
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert flagBox != null : "fx:id=\"flagBox\" was not injected: check your FXML file 'buildTab.fxml'.";
        assert certCheck != null : "fx:id=\"certCheck\" was not injected: check your FXML file 'buildTab.fxml'.";
        assert dontUpdate != null : "fx:id=\"dontUpdate\" was not injected: check your FXML file 'buildTab.fxml'.";
        assert skipCompile != null : "fx:id=\"skipCompile\" was not injected: check your FXML file 'buildTab.fxml'.";
        assert genSrc != null : "fx:id=\"genSrc\" was not injected: check your FXML file 'buildTab.fxml'.";
        assert genDoc != null : "fx:id=\"genDoc\" was not injected: check your FXML file 'buildTab.fxml'.";
        assert runBuildToolsBtn != null : "fx:id=\"runBuildToolsBtn\" was not injected: check your FXML file 'buildTab.fxml'.";
        assert outputAnchorPane != null : "fx:id=\"outputAnchorPane\" was not injected: check your FXML file 'buildTab.fxml'.";
        assert addOutputDirBtn != null : "fx:id=\"addOutputDirBtn\" was not injected: check your FXML file 'buildTab.fxml'.";
        assert delOutputBtn != null : "fx:id=\"delOutputBtn\" was not injected: check your FXML file 'buildTab.fxml'.";
        assert outputDirListView != null : "fx:id=\"outputDirListView\" was not injected: check your FXML file 'buildTab.fxml'.";
        assert choiceComboBox != null : "fx:id=\"choiceComboBox\" was not injected: check your FXML file 'buildTab.fxml'.";

        buildTools = new BuildTools();
        outputDirListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //choiceComboBox.setVisibleRowCount(10);
        // VersionImporter task = new VersionImporter(Settings.versionLink);
        // task.setOnSucceeded((event) -> {
        //     try {
        //         handleVersionMap(task.get());
        //     } catch (InterruptedException | ExecutionException e) {
        //         e.printStackTrace();
        //     }
        // });
        //Main.TASK_RUNNER.execute(task);
    }

    private void handleVersionMap(Map<Version, BuildInfo> map) {
        List<Version> versions = Lists.newArrayList(map.keySet());
        versions.sort(new VersionComparator());
        versions = Lists.reverse(versions);
        for(Version v : versions) {
            System.out.println("Version: " + v.getVersionString());
            this.choiceComboBox.getItems().add(v.getVersionString());
        }
        this.buildTools.setVersionMap(map);
    }
}
