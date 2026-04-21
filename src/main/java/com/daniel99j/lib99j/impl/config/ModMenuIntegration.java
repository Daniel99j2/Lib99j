package com.daniel99j.lib99j.impl.config;

import com.daniel99j.lib99j.api.config.ConfigManager;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.util.NullScreenFactory;
import net.fabricmc.loader.api.FabricLoader;

import java.util.HashMap;
import java.util.Map;

public class ModMenuIntegration implements ModMenuApi {
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        Map<String, ConfigScreenFactory<?>> out = new HashMap<>();
        ConfigManager.configs.forEach(((s, config) -> {
            if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
                out.put(s, new AutomaticConfigScreen(config).getModConfigScreenFactory());
            } else {
                out.put(s, new NullScreenFactory<>());
            }
        }));
        return out;
    }
}
