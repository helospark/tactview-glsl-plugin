package com.helospark.glslplugin.conditional;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Load bean if GLSL is present. Without filing majorVersion & minorVersion just check the presence of GLSL, otherwise checks if GLSL actualMajorVersion >= expectedMajorVersion && 
 * actualMinorVersion >= expectedMinorVersion.
 * If minorVersion present majorVersion should also be present.
 * @author helospark
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface ConditionalOnGlsl {
    public GlslVersion version() default GlslVersion.NULL;
}
