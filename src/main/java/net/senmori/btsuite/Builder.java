package net.senmori.btsuite;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.senmori.btsuite.controllers.ConsoleController;
import net.senmori.btsuite.gui.Console;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.task.GitInstaller;
import net.senmori.btsuite.task.MavenInstaller;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.LogHandler;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.Invoker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;

public class Builder extends Application {
    private static Stage WINDOW;
    private static final Settings SETTINGS = new Settings();
    private static final Invoker MAVEN_INVOKER = new DefaultInvoker();

    private static Console console = null;
    private static TabPane tabPane;

    public static void main(String[] args) {
        launch(args);
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        }));
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        }));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Builder.WINDOW = primaryStage;
        initWindow(WINDOW);

        if(SETTINGS.getDirectories().getTmpDir() != null && SETTINGS.getDirectories().getTmpDir().exists()) {
            FileUtil.deleteDirectory(SETTINGS.getDirectories().getTmpDir()); // prevent any conflicts
            SETTINGS.getDirectories().init();
        }

        Image icon = new Image(this.getClass().getClassLoader().getResourceAsStream("icon.png"));
        WINDOW.getIcons().add(icon);

        URL mainController = this.getClass().getClassLoader().getResource("fxml/mainController.fxml");
        tabPane = FXMLLoader.load(mainController);

        ConsoleController consoleController;

        Scene scene = new Scene(tabPane);
        Builder.WINDOW.setScene(scene);

        primaryStage.show();
        setActiveTab(WindowTab.BUILD);

        getWindow().setOnCloseRequest((event) -> {
            TaskPools.shutdownNow();
            FileUtil.deleteDirectory(SETTINGS.getDirectories().getTmpDir());
        });

        LogHandler.debug("Debug mode enabled.");
    }

    @Override
    public void stop() {
        if(!TaskPools.getService().isShutdown()) {
            TaskPools.shutdownNow();
        }
        FileUtil.deleteDirectory(SETTINGS.getDirectories().getTmpDir());
    }

    private void initWindow(Stage window) {
        window.setTitle("Build Tools");
        window.setResizable(true);
    }

    public static Stage getWindow() {
        return Builder.WINDOW;
    }

    public static void setConsole(Console console) {
        if ( console == null )
            Builder.console = console;
    }

    public static Settings getSettings() {
        return SETTINGS;
    }

    public static DefaultInvoker getInvoker() {
        return (DefaultInvoker)MAVEN_INVOKER;
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

    public static boolean isDebugEnabled() {
        return Boolean.getBoolean("debugBuildTools");
    }
}
