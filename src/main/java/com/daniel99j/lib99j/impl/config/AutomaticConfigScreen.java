package com.daniel99j.lib99j.impl.config;

import com.daniel99j.lib99j.api.config.ModConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.util.NullScreenFactory;

public class AutomaticConfigScreen {
    public AutomaticConfigScreen(ModConfig modConfig) {
    }

    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return new NullScreenFactory<>();
    }

}
