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
import net.senmori.btsuite.util.FileUtil;

import java.io.File;
import java.net.URL;

public class Main extends Application {

    public static final File WORK_DIR = new File("BTSuite/");
    public static final File SETTINGS_FILE = new File(WORK_DIR, "settings.json");
    public static final File TMP_DIR = new File(WORK_DIR, "tmp/");
    public static final File JAR_DIR = new File(WORK_DIR, "jars/");
    public static File MVN_DIR = new File(System.getenv("M2_HOME"));
    public static File PORTABLE_GIT_DIR = null;

    public static Stage WINDOW;
    public static Settings SETTINGS = new Settings();
    public static final TaskRunner TASK_RUNNER = new TaskRunner(3);

    private static Console console = null;
    private static TabPane tabPane;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.WINDOW = primaryStage;
        initWindow(WINDOW);
        initSettings();

        Main.TMP_DIR.deleteOnExit();

        Image icon = new Image(this.getClass().getClassLoader().getResourceAsStream("icon.png"));
        WINDOW.getIcons().add(icon);

        URL mainController = this.getClass().getClassLoader().getResource("fxml/mainController.fxml");
        tabPane = FXMLLoader.load(mainController);

        Scene scene = new Scene(tabPane);
        Main.WINDOW.setScene(scene);

        WINDOW.setOnCloseRequest((request) -> {
            stop();
        });

        getTaskRunner().submit(new GitInstaller());
        getTaskRunner().submit(new MavenInstaller());

        primaryStage.show();
    }

    @Override
    public void stop() {
        TASK_RUNNER.getPool().shutdown();
        FileUtil.deleteDirectory(Main.TMP_DIR);
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
        if(!Main.TMP_DIR.exists()) {
            Main.TMP_DIR.mkdir();
            PORTABLE_GIT_DIR = new File(Main.WORK_DIR, getSettings().getGitVersion());
        }
        if (! Main.JAR_DIR.exists()) {
            Main.JAR_DIR.mkdir();
        }
    }

    public static void setActiveTab(WindowTab tab) {
        switch (tab) {
            case CONSOLE:
                tabPane.getSelectionModel().select(1);
                break;
            case BUILD:
            default:
                tabPane.getSelectionModel().select(0);
        }
    }

}
