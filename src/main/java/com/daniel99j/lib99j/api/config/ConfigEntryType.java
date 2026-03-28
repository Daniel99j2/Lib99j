package com.daniel99j.lib99j.api.config;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public enum ConfigEntryType {
    AUTO,
    BOOLEAN,
    INT,
    FLOAT,
    DOUBLE,
    SHORT,
    LONG,
    STRING,
    ENUM,
    VEC3,
    VEC2,
    IDENTIFIER,
    CUSTOM;

    public static ConfigEntryType autoType(Class<?> type) {
        if (type == Boolean.class) return ConfigEntryType.BOOLEAN;
        if (type == Integer.class) return ConfigEntryType.INT;
        if (type == Float.class) return ConfigEntryType.FLOAT;
        if (type == Double.class) return ConfigEntryType.DOUBLE;
        if (type == Short.class) return ConfigEntryType.SHORT;
        if (type == Long.class) return ConfigEntryType.LONG;
        if (type == String.class) return ConfigEntryType.STRING;
        if (type == Vec3.class) return ConfigEntryType.VEC3;
        if (type == Vec2.class) return ConfigEntryType.VEC2;
        if (type == Identifier.class) return ConfigEntryType.IDENTIFIER;
        if (type.isEnum()) return ConfigEntryType.ENUM;

        throw new IllegalStateException("Unsupported config type: " + type);
    }
}
