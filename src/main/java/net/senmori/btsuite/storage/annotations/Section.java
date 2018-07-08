package net.senmori.btsuite.storage.annotations;

import net.senmori.btsuite.storage.SectionKey;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to denote which section an object should be serialized under.
 *
 * For example:
 *
 * <code>
 * <code>@Section</code>("urls")
 * public String workingDir;
 * </code>
 *
 * Would result in {@code workingDir} to be serialized in the 'urls' section of the configuration
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD } )
public @interface Section {

    SectionKey value() default SectionKey.NONE;
}
