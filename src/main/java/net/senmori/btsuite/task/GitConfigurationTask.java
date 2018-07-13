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

package net.senmori.btsuite.task;

import net.senmori.btsuite.command.CommandHandler;
import net.senmori.btsuite.command.ICommandIssuer;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.util.LogHandler;

public class GitConfigurationTask implements Runnable {


    private final BuildToolsSettings settings;
    private final BuildToolsSettings.Directories dirs;

    public GitConfigurationTask(BuildToolsSettings settings) {
        this.settings = settings;
        this.dirs = settings.getDirectories();
    }

    @Override
    public void run() {
        ICommandIssuer commandHandler = CommandHandler.getCommandIssuer();
        try {
            commandHandler.executeCommand( dirs.getWorkingDir().getFile(), "git", "--version" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        try {
            commandHandler.executeCommand( dirs.getWorkingDir().getFile(), "git", "config", "--global", "--includes", "user.name" );
        } catch ( Exception ex ) {
            LogHandler.info( "Git name not set, setting it to default value." );
            try {
                commandHandler.executeCommand( dirs.getWorkingDir().getFile(), "git", "config", "--global", "user.name", "BuildToolsSuite" );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        try {
            commandHandler.executeCommand( dirs.getWorkingDir().getFile(), "git", "config", "--global", "--includes", "user.email" );
        } catch ( Exception ex ) {
            LogHandler.info( "Git email not set, setting it to default value." );
            try {
                commandHandler.executeCommand( dirs.getWorkingDir().getFile(), "git", "config", "--global", "user.email", "buildToolsSuite@null.spigotmc.org" );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }
}
