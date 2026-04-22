package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.djutil.SupplyingEvent;
import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.CustomEvents;
import com.daniel99j.lib99j.api.ModInstallManager;
import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.impl.network.ClientboundLib99jPonderItemsPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PonderManager {
    /**
     * The event that registers all ponder groups, scenes, and builders
     * <p>Normally this is only called on game load, but it is also called with /ponder dev reload</p>
     */
    public static SupplyingEvent<PonderRegistrar> REGISTER = new SupplyingEvent<>();

    @ApiStatus.Internal
    public static Map<ServerPlayer, PonderScene> activeScenes = new HashMap<>();
    @ApiStatus.Internal
    public static Map<ServerPlayer, Identifier> scenesAboutToStart = new HashMap<>();

    @ApiStatus.Internal
    public static Map<Item, List<Identifier>> itemToBuilders = new HashMap<>();
    @ApiStatus.Internal
    public static Map<Identifier, PonderBuilder> idToBuilder = new HashMap<>();
    @ApiStatus.Internal
    public static Map<Identifier, PonderGroup> idToGroup = new HashMap<>();

    protected static boolean isLoading = false;

    public static boolean isPondering(ServerPlayer player) {
        return activeScenes.containsKey(player) && !activeScenes.get(player).isToBeRemoved();
    }

    @ApiStatus.Internal
    public static void load() {
        VFXUtils.registerCameraLockHandler(Identifier.fromNamespaceAndPath("ponder", "ponder_lock"), ((player, packet) -> {
            if (PonderManager.isPondering(player)) {
                if(packet.hasPosition()) PonderManager.activeScenes.get(player).identifyPos = new Vec3(packet.getX(0), packet.getY(0)+player.getEyeHeight(), packet.getZ(0));
                if(packet.hasRotation()) PonderManager.activeScenes.get(player).identifyPitch = packet.getXRot(0);
                if(packet.hasRotation()) PonderManager.activeScenes.get(player).identifyYaw = packet.getYRot(0);

            }
        }));

        CustomEvents.GAME_LOADED.register(PonderManager::reload);
    }

    public static void reload() {
        PonderManager.activeScenes.forEach(((player, ponderScene) -> {
            ponderScene.stopPondering(true);
        }));
        PonderManager.activeScenes.clear();
        PonderManager.scenesAboutToStart.clear();
        PonderManager.itemToBuilders.clear();
        PonderManager.idToBuilder.clear();
        PonderManager.idToGroup.clear();

        isLoading = true;
        REGISTER.invoke(new PonderRegistrar());
        idToGroup.forEach((id, group) -> {
            for (PonderBuilder builder : group.builders) {
                builder.groups.add(id);
            }
        });
        isLoading = false;

        if(Lib99j.getServer() != null) {
            for (ServerPlayer player : Lib99j.getServerOrThrow().getPlayerList().players) {
                if(ModInstallManager.isInstalled(Lib99j.MOD_ID, player)) {
                    List<Identifier> ponderItemIds = new ArrayList<>();
                    PonderManager.itemToBuilders.forEach(((item, _) -> {
                        ponderItemIds.add(BuiltInRegistries.ITEM.getKey(item));
                    }));

                    ServerPlayNetworking.send(player, new ClientboundLib99jPonderItemsPacket(ponderItemIds));
                }
            }
        }
    }

    @ApiStatus.Internal
    public static void tick() {
        scenesAboutToStart.forEach(((player, identifier) -> {
            idToBuilder.get(identifier).startPondering(player);
        }));
        scenesAboutToStart.clear();

        activeScenes.entrySet().removeIf((scene) -> {
            boolean toRemove = scene.getValue().isToBeRemoved();
            if(toRemove) Lib99j.getServerOrThrow().levels.remove(scene.getValue().levelKey);
            return toRemove;
        });

        for(PonderScene scene : activeScenes.values()) {
            scene.tick();
        }
    }

    public static PonderGroup getGroupForItem(Item item) {
        Identifier groupId = Identifier.fromNamespaceAndPath("_item_"+BuiltInRegistries.ITEM.getKey(item).getNamespace(), BuiltInRegistries.ITEM.getKey(item).getPath());
        return idToGroup.get(groupId);
    }

    public static PonderBuilder getBuilder(Identifier id) {
        return idToBuilder.get(id);
    }

    public static PonderGroup getGroup(Identifier id) {
        return idToGroup.get(id);
    }

    public static @Nullable PonderScene getActiveScene(ServerPlayer player) {
        return activeScenes.get(player);
    }
}
