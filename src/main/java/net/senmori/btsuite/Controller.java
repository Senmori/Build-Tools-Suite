package net.senmori.btsuite;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import net.senmori.btsuite.buildtools.ProjectBuilder;

public class Controller {
    /*
     * All project components go below here
     */
    public static ProjectBuilder buildToolsBuilder = new ProjectBuilder();


    /*
     * All build tab components go below here
     */
    @FXML private Tab buildTab;

    @FXML private VBox flagBox;
    @FXML private CheckBox certCheck;
    @FXML private CheckBox dontUpdate;
    @FXML private CheckBox skipCompile;
    @FXML private CheckBox genSrc;
    @FXML private CheckBox genDoc;

    @FXML private ChoiceBox revChoiceBox;

    @FXML private VBox outVBox;
    @FXML private Button outBrowseBtn;

    /*
     * All console components go below here
     */
    @FXML private Tab consoleTab;

    @FXML
    private void intialize() {
        // TODO: populate revChoiceBox options
        // TODO: create directory table (w/ delete button per-row)
        // TODO: Git settings
        // TODO: Maven settings (?)
        // TODO: Hope I can find a way to run all this is a somewhat OOP way.
    }
}
