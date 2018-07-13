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

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.concurrent.Task;
import net.senmori.btsuite.Console;
import net.senmori.btsuite.buildtools.BuildTools;
import net.senmori.btsuite.minecraft.MinecraftVersion;
import net.senmori.btsuite.minecraft.ReleaseType;
import net.senmori.btsuite.pool.TaskPool;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.SettingsFactory;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.TaskUtil;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public class ImportMinecraftVersionTask extends Task<Collection<MinecraftVersion>> {

    private final BuildToolsSettings settings;
    private final BuildToolsSettings.Directories dirs;

    private final String versionManifestURL;
    private final BuildTools buildTools;
    private final Console console;
    private final Collection<MinecraftVersion> availableVersions = Lists.newLinkedList();
    private final TaskPool pool = TaskPools.createFixedThreadPool( 4 );

    public ImportMinecraftVersionTask(BuildTools buildTools) {
        this.buildTools = buildTools;
        this.console = buildTools.getConsole();
        this.settings = buildTools.getSettings();
        this.dirs = this.settings.getDirectories();

        this.versionManifestURL = this.settings.getMinecraftVersionManifestURL();
    }

    @Override
    protected Collection<MinecraftVersion> call() throws IOException, java.io.FileNotFoundException {
        LogHandler.info( "Importing Minecraft Versions!" );
        File versionsDir = new File( dirs.getVersionsDir().getFile(), "minecraft" );
        versionsDir.mkdirs();
        File manifestFile = new File( versionsDir, "version_manifest.json" );
        if ( !manifestFile.exists() ) {
            // download it
            manifestFile.createNewFile();
            manifestFile = TaskUtil.asyncDownloadFile( versionManifestURL, manifestFile );
            console.setOptionalText( "" );

            if ( manifestFile == null ) {
                LogHandler.error( "*** Could not download \' version_manifest.json\' file." );
                return availableVersions;
            }
            LogHandler.info( "Downloaded new " + FilenameUtils.getBaseName( manifestFile.getName() ) );
        } else {
            LogHandler.info( "Found \'" + FilenameUtils.getBaseName( manifestFile.getName() ) + '\'' );
        }
        JsonObject json = SettingsFactory.getGson().fromJson( new FileReader( manifestFile ), JsonObject.class );
        if ( json == null ) {
            LogHandler.error( "*** Could not parse json in " + FilenameUtils.getBaseName( manifestFile.getName() ) + '\'' );
            return availableVersions;
        }

        if ( json.has( "versions" ) ) {
            LogHandler.info( "Processing versions..." );
            String lastType = "";
            JsonArray array = json.getAsJsonArray( "versions" );

            for ( JsonElement element : array ) {
                JsonObject version = element.getAsJsonObject();

                String id = version.get( "id" ).getAsString();
                String type = version.get( "type" ).getAsString();
                String releaseTime = version.get( "releaseTime" ).getAsString();
                String verURL = version.get( "url" ).getAsString();

                if ( !type.equalsIgnoreCase( lastType ) ) {
                    lastType = type;
                    LogHandler.info( "Processing release type: " + lastType );
                }

                ReleaseType releaseType = ReleaseType.getByName( type );

                LocalDateTime releaseDate = LocalDateTime.parse( releaseTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME ); // ISO_OFFSET_DATE_TIME

                console.setOptionalText( "Processing: " + id );

                // download this version's specific json file
                File versionFile = new File( versionsDir, id + ".json" );
                if ( !versionFile.exists() ) {
                    versionFile.createNewFile();
                    versionFile = TaskUtil.asyncDownloadFile( verURL, versionFile );

                    if ( versionFile == null ) {
                        LogHandler.error( "*** Unable to download \'" + id + "\'\'s version file." );
                        continue;
                    }
                }

                JsonObject versionJson = SettingsFactory.getGson().fromJson( new FileReader( versionFile ), JsonObject.class );
                if ( versionJson == null ) {
                    LogHandler.error( "*** Unable to parse version \"" + id + "\" manifest json." );
                    LogHandler.error( "*** Deleting " + versionFile.getName() + ". Please invalidate the cache in the Minecraft tab if this causes problems." );
                    FileDeleteStrategy.FORCE.delete( versionFile );
                    continue;
                }

                if ( versionJson.has( "downloads" ) ) {
                    JsonObject downloads = versionJson.getAsJsonObject( "downloads" );

                    if ( downloads.has( "server" ) ) {
                        JsonObject server = downloads.getAsJsonObject( "server" );

                        String sha = server.get( "sha1" ).getAsString();
                        String serverDownloadURL = server.get( "url" ).getAsString();

                        MinecraftVersion minecraftVersion = new MinecraftVersion( id, releaseType, releaseDate, sha, serverDownloadURL );

                        availableVersions.add( minecraftVersion );

                    } else if ( downloads.has( "client" ) ) {
                        // old_alpha or some old_beta
                        JsonObject server = downloads.getAsJsonObject( "client" );

                        String sha = server.get( "sha1" ).getAsString();
                        String serverDownloadURL = server.get( "url" ).getAsString();

                        MinecraftVersion minecraftVersion = new MinecraftVersion( id, releaseType, releaseDate, sha, serverDownloadURL );

                        availableVersions.add( minecraftVersion );
                    }
                }
            }
            console.setOptionalText( "" );
        }
        LogHandler.info( "All versions imported!" );
        console.setOptionalText( "All versions imported!" );
        return availableVersions;
    }
}
