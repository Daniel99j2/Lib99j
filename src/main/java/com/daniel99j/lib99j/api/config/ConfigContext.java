package com.daniel99j.lib99j.api.config;

import com.daniel99j.lib99j.Lib99j;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public enum ConfigContext {
    CLIENT("client"),
    DEDICATED_SERVER("dedicated_server"),
    LEVEL("level"),
    COMMON("common");

    private final String type;

    ConfigContext(String defaultFileName) {
        this.type = defaultFileName;
    }

    public String getType() {
        return this.type;
    }

    public String getDisplayName() {
        return switch (this) {
            case CLIENT -> "Client";
            case DEDICATED_SERVER -> "Dedicated Server";
            case LEVEL -> "Level";
            case COMMON -> "Common";
        };
    }

    public boolean isAvailable() {
        return switch (this) {
            case CLIENT -> FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
            case DEDICATED_SERVER -> FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER || Lib99j.getServer() != null;
            case LEVEL -> Lib99j.getServer() != null;
            case COMMON -> true;
        };
    }

    public Path resolveFolder(String modId) {
        return switch (this) {
            case CLIENT, DEDICATED_SERVER, COMMON -> FabricLoader.getInstance().getConfigDir().resolve(modId);
            case LEVEL -> Lib99j.getServerOrThrow().getWorldPath(LevelResource.ROOT).resolve("config").resolve(modId);
        };
    }
}
