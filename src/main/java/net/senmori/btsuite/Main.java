package net.senmori.btsuite;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.senmori.btsuite.controllers.ConsoleController;
import net.senmori.btsuite.gui.Console;
import net.senmori.btsuite.log.BuildToolsLog4jConfigFactory;
import net.senmori.btsuite.log.LoggerStream;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.task.GitInstaller;
import net.senmori.btsuite.task.MavenInstaller;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.LogHandler;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.Invoker;

import java.net.URL;

public class Main extends Application {

    private static Stage WINDOW;
    private static final Settings SETTINGS = new Settings();
    private static final Invoker MAVEN_INVOKER = new DefaultInvoker();

    private static Console console = null;
    private static TabPane tabPane;

    public static void main(String[] args) {
        ConfigurationFactory.setConfigurationFactory(new BuildToolsLog4jConfigFactory());
        LoggerStream.setOutAndErrToLog();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.WINDOW = primaryStage;
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
        Main.WINDOW.setScene(scene);

        primaryStage.show();
        setActiveTab(WindowTab.BUILD);

        getWindow().setOnCloseRequest((event) -> {
            TaskPools.shutdownNow();
            FileUtil.deleteDirectory(SETTINGS.getDirectories().getTmpDir());
        });


        TaskPools.async(() -> new GitInstaller())
                 .async(() -> new MavenInstaller() );

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
        return Main.WINDOW;
    }

    public static void setConsole(Console console) {
        if ( console == null )
            Main.console = console;
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
