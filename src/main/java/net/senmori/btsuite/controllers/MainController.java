package net.senmori.btsuite.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import net.senmori.btsuite.buildtools.ProjectBuilder;

public class MainController {

    @FXML Tab buildTab;
    @FXML Tab consoleTab;

    @FXML private BuildTabController buildTabController;

    @FXML private ConsoleController consoleController;


    @FXML
    private void initialize() {


    }
}
