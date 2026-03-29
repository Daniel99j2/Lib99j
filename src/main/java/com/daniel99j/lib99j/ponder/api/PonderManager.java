package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.CustomEvents;
import com.daniel99j.lib99j.api.GameProperties;
import com.daniel99j.lib99j.api.VFXUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class PonderManager {
    public static Map<ServerPlayer, PonderScene> activeScenes = new HashMap<>();
    public static Map<ServerPlayer, Identifier> scenesAboutToStart = new HashMap<>();

    @ApiStatus.Internal
    public static Map<Item, List<Identifier>> itemToBuilders = new HashMap<>();
    @ApiStatus.Internal
    public static Map<Identifier, PonderBuilder> idToBuilder = new HashMap<>();
    @ApiStatus.Internal
    public static Map<Identifier, PonderGroup> idToGroup = new HashMap<>();

    public static boolean frozen = false;

    public static boolean isPondering(ServerPlayer player) {
        return activeScenes.containsKey(player) && !activeScenes.get(player).isToBeRemoved();
    }

    @ApiStatus.Internal
    public static void load() {
        CustomEvents.GAME_LOADED.register(() -> {
            idToGroup.forEach((id, group) -> {
                for (PonderBuilder builder : group.builders) {
                    builder.groups.add(id);
                }
            });
            frozen = true;
            Lib99j.debug("Ponder registration now frozen");
        });
        VFXUtils.registerCameraLockHandler(Identifier.fromNamespaceAndPath("ponder", "ponder_lock"), ((player, packet) -> {
            if (PonderManager.isPondering(player)) {
                if(packet.hasPosition()) PonderManager.activeScenes.get(player).identifyPos = new Vec3(packet.getX(0), packet.getY(0)+player.getEyeHeight(), packet.getZ(0));
                if(packet.hasRotation()) PonderManager.activeScenes.get(player).identifyYaw = packet.getXRot(0);
                if(packet.hasRotation()) PonderManager.activeScenes.get(player).identifyPitch = packet.getYRot(0);

            }
        }));
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

    /**
     * Registers the builder from an item
     * <p>When pondering about an item, a set of rules is used to determine which ponder should show up first:</p>
     * <p>In general, the first registered association will be used</p>
     * <p>If the item namespace is equal to the id namespace, this will take priority over other registered associations</p>
     * @throws IllegalStateException When a ponder builder has been marked as hidden in commands (use registerBuilder() instead)
     */
    public static PonderBuilder registerItemToBuilder(Item item, PonderBuilder builder) {
        if(builder.shouldHideFromCommands()) throw new IllegalStateException("Ponder builder {} is hidden, therefor cannot be added to an item".replace("{}", builder.id.toString()));
        if(builder.item == null) builder.item = item;
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        boolean fromNameSpace = Objects.equals(itemId.getNamespace(), builder.id.getNamespace());

        registerBuilder(builder);

        if (!itemToBuilders.containsKey(item)) {
            itemToBuilders.put(item, new ArrayList<>());
        }
        if (fromNameSpace && !itemToBuilders.get(item).isEmpty() && (!Objects.equals(PonderManager.idToBuilder.get(itemToBuilders.get(item).getFirst()).id.getNamespace(), itemId.getNamespace()))) {
            itemToBuilders.get(item).addFirst(builder.id);
            builder.item = item;
        } else itemToBuilders.get(item).add(builder.id);

        Identifier groupId = Identifier.fromNamespaceAndPath("_item_"+BuiltInRegistries.ITEM.getKey(builder.item).getNamespace(), BuiltInRegistries.ITEM.getKey(builder.item).getPath());

        if(!idToGroup.containsKey(groupId)) PonderManager.registerGroup(new PonderGroup(groupId, new ItemStackTemplate(builder.item), new ArrayList<>()));

        idToGroup.get(groupId).builders.add(builder);

        return builder;
    }

    /**
     * Registers the builder from an ID
     * <p>Don't use this for ponders relating to items - see registerItemToBuilder instead
     *
     * @throws IllegalStateException When a ponder builder has already been registered with this ID
     */
    public static PonderBuilder registerBuilder(PonderBuilder builder) {
        if(registerIdToBuilderNoThrow(builder)) return builder;
        throw new IllegalStateException("A ponder builder is already registered for {}".replace("{}", builder.id.toString()));
    }

    /**
    * Registers the builder from an ID. Note this is not actually deprecated, but is just recommended not to use
     * <p>See registerIdToBuilder() instead
    */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static boolean registerIdToBuilderNoThrow(PonderBuilder builder) {
        if(frozen) throw new IllegalStateException("Ponder builder {} was registered after load".replace("{}", builder.id.toString()));
        if(idToBuilder.containsKey(builder.id)) return false;
        idToBuilder.put(builder.id, builder);
        builder.registered = true;
        return true;
    }

    /**
     * Registers a ponder group
     * <p>These groups are accessed through the ponder menu (L), and allow for associated groups and scenes to a specific scene to be accessed without using commands
     *
     * @throws IllegalStateException When a ponder builder has already been registered with this ID
     */
    public static PonderGroup registerGroup(PonderGroup builder) {
        GameProperties.throwIfPonderNotEnabled("Ponder has not been enabled! Use GameProperties.enablePonder()");
        if(frozen) throw new IllegalStateException("Ponder group {} was registered after load".replace("{}", builder.id.toString()));

        if(idToGroup.containsKey(builder.id)) throw new IllegalStateException("A ponder builder is already registered for {}".replace("{}", builder.id.toString()));
        idToGroup.put(builder.id, builder);
        //Check that the group's builders are modifiable
        //Other mods should always be able to access other mod's groups!
        try {
            PonderBuilder test = PonderBuilder.create(null, null, Component.empty(), Component.empty());
            builder.builders.add(test);
            builder.builders.remove(test);
        } catch (Exception e) {
            throw new IllegalStateException("Ponder group {}'s builder list has prevented other mods from adding their own builders".replace("{}", builder.id.toString()), e);
        }
        return builder;
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
}
