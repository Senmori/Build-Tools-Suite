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
