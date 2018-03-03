/**
 * Sample Skeleton for 'buildTab.fxml' Controller Class
 */

package net.senmori.btsuite.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class BuildTabController {

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

    }
}
