/*
 * Copyright (c) 2018, Senmori. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

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
