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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;

public class Builder extends Application {
    public static final Directory WORKING_DIR = new Directory( System.getProperty( "user.dir" ), "BTSuite" );
    public static final Directory SETTINGS_FILE = new Directory( WORKING_DIR, "BTS_Settings.json" );


    private static BuildToolsSettings SETTINGS;
    private static Stage WINDOW;

    private static TabPane tabPane;

    public static void main(String[] args) {
        PrintStream empty = new PrintStream( new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        } );
        System.setOut( empty );
        System.setErr( empty );
        launch( args );
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        WORKING_DIR.getFile().mkdirs();
        SETTINGS = BuildToolsSettings.create();


        Builder.WINDOW = primaryStage;
        WINDOW.setTitle( "Build Tools Suite" );
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
                tabPane.getSelectionModel().select( 0 );
                break;
            case BUILD:
            default:
                tabPane.getSelectionModel().select( 1 );
        }
    }

    public static boolean isDebugEnabled() {
        return Boolean.getBoolean( "debugBuildTools" );
    }
}
