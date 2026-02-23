package com.daniel99j.lib99j.api.config;

import com.daniel99j.lib99j.impl.config.AutomaticConfigScreen;
import org.jspecify.annotations.Nullable;

public class ModConfig {
    private final AutomaticConfigScreen screen = new AutomaticConfigScreen(this);
    public final @Nullable ConfigHolder gameConfig;
    public final @Nullable ConfigHolder worldConfig;

    public ModConfig(@Nullable ConfigHolder gameConfig, @Nullable ConfigHolder worldConfig) {
        this.gameConfig = gameConfig;
        this.worldConfig = worldConfig;
    }

    public AutomaticConfigScreen getScreen() {
        return screen;
    }
}
