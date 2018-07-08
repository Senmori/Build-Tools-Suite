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
