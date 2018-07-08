package net.senmori.btsuite;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.Directory;
import net.senmori.btsuite.storage.SettingsFactory;
import net.senmori.btsuite.util.LogHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class Builder extends Application {
    public static final Directory WORKING_DIR = new Directory( System.getProperty( "user.dir" ), "BTSuite" );
    public static final Directory SETTINGS_FILE = new Directory( WORKING_DIR, "BTS_Settings.json" );


    private static BuildToolsSettings SETTINGS;
    private static Stage WINDOW;

    private static TabPane tabPane;

    public static void main(String[] args) {
        float javaVersion = Float.parseFloat( System.getProperty( "java.class.version" ) );

        if ( javaVersion < 52.0F ) {
            LogHandler.error( "*** WARNING *** Outdated Java detected (" + javaVersion + "). Minecraft >= 1.12 requires at least Java 8." );
            LogHandler.error( "*** WARNING *** You may use java -version to double check your Java version." );
        }
        PrintStream empty = new PrintStream( new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        } );
        //System.setOut( empty );
        //System.setErr( empty );
        launch( args );
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        WORKING_DIR.getFile().mkdirs();

        try {
            SETTINGS = TaskPools.submit( () -> SettingsFactory.loadSettings( SETTINGS_FILE.getFile() ) ).get();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        } catch ( ExecutionException e ) {
            e.printStackTrace();
        }


        Builder.WINDOW = primaryStage;
        WINDOW.setTitle( "Build Tools" );
        WINDOW.setResizable( true );


        Image icon = new Image(this.getClass().getClassLoader().getResourceAsStream("icon.png"));
        WINDOW.getIcons().add(icon);

        URL mainController = this.getClass().getClassLoader().getResource("fxml/mainController.fxml");
        tabPane = FXMLLoader.load(mainController);

        Scene scene = new Scene(tabPane);
        WINDOW.setScene( scene );

        primaryStage.show();
        primaryStage.setMinWidth( primaryStage.getWidth() );
        primaryStage.setMinHeight( primaryStage.getHeight() );

        setActiveTab(WindowTab.BUILD);

        getWindow().setOnCloseRequest((event) -> {
            TaskPools.shutdownNow();
            Platform.exit();
        });
    }

    @Override
    public void stop() {
        if(!TaskPools.getService().isShutdown()) {
            TaskPools.shutdownNow();
        }
    }

    public static Stage getWindow() {
        return Builder.WINDOW;
    }

    public TabPane getTabPane() {
        return tabPane;
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

    private static final Boolean DEBUG = Boolean.TRUE;
    public static boolean isDebugEnabled() {
        return DEBUG;
    }
}
