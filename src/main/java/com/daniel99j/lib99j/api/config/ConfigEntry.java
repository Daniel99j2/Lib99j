package com.daniel99j.lib99j.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigEntry {
    String category() default "general";
    ConfigEntryType type() default ConfigEntryType.AUTO;

    boolean show() default true;

    boolean sync() default true;

    boolean requiresAdmin() default true;

    double min() default Double.NaN;
    double max() default Double.NaN;

    double step() default 1.0; // for sliders
    boolean requiresRestart() default false;
}