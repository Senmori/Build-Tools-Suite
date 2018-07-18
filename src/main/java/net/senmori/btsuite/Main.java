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

import com.google.common.collect.Maps;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.senmori.btsuite.controllers.ControllerFactory;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.Directory;

import java.util.Map;

public class Main extends Application {
    public static final Directory WORKING_DIR = new Directory( System.getProperty( "user.dir" ), "BTSuite" );
    public static final Directory SETTINGS_FILE = new Directory( WORKING_DIR, "BTS_Settings.json" );

    private static Main INSTANCE = null;

    public static Main getInstance() {
        return INSTANCE;
    }

    private Map<WindowTab, Tab> tabMap = Maps.newConcurrentMap();

    private Stage window;
    private TabPane tabPane;
    private ControllerFactory controllerFactory;

    public static void main(String[] args) {
        launch( args );
    }

    @Override
    public void init() {

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        INSTANCE = this;
        WORKING_DIR.getFile().mkdirs();
        BuildToolsSettings.create();
        BuildToolsSettings.getInstance().getDirectories().init();

        window = primaryStage;
        window.setTitle( "Build Tools Suite" );
        window.setResizable( true );

        Image icon = new Image( this.getClass().getClassLoader().getResourceAsStream( "icon.png" ) );
        window.getIcons().add( icon );

        controllerFactory = new ControllerFactory( BuildToolsSettings.getInstance() );
        primaryStage.addEventHandler( WindowEvent.WINDOW_SHOWN, (event) -> {
            controllerFactory.startupTasks();
        } );

        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory( controllerFactory );
        loader.setLocation( getClass().getClassLoader().getResource( "mainController.fxml" ) );
        tabPane = loader.load();

        tabMap.put( WindowTab.CONSOLE, tabPane.getTabs().get( 0 ) );
        tabMap.put( WindowTab.BUILD, tabPane.getTabs().get( 1 ) );
        tabMap.put( WindowTab.MINECRAFT, tabPane.getTabs().get( 2 ) );

        Scene scene = new Scene( tabPane );
        window.setScene( scene );

        primaryStage.show();
        primaryStage.setMinWidth( primaryStage.getWidth() );
        primaryStage.setMinHeight( primaryStage.getHeight() );


        getWindow().setOnCloseRequest( (event) -> {
            TaskPools.shutdown();
        });
    }

    @Override
    public void stop() {
    }

    public static Stage getWindow() {
        return Main.getInstance().window;
    }

    public static void setActiveTab(WindowTab tab) {
        Tab newTab = Main.getInstance().tabMap.getOrDefault( tab, Main.getInstance().tabPane.getTabs().get( 0 ) );
        Main.getInstance().getTabPane().getSelectionModel().select( newTab );
    }

    public TabPane getTabPane() {
        return Main.getInstance().tabPane;
    }

    public static boolean isDebugEnabled() {
        return false;
    }
}
