package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.config.ConfigUtils;
import com.daniel99j.lib99j.impl.network.ClientboundLib99jSyncConfigOptionPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ServerConfigCopy {
    private static final Map<String, Map<String, String>> configOptions = new HashMap<>();
    private static final Map<String, Map<String, Object>> configOptionsCache = new HashMap<>();

    public static <T> T getConfigOption(String modId, String name, Class<T> clazz, T fallback) {
        if(configOptionsCache.containsKey(modId) && configOptionsCache.get(modId).containsKey(name)) {
            Object value = configOptionsCache.get(modId).get(name);
            if(!value.getClass().equals(clazz)) {
                Lib99j.LOGGER.warn("Synced config value for {} named {} was of class {}, expected: {}", modId, name, value.getClass(), clazz);
                return fallback;
            } else {
                //noinspection unchecked
                return ((T) value);
            }
        };
        T realObject = getConfigOptionNoCache(modId, name, clazz, fallback);

        if(!configOptionsCache.containsKey(modId)) configOptionsCache.put(modId, new HashMap<>());

        configOptionsCache.get(modId).put(name, realObject);

        return realObject;
    }

    public static <T> T getConfigOptionNoCache(String modId, String name, Class<T> clazz, T fallback) {
        if(!configOptions.containsKey(modId)) return fallback;
        if(!configOptions.get(modId).containsKey(name)) {
            Lib99j.LOGGER.warn("Mod {} was present, but it's synced config values did not contain {}", modId, name);
            return fallback;
        };

        try {
            T value = ConfigUtils.GSON.fromJson(configOptions.get(modId).get(name), clazz);
            return value != null ? value : fallback;
        } catch (Exception e) {
            Lib99j.LOGGER.warn("Failed to deserialize synced config option {}:{} as {}", modId, name, clazz.getName(), e);
            return fallback;
        }
    }

    @ApiStatus.Internal
    public static void load() {
        ClientPlayConnectionEvents.DISCONNECT.register(((clientPacketListener, minecraft) -> {
            configOptions.clear();
            configOptionsCache.clear();
        }));


        ClientPlayNetworking.registerGlobalReceiver(ClientboundLib99jSyncConfigOptionPacket.ID, (payload, context) -> {
            configOptionsCache.clear();
            configOptions.putIfAbsent(payload.modId(), new HashMap<>());
            configOptions.get(payload.modId()).put(payload.name(), payload.value());
        });
    }
}
