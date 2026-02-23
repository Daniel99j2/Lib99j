package com.daniel99j.lib99j.api.config;

import java.lang.reflect.Field;

public enum ConfigEntryType {
    AUTO,
    BOOLEAN,
    INT,
    DOUBLE,
    SHORT,
    LONG,
    STRING,
    ENUM,
    VECTOR,
    CUSTOM;

    public static ConfigEntryType autoType(Field field) {
        Class<?> type = field.getType();
        if (type == boolean.class) return ConfigEntryType.BOOLEAN;
        if (type == int.class) return ConfigEntryType.INT;
        if (type == double.class) return ConfigEntryType.DOUBLE;
        if (type == String.class) return ConfigEntryType.STRING;
        if (type.isEnum()) return ConfigEntryType.ENUM;

        throw new IllegalStateException("Unsupported config type: " + type);
    }
}