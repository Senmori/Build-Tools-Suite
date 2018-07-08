package net.senmori.btsuite.util;

import com.google.gson.annotations.SerializedName;
import net.senmori.btsuite.storage.SectionKey;
import net.senmori.btsuite.storage.annotations.Section;
import net.senmori.btsuite.storage.annotations.SerializedValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public final class AnnotationUtil {


    public static boolean isAnnotationPresent(Field field, Class<? extends Annotation> annotation) {
        return field.getAnnotationsByType( annotation ) != null;
    }

    public static SectionKey getSectionKeyFromField(Field field) {
        return field.getAnnotation( Section.class ).value();
    }

    public static String getSerializedName(Field field) {
        String name = field.getName();
        if ( AnnotationUtil.isAnnotationPresent( field, SerializedName.class ) ) {
            SerializedName anno = field.getAnnotation( SerializedName.class );
            return anno.value();
        }
        return name;
    }

    public static String getSerializedValue(Field field, Object owner) throws IllegalAccessException {
        if ( isAnnotationPresent( field, SerializedValue.class ) ) {
            SerializedValue anno = field.getAnnotation( SerializedValue.class );
            return anno.value();
        }
        return field.get( owner ).toString();
    }
}
