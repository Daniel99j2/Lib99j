package com.daniel99j.lib99j.api.config;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public class ConfigUtil {
    @ApiStatus.Internal
    public static Map<String, ModConfig> configs = new HashMap<>();

    public static void addConfig(String mod, ModConfig config) {
        configs.put(mod, config);
    }
}
