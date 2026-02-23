package com.daniel99j.lib99j.impl.config;

import com.daniel99j.lib99j.api.config.ConfigUtil;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.util.HashMap;
import java.util.Map;

public class ModMenuIntegration implements ModMenuApi {
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        Map<String, ConfigScreenFactory<?>> out = new HashMap<>();
        ConfigUtil.configs.forEach(((s, config) -> {
            out.put(s, config.getScreen().getModConfigScreenFactory());
        }));
        return out;
    }
}
