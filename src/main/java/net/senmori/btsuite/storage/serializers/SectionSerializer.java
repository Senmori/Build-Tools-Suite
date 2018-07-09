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

package net.senmori.btsuite.storage.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.senmori.btsuite.storage.SectionKey;
import net.senmori.btsuite.storage.SettingsFactory;
import net.senmori.btsuite.storage.annotations.Exclude;
import net.senmori.btsuite.storage.annotations.Section;
import net.senmori.btsuite.util.AnnotationUtil;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * A SectionSerializer will take a given object, check if the fields within are annotation with {@link Section}.
 * If so, it will retrieve all fields
 */
public class SectionSerializer {

    private final SectionKey sectionKey;

    public SectionSerializer(SectionKey key) {
        this.sectionKey = key;
    }

    public SectionKey getSectionKey() {
        return sectionKey;
    }

    /**
     * Serialize a given collection of {@link Field}s to this section.
     *
     * @param element the {@link JsonElement} to serialize the fields into
     * @param fields  the fields to serialize
     */
    public void serialize(JsonObject element, Collection<Field> fields, Object owner) {
        JsonObject section = new JsonObject();
        if ( fields == null || fields.isEmpty() ) {
            return;
        }
        for ( Field field : fields ) {
            if ( AnnotationUtil.isAnnotationPresent( field, Exclude.class ) ) continue; // ignore these
            String name = AnnotationUtil.getSerializedName( field );
            try {
                String value = AnnotationUtil.getSerializedValue( field, owner );
                section.addProperty( name, value );
            } catch ( IllegalAccessException e ) {
                e.printStackTrace();
            }
        }
        element.add( sectionKey.getSection(), section );
    }

    public static class OutputDirectories extends SectionSerializer {
        public OutputDirectories() {
            super( SectionKey.URL );
        }

        @Override
        public void serialize(JsonObject element, Collection<Field> fields, Object owner) {

            for ( Field field : fields ) {
                if ( ! field.getType().isAssignableFrom( Collection.class ) ) {
                    continue;
                }
                try {
                    Collection<?> collection = ( Collection<?> ) field.get( owner );
                    JsonElement tree = SettingsFactory.getGson().toJsonTree( collection, field.getType() );
                    element.add( getSectionKey().getSection(), tree );
                } catch ( IllegalAccessException e ) {
                    e.printStackTrace();
                }
            }
        }
    }
}
