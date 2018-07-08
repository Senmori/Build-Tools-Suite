package net.senmori.btsuite.storage.serializers;

import com.google.gson.JsonObject;
import net.senmori.btsuite.storage.SectionKey;
import net.senmori.btsuite.storage.annotations.Exclude;
import net.senmori.btsuite.util.AnnotationUtil;

import java.lang.reflect.Field;
import java.util.Collection;

public class DefaultSerializer extends SectionSerializer {
    public DefaultSerializer() {
        super( SectionKey.NONE );
    }

    @Override
    public void serialize(JsonObject element, Collection<Field> fields, Object owner) {
        for ( Field field : fields ) {
            if ( AnnotationUtil.isAnnotationPresent( field, Exclude.class ) ) continue; // ignore these
            String name = AnnotationUtil.getSerializedName( field );
            try {
                String value = AnnotationUtil.getSerializedValue( field, owner );
                element.addProperty( name, value );
            } catch ( IllegalAccessException e ) {
                e.printStackTrace();
            }
        }
    }
}
