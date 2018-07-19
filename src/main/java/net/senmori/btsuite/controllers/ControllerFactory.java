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

package net.senmori.btsuite.controllers;

import com.google.common.collect.Maps;
import javafx.util.Callback;
import net.senmori.btsuite.Console;
import net.senmori.btsuite.buildtools.BuildTools;
import net.senmori.btsuite.minecraft.VersionManifest;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.task.GitConfigurationTask;
import net.senmori.btsuite.task.GitInstaller;
import net.senmori.btsuite.task.MavenInstaller;

import java.util.Map;

public class ControllerFactory implements Callback<Class<?>, Object> {

    private final BuildToolsSettings settings;
    private final BuildToolsSettings.Directories directories;
    private final Map<Class<?>, Object> controllerMap = Maps.newHashMap();

    private final BuildTools buildTools;
    private final VersionManifest versionManifest = new VersionManifest();
    private final Console console = new Console();

    private final ConsoleController consoleController;
    private final MinecraftTabController minecraftTabController;
    private final BuildTabController buildTabController;
    private final MainController mainController = new MainController();

    public ControllerFactory(BuildToolsSettings settings) {
        this.settings = settings;
        directories = settings.getDirectories();

        buildTools = new BuildTools( directories.getWorkingDir(), settings, versionManifest, console );
        consoleController = new ConsoleController( this.console );
        minecraftTabController = new MinecraftTabController( console, versionManifest, this.buildTools );
        this.buildTabController = new BuildTabController( this.buildTools );


        controllerMap.put( ConsoleController.class, consoleController );
        controllerMap.put( MinecraftTabController.class, minecraftTabController );
        controllerMap.put( BuildTabController.class, buildTabController );
        controllerMap.put( MainController.class, mainController );
    }

    @Override
    public Object call(Class<?> param) {
        return controllerMap.get( param );
    }

    /**
     * All tasks that should always be started when the program starts should be submitted here.
     * <p>
     * This method should only be called once; directly after the main stage is shown.
     */
    public void startupTasks() {
        GitInstaller git = new GitInstaller();
        git.setOnSucceeded( (worker) -> {
            TaskPools.submit( new GitConfigurationTask( buildTools.getSettings() ) );
        } );
        TaskPools.submit( git );
        MavenInstaller maven = new MavenInstaller();
        TaskPools.submit( maven );

        buildTabController.importVersions( false );
        minecraftTabController.importVersions( false );
    }
}
