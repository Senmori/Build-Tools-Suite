package net.senmori.btsuite;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.gui.Console;
import net.senmori.btsuite.task.GitInstaller;
import net.senmori.btsuite.task.MavenInstaller;
import net.senmori.btsuite.task.SpigotVersionImporter;
import net.senmori.btsuite.util.LogHandler;
import sun.rmi.runtime.Log;

import java.net.URL;
import java.util.Map;

public class Main extends Application {

    private static Stage WINDOW;
    private static final Settings SETTINGS = new Settings();
    private static TaskChainFactory taskChainFactory = BuildToolsTaskChainFactory.create();


    private static Console console = null;
    private static TabPane tabPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.WINDOW = primaryStage;
        initWindow(WINDOW);

        Image icon = new Image(this.getClass().getClassLoader().getResourceAsStream("icon.png"));
        WINDOW.getIcons().add(icon);

        URL mainController = this.getClass().getClassLoader().getResource("fxml/mainController.fxml");
        tabPane = FXMLLoader.load(mainController);

        Scene scene = new Scene(tabPane);
        Main.WINDOW.setScene(scene);

        primaryStage.show();
        setActiveTab(WindowTab.BUILD);

        Map<VersionString, BuildInfo> test = SpigotVersionImporter.getVersions(SETTINGS.getVersionLink());
        Main.newChain()
            .async(() -> GitInstaller.install())
            .async(() -> MavenInstaller.install() );

        LogHandler.debug("Debug mode enabled.");
    }

    private void initWindow(Stage window) {
        window.setTitle("Build Tools");
        window.setResizable(true);
    }

    public static Stage getWindow() {
        return Main.WINDOW;
    }

    public static void setConsole(Console console) {
        if ( console == null )
            Main.console = console;
    }

    public static Settings getSettings() {
        return SETTINGS;
    }

    public static void setActiveTab(WindowTab tab) {
        switch ( tab ) {
            case CONSOLE:
                tabPane.getSelectionModel().select(0);
                break;
            case BUILD:
            default:
                tabPane.getSelectionModel().select(1);
        }
    }

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }

    public static boolean isDebugEnabled() {
        return Boolean.getBoolean("debugBuildTools");
    }
}
