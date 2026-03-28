package com.daniel99j.lib99j.api.config;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.ModInstallManager;
import com.daniel99j.lib99j.impl.network.ClientboundLib99jSyncConfigOptionPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigManager {
    @ApiStatus.Internal
    public static Map<String, ModConfig> configs = new LinkedHashMap<>();

    @ApiStatus.Internal
    public static void load() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> configs.values().forEach(config -> {
            ConfigHolder<?> serverConfig = config.get(ConfigContext.DEDICATED_SERVER);
            if (serverConfig != null) {
                serverConfig.reload();
            }

            ConfigHolder<?> saveConfig = config.get(ConfigContext.WORLD);
            if (saveConfig != null) {
                saveConfig.reload();
            }
        }));

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> configs.values().forEach(config -> {
            config.unload(ConfigContext.WORLD);
            config.unload(ConfigContext.DEDICATED_SERVER);
        }));

        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) ClientTickEvents.END_CLIENT_TICK.register((server) -> {
            configs.values().forEach(ModConfig::checkDirty);
        });

        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) ServerTickEvents.END_SERVER_TICK.register((server) -> {
            configs.values().forEach(ModConfig::checkDirty);
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, a, b) -> configs.values().forEach(ModConfig::reloadAvailable));
    }

    public static ModConfig addConfig(String mod, ModConfig config) {
        config.setModId(mod);
        configs.put(mod, config);
        ConfigHolder<?> clientConfig = config.get(ConfigContext.CLIENT);
        if (clientConfig != null && clientConfig.isAvailable()) {
            clientConfig.reload();
        }
        if (net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.SERVER) {
            ConfigHolder<?> serverConfig = config.get(ConfigContext.DEDICATED_SERVER);
            if (serverConfig != null) {
                serverConfig.reload();
            }
        }
        return config;
    }

    public static @Nullable ModConfig getConfig(String mod) {
        return configs.get(mod);
    }

    public static ModConfig getConfigOrThrow(String mod) {
        if(!configs.containsKey(mod)) throw new IllegalStateException("Cannot find config for 1".replace("1", mod));
        return getConfig(mod);
    }

    public static Map<String, ModConfig> getConfigs() {
        return Collections.unmodifiableMap(configs);
    }

    public static void syncConfigs(ServerPlayer player) {
        configs.forEach((modId, config) -> {
            for (ConfigHolder<?> availableConfig : config.getAvailableConfigs()) {
                if(availableConfig.getContext() != ConfigContext.CLIENT) {
                    for (ConfigHolder.ConfigField field : availableConfig.getFields()) {
                         if(field.annotation().sync())  {
                             if(ModInstallManager.isInstalled(Lib99j.MOD_ID, player)) {
                                 ServerPlayNetworking.send(player, new ClientboundLib99jSyncConfigOptionPacket(modId, field.getSerializedName(), ConfigUtils.GSON.toJson(field.getValue(availableConfig.get()), field.getValue(availableConfig.get()).getClass())));
                             }
                        }
                    }
                }

            }
        });
    }
}
