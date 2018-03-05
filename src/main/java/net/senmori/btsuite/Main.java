package net.senmori.btsuite;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.senmori.btsuite.gui.Console;
import net.senmori.btsuite.settings.Settings;
import net.senmori.btsuite.task.GitInstaller;
import net.senmori.btsuite.task.MavenInstaller;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    public static final File WORK_DIR = new File("BTSuite/");
    public static final File SETTINGS_FILE = new File(WORK_DIR, "settings.json");

    public static Stage WINDOW;
    public static final Settings SETTINGS = new Settings();
    public static final TaskRunner TASK_RUNNER = new TaskRunner(3);

    public static Console console;

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

        // install git
        GitInstaller gitTask = new GitInstaller();
        gitTask.setOnSucceeded((event) -> {
             System.out.println("Git bash installation response: " + gitTask.getValue().name());
        });
        TASK_RUNNER.execute(gitTask);

        // install maven
        MavenInstaller mavenTask = new MavenInstaller();
        TASK_RUNNER.execute(mavenTask);
    }

    public static Stage getWindow() {
        return Main.WINDOW;
    }

    public static Console getConsole() {
        return console;
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
        // load from gson
    }

}
