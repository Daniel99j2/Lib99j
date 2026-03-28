package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.api.config.ConfigManager;
import com.daniel99j.lib99j.impl.network.ClientboundLib99jHelloPacket;
import com.daniel99j.lib99j.impl.network.ServerboundLib99HelloPacket;
import com.daniel99j.lib99j.impl.network.ServerboundLib99jInstalledModsPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class ModInstallManager {
    private static final ArrayList<String> syncedModIds = new ArrayList<>();
    private static final Map<UUID, ArrayList<String>> playerModList = new HashMap<>();

    public static void syncInstalledStatus(String modId) {
        syncedModIds.add(modId);
    }

    public static boolean isInstalled(String modId, ServerPlayer player) {
        if(!syncedModIds.contains(modId)) throw new IllegalStateException("Mod id 1 was not set to sync".replace("1", modId));
        return playerModList.containsKey(player.getUUID()) && playerModList.get(player.getUUID()).contains(modId);
    }

    public static ArrayList<String> getSyncedModIds() {
        return syncedModIds;
    }

    @ApiStatus.Internal
    public static void load() {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) ClientPlayConnectionEvents.JOIN.register((listener, sender, minecraft) -> {
            ClientPlayNetworking.send(new ServerboundLib99HelloPacket());
        });

        ServerPlayerEvents.LEAVE.register((serverPlayer) -> {
            playerModList.remove(serverPlayer.getUUID());
        });

        ServerPlayNetworking.registerGlobalReceiver(ServerboundLib99HelloPacket.ID, (payload, context) -> {
            playerModList.remove(context.player().getUUID());
            playerModList.put(context.player().getUUID(), new ArrayList<>());
            ServerPlayNetworking.send(context.player(), new ClientboundLib99jHelloPacket());
        });

        ServerPlayNetworking.registerGlobalReceiver(ServerboundLib99jInstalledModsPacket.ID, (payload, context) -> {
            playerModList.get(context.player().getUUID()).addAll(payload.installedMods());

            ConfigManager.syncConfigs(context.player());
        });

        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) ClientPlayNetworking.registerGlobalReceiver(ClientboundLib99jHelloPacket.ID, (payload, context) -> {
            List<String> installedMods = new ArrayList<>();
            for (String syncedModId : syncedModIds) {
                if(FabricLoader.getInstance().isModLoaded(syncedModId)) installedMods.add(syncedModId);
            }
            ClientPlayNetworking.send(new ServerboundLib99jInstalledModsPacket(installedMods));
        });
    }
}
