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

package net.senmori.btsuite.storage.adapters;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.ConfigurationKey;
import net.senmori.btsuite.storage.SectionKey;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * The settings file is formatted into several sections.
 * 'urls', 'versions', 'output_directories', and 'directories'
 */
public final class BuildToolsDeserializer implements com.google.gson.JsonDeserializer<BuildToolsSettings> {
    @Override
    public BuildToolsSettings deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = element.getAsJsonObject();

        Map<String, String> map = Maps.newHashMap();
        // urls
        if ( obj.has( SectionKey.URL.getSection() ) ) {
            JsonObject urls = obj.getAsJsonObject( SectionKey.URL.getSection() );
            map.put( ConfigurationKey.Name.SPIGOT_VERSION_LINK, urls.get( ConfigurationKey.Name.SPIGOT_VERSION_LINK ).getAsString() );
            map.put( ConfigurationKey.Name.GIT_INSTALLER_LINK, urls.get( ConfigurationKey.Name.GIT_INSTALLER_LINK ).getAsString() );
            map.put( ConfigurationKey.Name.MAVEN_INSTALLER_LINK, urls.get( ConfigurationKey.Name.MAVEN_INSTALLER_LINK ).getAsString() );
            map.put( ConfigurationKey.Name.STASH_REPO_LINK, urls.get( ConfigurationKey.Name.STASH_REPO_LINK ).getAsString() );
            map.put( ConfigurationKey.Name.MC_JAR_DOWNLOAD_LINK, urls.get( ConfigurationKey.Name.MC_JAR_DOWNLOAD_LINK ).getAsString() );
            map.put( ConfigurationKey.Name.S3_DOWNLOAD_LINK, urls.get( ConfigurationKey.Name.S3_DOWNLOAD_LINK ).getAsString() );
        }
        // versions
        if ( obj.has( SectionKey.VERSIONS.getSection() ) ) {
            JsonObject ver = obj.getAsJsonObject( SectionKey.VERSIONS.getSection() );
            map.put( ConfigurationKey.Name.DEFAULT_SPIGOT_VERSION, ver.get( ConfigurationKey.Name.DEFAULT_SPIGOT_VERSION ).getAsString() );
            map.put( ConfigurationKey.Name.MAVEN_VERSION, ver.get( ConfigurationKey.Name.MAVEN_VERSION ).getAsString() );
            map.put( ConfigurationKey.Name.PORTABLE_GIT_VERSION, ver.get( ConfigurationKey.Name.PORTABLE_GIT_VERSION ).getAsString() );
        }
        // output_directories
        List<String> outputDirs = Lists.newLinkedList();
        if ( obj.has( SectionKey.OUTPUT_DIRS.getSection() ) ) {
            JsonArray recent = obj.getAsJsonArray( SectionKey.OUTPUT_DIRS.getSection() );
            for ( JsonElement dir : recent ) {
                String path = dir.getAsString();
                outputDirs.add( path );
            }
        }
        return new BuildToolsSettings( map, outputDirs );
    }
}
