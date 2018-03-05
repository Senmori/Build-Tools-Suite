package net.senmori.btsuite;

import com.google.common.base.Strings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.senmori.btsuite.gui.Console;
import net.senmori.btsuite.settings.Settings;
import net.senmori.btsuite.task.GitInstaller;
import net.senmori.btsuite.task.MavenInstaller;
import net.senmori.btsuite.util.ProcessRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    public static final File WORK_DIR = new File("BTSuite/");
    public static final File SETTINGS_FILE = new File(WORK_DIR, "settings.json");
    public static final File TMP_DIR = new File(WORK_DIR, "tmp/");

    public static Stage WINDOW;
    public static final Settings SETTINGS = new Settings();
    public static final TaskRunner TASK_RUNNER = new TaskRunner(3);

    private static Console console = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.WINDOW = primaryStage;
        initWindow(WINDOW);
        initSettings();

        Image icon = new Image(this.getClass().getClassLoader().getResourceAsStream("icon.png"));
        WINDOW.getIcons().add(icon);

        URL mainController = this.getClass().getClassLoader().getResource("fxml/mainController.fxml");
        TabPane tabPane = new FXMLLoader(mainController).load();

        Scene scene = new Scene(tabPane);
        Main.WINDOW.setScene(scene);
        // must be last, duh
        primaryStage.show();

        WINDOW.setOnCloseRequest((request) -> {
            TASK_RUNNER.getPool().shutdownNow();
        });

        getTaskRunner().execute(new GitInstaller());
        getTaskRunner().execute(new MavenInstaller());
    }

    public static Stage getWindow() {
        return Main.WINDOW;
    }

    public static Console getConsole() {
        return console;
    }

    public static void setConsole(Console console) {
        if(console == null)
            Main.console = console;
    }

    public static TaskRunner getTaskRunner() {
        return TASK_RUNNER;
    }

    public static Settings getSettings() {
        return SETTINGS;
    }

    private void initWindow(Stage window) {
        window.setTitle("Build Tools");
        window.setResizable(true);
    }

     public static void main(String[] args) {
        launch(args);
     }

    private void initSettings() {
        if(!Main.WORK_DIR.exists()) {
            Main.WORK_DIR.mkdir();
        }
        if(!Main.SETTINGS_FILE.exists()) {
            try {
                Main.SETTINGS_FILE.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!Main.TMP_DIR.exists()) {
            Main.TMP_DIR.mkdir();
        }
    }

}
