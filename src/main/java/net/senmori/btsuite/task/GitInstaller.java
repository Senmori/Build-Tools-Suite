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

import javafx.concurrent.Task;
import net.senmori.btsuite.command.CommandHandler;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.Directory;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.SystemChecker;
import net.senmori.btsuite.util.TaskUtil;

import java.io.File;

public class GitInstaller extends Task<Boolean> {

    private final BuildToolsSettings settings = BuildToolsSettings.getInstance();
    private final BuildToolsSettings.Directories dirs = settings.getDirectories();

    public GitInstaller() {
    }

    @Override
    public Boolean call() {
        // check for normal git installation
        try {
            LogHandler.debug( "Checking for Git install location." );
            CommandHandler.getCommandIssuer().executeCommand( dirs.getWorkingDir().getFile(), "sh", "-c", "exit" );
            return true;
        } catch ( Exception e ) {
            LogHandler.info( "Git not found. Trying to install PortableGit" );
        }
        return doInstall();
    }

    private boolean doInstall() {
        try {
            if ( SystemChecker.isWindows() ) {
                File portableGitDir = dirs.getPortableGitDir().getFile();
                Directory portableGitExe = new Directory( dirs.getPortableGitDir().getFile().getAbsolutePath(), "PortableGit" );

                if ( portableGitExe.getFile().exists() && portableGitExe.getFile().isDirectory() ) {
                    dirs.setPortableGitDir( portableGitExe );
                    LogHandler.info( "Found PortableGit already installed at " + portableGitExe.getFile() );
                    return true;
                }

                if ( ! portableGitDir.exists() ) {
                    portableGitDir.mkdirs();
                    LogHandler.warn( "*** Could not find PortableGit executable, downloading. ***" );
                    portableGitDir = TaskUtil.asyncDownloadFile( settings.getGitInstallerLink(), portableGitDir );
                }
                if ( ! FileUtil.isDirectory( portableGitExe.getFile() ) ) {
                    portableGitExe.getFile().mkdirs();
                    // yes to all, silent, don't install.  Only -y seems to work
                    // ProcessRunner appends information we don't need
                    Runtime.getRuntime().exec( portableGitDir.getPath(), new String[] { "-y", "-gm2", "-nr" }, portableGitDir.getParentFile() );

                    LogHandler.warn( "*** Please note this is a beta feature, so if it does not work please also try a manual install valueOf git from https://git-for-windows.github.io/ ***" );
                    dirs.setPortableGitDir( portableGitExe );
                    LogHandler.info( "Successfully installed PortableGit to " + dirs.getPortableGitDir() );
                }
            } else { // end if windows check
                LogHandler.error( " Invalid Architecture! This program can only be run on Windows systems!" );
                return false; // Invalid Architecture
            }
            LogHandler.info( "Git installation success!" );
            return true;
        } catch ( Exception e ) {
            LogHandler.error("Failed to install git!");
            return false;
        }
    }
}
