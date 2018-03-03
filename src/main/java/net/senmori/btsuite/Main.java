package net.senmori.btsuite;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import net.senmori.btsuite.controllers.BuildTabController;
import net.senmori.btsuite.controllers.ConsoleController;
import net.senmori.btsuite.controllers.MainController;
import net.senmori.btsuite.settings.Settings;
import java.io.File;
import java.net.URL;

public class Main extends Application {

    public static final File WORK_DIR = new File("./BTSuite");
    public static final File SETTINGS_FILE = new File(WORK_DIR, "settings.json");

    public static Stage WINDOW;
    public static Settings SETTINGS = new Settings();

    public static MainController MAIN_CONTROLLER;
    public static BuildTabController BUILD_TAB_CONTROLLER;
    public static ConsoleController CONSOLE_CONTROLLER;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.WINDOW = primaryStage;
        initWindow(WINDOW);

        URL mainController = this.getClass().getClassLoader().getResource("fxml/mainController.fxml");
        TabPane tabPane = FXMLLoader.load(mainController);

        MAIN_CONTROLLER = new FXMLLoader(this.getClass().getResource("fxml/mainController.fxml")).getController();
        BUILD_TAB_CONTROLLER = new FXMLLoader(this.getClass().getResource("fxml/console.fxml")).getController();
        CONSOLE_CONTROLLER = new FXMLLoader(this.getClass().getResource("fxml/buildTab.fxml")).getController();

        Scene scene = new Scene(tabPane);
        Main.WINDOW.setScene(scene);
        // must be last, duh
        primaryStage.show();
    }

    public static Stage getWindow() {
        return Main.WINDOW;
    }

    private void initWindow(Stage window) {
        window.setTitle("Build Tools");
        window.setResizable(true);
    }

     public static void main(String[] args) {
        launch(args);
     }
}
