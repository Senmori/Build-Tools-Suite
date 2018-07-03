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
import net.senmori.btsuite.Main;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.buildtools.BuildTools;
import net.senmori.btsuite.settings.Settings;
import net.senmori.btsuite.task.VersionImporter;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.Version;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

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
        dirChooser.setInitialDirectory(Main.WORK_DIR);
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
        if ( ! buildTools.isRunning() ) {
            if ( choiceComboBox.getSelectionModel().getSelectedItem() == null ) {
                buildTools.setVersion(Main.getSettings().getDefaultVersion());
            } else {
                buildTools.setVersion(choiceComboBox.getSelectionModel().getSelectedItem().toLowerCase());
            }
            Main.getTaskRunner().getPool().submit(new Runnable() {
                @Override
                public void run() {
                    buildTools.run();
                }
            });
            runBuildToolsBtn.setDisable(true);
        } else {
            runBuildToolsBtn.setDisable(false);
        }
    }

    @FXML
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

        choiceComboBox.setVisibleRowCount(10);
        Settings settings = Main.getSettings();
        VersionImporter task = new VersionImporter(settings.getVersionLink(), Main.getTaskRunner().getPool());
        task.setOnSucceeded((event) -> {
            try {
                handleVersionMap(task.get());
            } catch ( InterruptedException | ExecutionException e ) {
                e.printStackTrace();
            }
        });
        Main.TASK_RUNNER.getPool().submit(task); // use pool directly
    }

    private void handleVersionMap(Map<Version, BuildInfo> map) {
        List<Version> versions = Lists.newArrayList(map.keySet());
        versions.sort(Version::compareTo);
        versions = Lists.reverse(versions);
        for ( Version ver : versions ) {
            this.choiceComboBox.getItems().add(ver.getVersionString());
        }
        this.buildTools.setVersionMap(map);
    }
}
