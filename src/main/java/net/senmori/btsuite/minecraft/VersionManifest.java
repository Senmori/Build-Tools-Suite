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

package net.senmori.btsuite.minecraft;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.SettingsFactory;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.TaskUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Getter
public class VersionManifest {
    private static final VersionManifest INSTANCE = new VersionManifest();
    private static final BuildToolsSettings SETTINGS = BuildToolsSettings.getInstance();
    private static final BuildToolsSettings.Directories DIRS = SETTINGS.getDirectories();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static VersionManifest getInstance() {
        return INSTANCE;
    }

    private VersionManifest() {
    }

    private final BooleanProperty initializedProperty = new SimpleBooleanProperty( false );
    private Collection<MinecraftVersion> availableVersions = Lists.newLinkedList();
    private String latestSnapshot;
    private String latestRelease;


    public MinecraftVersion getLatestSnapshot() {
        return getVersion( this.latestSnapshot );
    }

    public MinecraftVersion getLatestRelease() {
        return getVersion( this.latestRelease );
    }

    public MinecraftVersion getVersion(String version) {
        return availableVersions.stream().filter( (ver) -> ver.getVersion().equalsIgnoreCase( version ) ).findFirst().orElse( null );
    }

    public Collection<MinecraftVersion> getByReleaseType(ReleaseType releaseType) {
        return availableVersions.stream().filter( (ver) -> ver.getReleaseType() == releaseType ).collect( Collectors.toList() );
    }

    public void invalidateCache() {
        getInitializedProperty().set( false );
        // delete versions_manifest file
        availableVersions.clear();
        File versionsDir = new File( DIRS.getVersionsDir().getFile(), "minecraft" );
        versionsDir.mkdirs();
        File manifestFile = new File( versionsDir, "version_manifest.json" );
        try {
            boolean del = Files.deleteIfExists( manifestFile.toPath() );
            LogHandler.info( "Deleted version_manifest" );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }


    public boolean init() throws ExecutionException, InterruptedException {
        if ( initializedProperty.get() ) {
            return false;
        }
        boolean init = TaskPools.submit( () -> {
            File versionsDir = new File( DIRS.getVersionsDir().getFile(), "minecraft" );
            versionsDir.mkdirs();
            File manifestFile = new File( versionsDir, "version_manifest.json" );

            if ( ! manifestFile.exists() ) {
                manifestFile.createNewFile();
                manifestFile = TaskUtil.asyncDownloadFile( SETTINGS.getMinecraftVersionManifestURL(), manifestFile );

                if ( manifestFile == null ) {
                    LogHandler.error( "*** Unable to download \'version_manifest\' file!" );
                    return false;
                }
                LogHandler.info( "Downloaded \'" + manifestFile.getAbsolutePath() + "\' to " + versionsDir.getName() );
            } else {
                LogHandler.info( "Found \'" + manifestFile.getAbsolutePath() + "\' in " + versionsDir.getName() );
            }

            final File fManifest = manifestFile;
            JsonObject json = TaskPools.submit( () -> {
                return SettingsFactory.getGson().fromJson( new FileReader( fManifest ), JsonObject.class );
            } ).get();
            if ( json == null ) {
                LogHandler.error( "*** Unable to process \'version_manifest\' with GSON." );
                return false;
            }
            if ( json.has( "latest" ) ) {
                JsonObject latest = json.getAsJsonObject( "latest" );
                this.latestRelease = latest.get( "release" ).getAsString();
                this.latestSnapshot = latest.get( "snapshot" ).getAsString();
                LogHandler.info( "Imported latest release and shapshot versions." );
                LogHandler.info( "Latest Release: " + this.latestRelease );
                LogHandler.info( "Latest Snapshot: " + this.latestSnapshot );
            }

            if ( json.has( "versions" ) ) {
                JsonArray array = json.getAsJsonArray( "versions" );

                for ( JsonElement element : array ) {
                    JsonObject version = element.getAsJsonObject();

                    String id = version.get( "id" ).getAsString();
                    String type = version.get( "type" ).getAsString();
                    String releaseTime = version.get( "releaseTime" ).getAsString();
                    String url = version.get( "url" ).getAsString();

                    ReleaseType releaseType = ReleaseType.getByName( type );

                    LocalDateTime releaseDate = LocalDateTime.parse( releaseTime, DATE_FORMATTER ); // ISO_OFFSET_DATE_TIME

                    // download this version's specific json file
                    File versionFile = new File( versionsDir, id + ".json" );
                    if ( ! versionFile.exists() ) {
                        versionFile = TaskUtil.asyncDownloadFile( url, versionFile );

                        if ( versionFile == null ) {
                            LogHandler.error( "*** Unable to download \'" + id + "\'\'s version file." );
                            continue;
                        }
                    }

                    JsonObject versionJson = SettingsFactory.getGson().fromJson( new FileReader( versionFile ), JsonObject.class );
                    if ( versionJson == null ) {
                        LogHandler.error( "*** Unable to parse \'" + id + "\'s manifest json." );
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
                            JsonObject server = downloads.getAsJsonObject( "client" );

                            String sha = server.get( "sha1" ).getAsString();
                            String serverDownloadURL = server.get( "url" ).getAsString();

                            MinecraftVersion minecraftVersion = new MinecraftVersion( id, releaseType, releaseDate, sha, serverDownloadURL );

                            availableVersions.add( minecraftVersion );
                        }
                    }
                }

                for ( ReleaseType type : ReleaseType.values() ) {
                    int count = getByReleaseType( type ).size();
                    LogHandler.info( "Loaded " + count + " versions for release type " + type );
                }
            }

            return true;
        } ).get();
        initializedProperty.set( init );
        return init;
    }
}
