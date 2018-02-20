package net.senmori.btsuite;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import net.senmori.btsuite.settings.Settings;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;

public class Main extends Application {

    public static final int DEF_WIDTH = 600;
    public static final int DEF_HEIGHT = 600;

    public static Stage window;


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.window = primaryStage;
        initWindow(window);

        TabPane tabPane = FXMLLoader.load(new URL("src/main/resources/application.fxml"));

        Settings.init();

        Scene scene = new Scene(tabPane, DEF_WIDTH, DEF_HEIGHT);
        window.setScene(scene);
        // must be last, duh
        primaryStage.show();

        window.setOnHidden((event) -> {
          System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
          System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
        });
    }

    public static Stage getWindow() {
        return window;
    }

    private void initWindow(Stage window) {
        window.setTitle("Build Tools");
        window.setResizable(true);
        window.setMinHeight(DEF_HEIGHT);
        window.setMinWidth(DEF_WIDTH);
        window.setHeight(DEF_HEIGHT);
        window.setWidth(DEF_WIDTH);
    }

     public static void main(String[] args) {
        launch(args);
     }
}
