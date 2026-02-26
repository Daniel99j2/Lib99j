package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.lib99j.Lib99j;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PonderManager {
    public static Map<ServerPlayer, PonderScene> activeScenes = new HashMap<>();

    @ApiStatus.Internal
    public static Map<Item, List<PonderBuilder>> itemToBuilders = new HashMap<>();
    @ApiStatus.Internal
    public static Map<Identifier, PonderBuilder> idToBuilder = new HashMap<>();

    public static boolean isPondering(ServerPlayer player) {
        return activeScenes.containsKey(player) && !activeScenes.get(player).isToBeRemoved();
    }

    @ApiStatus.Internal
    public static void tick() {
        activeScenes.entrySet().removeIf((scene) -> {
            boolean toRemove = scene.getValue().isToBeRemoved();
            if(toRemove) Lib99j.getServerOrThrow().levels.remove(scene.getValue().worldKey);
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
     * <p>If the Identifier other is not null and the item namespace is equal to the other namespace, this will take priority over other registered associations</p>
     * @param other If not null, the ID to use in addition to the item. Also is used to determine source
     */
    public static PonderBuilder registerItemToBuilder(Item item, PonderBuilder builder, @Nullable Identifier other) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        boolean fromNameSpace = false;
        if(other != null) {
            //if the builder already has a source then dont try and override it
            //you really shouldnt be using multiple namespaces on items for one ponder, but ill allow it
            if(builder.sourceNamespace == null) {
                builder.sourceNamespace = other.getNamespace();
                if(Objects.equals(itemId.getNamespace(), other.getNamespace())) fromNameSpace = true;
            };
            registerIdToBuilder(other, builder);
        }
        if(!itemToBuilders.containsKey(item)) {
            itemToBuilders.put(item, new ArrayList<>());
        };
        if(fromNameSpace && !itemToBuilders.get(item).isEmpty() && (!Objects.equals(itemToBuilders.get(item).getFirst().sourceNamespace, itemId.getNamespace()))) {
            itemToBuilders.get(item).addFirst(builder);
        } else itemToBuilders.get(item).add(builder);
        return builder;
    }

    /**
     * Registers the builder from an ID
     * <p>Don't use this for ponders relating to items - see registerItemToBuilder instead
     * @throws IllegalStateException When a ponder builder has already been registered with this ID
     */
    public static PonderBuilder registerIdToBuilder(Identifier id, PonderBuilder builder) {
        if(registerIdToBuilderNoThrow(id, builder)) return builder;
        throw new IllegalStateException("A ponder builder is already registered for {}".replace("{}", id.toString()));
    }

    /**
    * Registers the builder from an ID. Note this is not actually deprecated, but is just recommended not to use
     * <p>See registerIdToBuilder() instead
    */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static boolean registerIdToBuilderNoThrow(Identifier id, PonderBuilder builder) {
        if(idToBuilder.containsKey(id)) return false;
        idToBuilder.put(id, builder);
        builder.registered = true;
        return true;
    }
}
