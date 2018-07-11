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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.util.LogHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.stream.Collectors;

@Getter
public class VersionManifest {
    private static final VersionManifest INSTANCE = new VersionManifest();

    public static VersionManifest getInstance() {
        return INSTANCE;
    }

    private VersionManifest() {
    }


    private final BooleanProperty initializedProperty = new SimpleBooleanProperty( this, "VersionManifest", false );
    private final Collection<MinecraftVersion> availableVersions = Lists.newLinkedList();

    public boolean isInitialized() {
        return initializedProperty.get();
    }

    public MinecraftVersion getVersion(String version) {
        return availableVersions.stream().filter( (ver) -> ver.getVersion().equalsIgnoreCase( version ) ).findFirst().orElse( null );
    }

    public Collection<MinecraftVersion> getByReleaseType(ReleaseType releaseType) {
        return availableVersions.stream().filter( (ver) -> ver.getReleaseType() == releaseType ).collect( Collectors.toList() );
    }

    public void setAvailableVersions(Collection<MinecraftVersion> versions) {
        this.availableVersions.clear();
        this.availableVersions.addAll( versions );
    }

    public boolean invalidateCache() {
        initializedProperty.set( false );
        // delete versions_manifest file
        availableVersions.clear();
        BuildToolsSettings.Directories dirs = BuildToolsSettings.getInstance().getDirectories();
        File versionsDir = new File( dirs.getVersionsDir().getFile(), "minecraft" );
        versionsDir.mkdirs();
        File manifestFile = new File( versionsDir, "version_manifest.json" );
        boolean delete = false;
        try {
            delete = Files.deleteIfExists( manifestFile.toPath() );
            LogHandler.info( "Deleted version_manifest" );
        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        }
        return delete;
    }
}
