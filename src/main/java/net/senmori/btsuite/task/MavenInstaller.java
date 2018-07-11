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
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.Directory;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.ZipUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MavenInstaller extends Task<File> {

    private final BuildToolsSettings buildToolsSettings = BuildToolsSettings.getInstance();
    private final BuildToolsSettings.Directories dirs = buildToolsSettings.getDirectories();

    public MavenInstaller() {
    }


    @Override
    public File call() {
        LogHandler.debug( "Checking for Maven install location." );
        if ( isInstalled() ) {
            File mvn = new File( System.getenv( "M2_HOME" ) );
            dirs.setMvnDir( new Directory( mvn.getParent(), mvn.getName() ) );
            LogHandler.info( "Maven is installed at " + dirs.getMvnDir().getFile().getAbsolutePath() );
            return dirs.getMvnDir().getFile();
        }

        File maven = dirs.getMvnDir().getFile();
        if ( !maven.exists() ) {
            maven.mkdirs();
            LogHandler.info( "Maven does not exist, downloading. Please wait." );

            File mvnTemp = new File( dirs.getWorkingDir().getFile(), "mvn.zip" );

            try {
                String url = buildToolsSettings.getMvnInstallerLink();
                mvnTemp = TaskPools.submit( new FileDownloadTask( url, mvnTemp ) ).get();
                ZipUtil.unzip( mvnTemp, dirs.getMvnDir().getFile() );
                dirs.setMvnDir( new Directory( dirs.getMvnDir(), "apache-maven-" + BuildToolsSettings.getInstance().getMavenVersion() ) );
                mvnTemp.delete();
            } catch ( IOException | InterruptedException | ExecutionException e ) {
                e.printStackTrace();
                return null;
            }
        } else {
            // get inner folder
            maven = new File( dirs.getMvnDir().getFile(), "apache-maven-" + BuildToolsSettings.getInstance().getMavenVersion() );
            if ( ! maven.exists() ) {
                LogHandler.info( "Maven directory was found, but no maven installation!" );
                FileUtils.deleteQuietly( dirs.getMvnDir().getFile() ); // delete and ignore errors
                TaskPools.execute( new MavenInstaller() );
                this.cancel( true );
                return null;
            } else { // apache-maven-<version> exists
                LogHandler.info( "Local install of maven found!" );
                dirs.setMvnDir( new Directory( dirs.getMvnDir(), "apache-maven-" + BuildToolsSettings.getInstance().getMavenVersion() ) );
            }
        }
        LogHandler.info( "Maven is installed at " + dirs.getMvnDir().getFile() );
        return dirs.getMvnDir().getFile();
    }

    private boolean isInstalled() {
        String m2Home = System.getenv("M2_HOME");
        return m2Home != null && new File(m2Home).exists();
    }
}
