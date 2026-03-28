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

    double min() default Double.NaN;

    double max() default Double.NaN;

    boolean sync() default false;

    ConfigDisplayType displayType() default ConfigDisplayType.AUTO;

    boolean requiresRestart() default false;
}