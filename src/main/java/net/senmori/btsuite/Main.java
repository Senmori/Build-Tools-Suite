package net.senmori.btsuite;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import net.senmori.btsuite.settings.Settings;
import net.senmori.btsuite.version.VersionImporter;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.URL;

public class Main extends Application {

    public static final File WORK_DIR = new File("./BTSuite");
    public static final File SETTINGS_FILE = new File(WORK_DIR, "settings.json");

    public static Stage WINDOW;
    public static Settings SETTINGS = new Settings();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.WINDOW = primaryStage;
        initWindow(WINDOW);

        URL mainController = this.getClass().getClassLoader().getResource("fxml/mainController.fxml");
        TabPane tabPane = FXMLLoader.load(mainController);
        tabPane.getSelectionModel().select(1);

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
