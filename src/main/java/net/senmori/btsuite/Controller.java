package net.senmori.btsuite;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import net.senmori.btsuite.buildtools.ProjectBuilder;

public class Controller {
    /*
     * All project components go below here
     */
    public static ProjectBuilder buildToolsBuilder;


    /*
     * All build tab components go below here
     */
    @FXML public Tab buildTab;

    @FXML public VBox flagBox;
    @FXML public CheckBox certCheck;
    @FXML public CheckBox dontUpdate;
    @FXML public CheckBox skipCompile;
    @FXML public CheckBox genSrc;
    @FXML public CheckBox genDoc;

    @FXML public ChoiceBox revChoiceBox;

    @FXML public Button runBuildToolsBtn;

    // Output Directory section
    @FXML public AnchorPane outputAnchorPane;
    @FXML public Button addOutputDirBtn;
    @FXML public Button delOutputBtn;
    @FXML public ListView outputDirListView;

    /*
     * All console components go below here
     */
    @FXML public Tab consoleTab;
    @FXML public AnchorPane consoleAnchorPane;
    @FXML public TextArea consoleTextArea;

    @FXML
    private void intialize() {
        // TODO: populate revChoiceBox options
        // TODO: create directory table (w/ delete button per-row)
        // TODO: Git settings
        // TODO: Maven settings (?)
        // TODO: Hope I can find a way to run all this is a somewhat OOP way.

        buildToolsBuilder = new ProjectBuilder(this);

        consoleTab.setOnSelectionChanged((event) -> {
            consoleTextArea.setText("awesome text");

            for(int i = 0; i < 2000; i++) {
                consoleTextArea.appendText("TEST STRING\n");
            }
        });
    }

    public void onCertCheckClicked() {
        buildToolsBuilder.setDisableCertCheck(!buildToolsBuilder.isDisableCertCheck());
    }
}
