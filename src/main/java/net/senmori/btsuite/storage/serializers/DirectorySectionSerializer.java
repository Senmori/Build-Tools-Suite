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
