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

package net.senmori.btsuite.storage.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.ConfigurationKey;
import net.senmori.btsuite.storage.Directory;
import net.senmori.btsuite.storage.SectionKey;
import net.senmori.btsuite.util.AnnotationUtil;

import java.lang.reflect.Field;
import java.util.Collection;

public class DirectorySectionSerializer extends SectionSerializer {
    public DirectorySectionSerializer() {
        super( SectionKey.DIRECTORIES );
    }

    @Override
    public void serialize(JsonObject element, Collection<Field> fields, Object owner) {
        for ( Field field : fields ) {
            if ( ! field.getType().isAssignableFrom( BuildToolsSettings.Directories.class ) ) {
                continue;
            }

            try {
                BuildToolsSettings.Directories directories = ( BuildToolsSettings.Directories ) field.get( owner );

                Field[] dirFields = directories.getClass().getDeclaredFields();
                JsonArray array = new JsonArray();
                for ( Field f : dirFields ) {
                    if ( f.getType().isAssignableFrom( Directory.class ) ) {
                        Directory directory = ( Directory ) f.get( directories );

                        String name = AnnotationUtil.getSerializedName( field );
                        String parent = directory.getParent();
                        String path = directory.getPath();

                        JsonObject obj = new JsonObject();
                        obj.addProperty( ConfigurationKey.Name.DIR_ID, name );
                        obj.addProperty( ConfigurationKey.Name.DIR_PARENT, parent );
                        obj.addProperty( ConfigurationKey.Name.DIR_PATH, path );

                        array.add( obj );
                    }
                }
                element.add( getSectionKey().getSection(), array );
            } catch ( IllegalAccessException e ) {
                e.printStackTrace();
            }
        }
    }
}
