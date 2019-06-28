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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import net.senmori.btsuite.versioning.ComparableVersion;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
public class VersionManifest {

    private final BooleanProperty initializedProperty = new SimpleBooleanProperty( this, "initialized", false );
    private final BooleanProperty importVersionsProxyProperty = new SimpleBooleanProperty( this, "importVersions", false );
    private final ObservableList<MinecraftVersion> availableVersions = FXCollections.observableArrayList();

    public BooleanProperty getInitializedProperty() {
        return initializedProperty;
    }

    public boolean isInitialized() {
        return initializedProperty.get();
    }

    public void setInitialized(boolean value) {
        initializedProperty.set( value );
    }

    public BooleanProperty getImportVersionsProperty() {
        return importVersionsProxyProperty;
    }

    public void importVersions() {
        importVersionsProxyProperty.set( true );
    }

    public MinecraftVersion getVersion(String version) {
        ComparableVersion compare = new ComparableVersion( version );
        return availableVersions.stream()
                .filter( (ver) -> ver.getVersion().equals( compare ) )
                .findFirst()
                .orElse( null );
    }

    public Collection<MinecraftVersion> getVersionsByReleaseType(ReleaseType releaseType) {
        return availableVersions.stream()
                .filter( (ver) -> ver.getReleaseType() == releaseType )
                .collect( Collectors.toList() );
    }

    public void setAvailableVersions(Collection<MinecraftVersion> versions) {
        this.availableVersions.clear();
        availableVersions.addAll( versions );
        this.initializedProperty.set( true );
    }
}
