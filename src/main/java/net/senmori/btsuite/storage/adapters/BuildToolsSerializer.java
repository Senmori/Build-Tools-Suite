/*
 * Copyright (c) $year, $user. BuildToolsSuite. All rights reserved.
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.SectionKey;
import net.senmori.btsuite.storage.annotations.Exclude;
import net.senmori.btsuite.storage.annotations.Section;
import net.senmori.btsuite.storage.serializers.DefaultSerializer;
import net.senmori.btsuite.storage.serializers.DirectorySectionSerializer;
import net.senmori.btsuite.storage.serializers.SectionSerializer;
import net.senmori.btsuite.util.AnnotationUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * The settings file is formatted into several sections.
 * 'urls', 'versions', 'output_directories', and 'directories'
 */
public class BuildToolsSerializer implements JsonSerializer<BuildToolsSettings> {
    private static SectionSerializer DEFAULT_SERIALIZER = new DefaultSerializer();

    private final Map<SectionKey, SectionSerializer> serializers = Maps.newHashMap();

    public BuildToolsSerializer() {
        serializers.put( SectionKey.NONE, new DefaultSerializer() );
        serializers.put( SectionKey.URL, new SectionSerializer( SectionKey.URL ) );
        serializers.put( SectionKey.DIRECTORIES, new DirectorySectionSerializer() );
        serializers.put( SectionKey.VERSIONS, new SectionSerializer( SectionKey.VERSIONS ) );
        serializers.put( SectionKey.OUTPUT_DIRS, new SectionSerializer( SectionKey.OUTPUT_DIRS ) );
    }

    private SectionSerializer getSerializer(SectionKey key) {
        return serializers.getOrDefault( key, DEFAULT_SERIALIZER );
    }

    @Override
    public JsonElement serialize(BuildToolsSettings settings, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        Multimap<SectionKey, Field> fieldMap = ArrayListMultimap.create();
        for ( Field field : settings.getClass().getDeclaredFields() ) {
            if ( Modifier.isStatic( field.getModifiers() ) || Modifier.isTransient( field.getModifiers() ) ) {
                continue;
            }
            if ( AnnotationUtil.isAnnotationPresent( field, Exclude.class ) ) {
                continue;
            }
            if ( AnnotationUtil.isAnnotationPresent( field, Section.class ) ) {
                SectionKey key = AnnotationUtil.getSectionKeyFromField( field );
                fieldMap.put( key, field );
            } else {
                fieldMap.put( SectionKey.NONE, field );
            }
        }

        for ( SectionKey key : fieldMap.keySet() ) {
            getSerializer( key ).serialize( json, fieldMap.get( key ), settings );
        }
        return json;
    }
}
