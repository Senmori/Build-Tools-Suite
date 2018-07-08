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
