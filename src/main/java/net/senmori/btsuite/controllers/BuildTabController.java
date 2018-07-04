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
import lombok.val;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.VersionString;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.buildtools.BuildTools;
import net.senmori.btsuite.Settings;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.task.SpigotVersionImporter;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.JavaFxUtils;
import net.senmori.btsuite.util.LogHandler;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BuildTabController {

    private Settings settings = Main.getSettings();
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
        dirChooser.setInitialDirectory(settings.getDirectories().getWorkingDir());
        dirChooser.setTitle("Add output directory");
        File output = dirChooser.showDialog(Main.getWindow());
        if ( FileUtil.isDirectory(output) ) {
            this.outputDirListView.getItems().add(output.getAbsolutePath());
            this.delOutputBtn.setDisable(false);
        }
    }

    @FXML
    void onDelOutputDirClicked(ActionEvent event) {
        ObservableList<String> selected = this.outputDirListView.getSelectionModel().getSelectedItems();
        ObservableList<String> all = this.outputDirListView.getItems();
        all.removeAll(selected);
        this.outputDirListView.setItems(all);
        if ( this.outputDirListView.getItems().isEmpty() ) {
            this.delOutputBtn.setDisable(true);
        }
    }

    @FXML
    void onRunBuildToolsClicked() {
        if ( !buildTools.isRunning() ) {
            if ( choiceComboBox.getSelectionModel().getSelectedItem() == null ) {
                buildTools.setVersion(Main.getSettings().getDefaultVersion());
            } else {
                buildTools.setVersion(choiceComboBox.getSelectionModel().getSelectedItem().toLowerCase());
            }
            TaskPools.submit(() -> buildTools.run());
            runBuildToolsBtn.setDisable(true);
        } else {
            runBuildToolsBtn.setDisable(false);
        }
    }

    @FXML
    void initialize() {
        buildTools = new BuildTools();
        outputDirListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        choiceComboBox.setVisibleRowCount(10);
        SpigotVersionImporter importer = new SpigotVersionImporter(settings.getVersionLink());
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

    private void handleVersionMap(Map<VersionString, BuildInfo> map) {
        List<VersionString> versions = Lists.newArrayList(map.keySet());
        versions.sort(VersionString::compareTo);
        versions = Lists.reverse(versions);
        for ( VersionString ver : versions ) {
            this.choiceComboBox.getItems().add(ver.getVersionString());
        }
        this.buildTools.setVersionMap(map);
        ObservableList<String> items = choiceComboBox.getItems();
    }
}
