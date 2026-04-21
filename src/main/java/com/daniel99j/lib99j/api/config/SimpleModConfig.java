package com.daniel99j.lib99j.api.config;

public class SimpleModConfig<CLIENT, COMMON, DEDICATED_SERVER, LEVEL> {
    private final ModConfig config;

    public SimpleModConfig(ModConfig config) {
        this.config = config;
    }

    public CLIENT getClient() {
        //noinspection unchecked
        return (CLIENT) this.config.getOrThrow(ConfigContext.CLIENT).getOrThrow();
    }

    public COMMON getCommon() {
        //noinspection unchecked
        return (COMMON) this.config.getOrThrow(ConfigContext.COMMON).getOrThrow();
    }

    public DEDICATED_SERVER getServer() {
        //noinspection unchecked
        return (DEDICATED_SERVER) this.config.getOrThrow(ConfigContext.DEDICATED_SERVER).getOrThrow();
    }

    public LEVEL getLevel() {
        //noinspection unchecked
        return (LEVEL) this.config.getOrThrow(ConfigContext.LEVEL).getOrThrow();
    }

    public ModConfig getConfig() {
        return config;
    }
}
