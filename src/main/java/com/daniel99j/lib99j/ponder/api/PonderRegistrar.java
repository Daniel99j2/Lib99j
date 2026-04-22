package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.lib99j.api.GameProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.ArrayList;
import java.util.Objects;

public class PonderRegistrar {
    protected PonderRegistrar() {

    }

    /**
     * Registers the builder from an item
     * <p>When pondering about an item, a set of rules is used to determine which ponder should show up first:</p>
     * <p>In general, the first registered association will be used</p>
     * <p>If the item namespace is equal to the id namespace, this will take priority over other registered associations</p>
     * @throws IllegalStateException When a ponder builder has been marked as hidden in commands (use registerBuilder() instead)
     */
    public PonderBuilder registerItemToBuilder(Item item, PonderBuilder builder) {
        if(builder.shouldHideFromCommands()) throw new IllegalStateException("Ponder builder {} is hidden, therefor cannot be added to an item".replace("{}", builder.id.toString()));
        if(builder.item == null) builder.item = item;
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        boolean fromNameSpace = Objects.equals(itemId.getNamespace(), builder.id.getNamespace());

        registerBuilder(builder);

        if (!PonderManager.itemToBuilders.containsKey(item)) {
            PonderManager.itemToBuilders.put(item, new ArrayList<>());
        }
        if (fromNameSpace && !PonderManager.itemToBuilders.get(item).isEmpty() && (!Objects.equals(PonderManager.idToBuilder.get(PonderManager.itemToBuilders.get(item).getFirst()).id.getNamespace(), itemId.getNamespace()))) {
            PonderManager.itemToBuilders.get(item).addFirst(builder.id);
            builder.item = item;
        } else PonderManager.itemToBuilders.get(item).add(builder.id);

        Identifier groupId = Identifier.fromNamespaceAndPath("_item_"+BuiltInRegistries.ITEM.getKey(builder.item).getNamespace(), BuiltInRegistries.ITEM.getKey(builder.item).getPath());

        if(!PonderManager.idToGroup.containsKey(groupId)) registerGroup(new PonderGroup(groupId, new ItemStackTemplate(builder.item), new ArrayList<>()));

        PonderManager.idToGroup.get(groupId).builders.add(builder);

        return builder;
    }

    /**
     * Registers the builder from an ID
     * <p>Don't use this for ponders relating to items - see registerItemToBuilder instead
     *
     * @throws IllegalStateException When a ponder builder has already been registered with this ID
     */
    public PonderBuilder registerBuilder(PonderBuilder builder) {
        if(registerIdToBuilderNoThrow(builder)) return builder;
        throw new IllegalStateException("A ponder builder is already registered for {}".replace("{}", builder.id.toString()));
    }

    /**
     * Registers the builder from an ID. Note this is not actually deprecated, but is just recommended not to use
     * <p>See registerIdToBuilder() instead
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public boolean registerIdToBuilderNoThrow(PonderBuilder builder) {
        if(!PonderManager.isLoading) throw new IllegalStateException("Ponder builder {} was registered outside of the load event".replace("{}", builder.id.toString()));
        if(PonderManager.idToBuilder.containsKey(builder.id) && PonderManager.idToBuilder.get(builder.id).equals(builder)) return true; //if its already registered allow it so that multiple items can use a single ponder
        if(PonderManager.idToBuilder.containsKey(builder.id)) return false;
        PonderManager.idToBuilder.put(builder.id, builder);
        builder.registered = true;
        return true;
    }

    /**
     * Registers a ponder group
     * <p>These groups are accessed through the ponder menu (L), and allow for associated groups and scenes to a specific scene to be accessed without using commands
     *
     * @throws IllegalStateException When a ponder builder has already been registered with this ID
     */
    public PonderGroup registerGroup(PonderGroup builder) {
        GameProperties.throwIfPonderNotEnabled("Ponder has not been enabled! Use GameProperties.enablePonder()");
        if(!PonderManager.isLoading) throw new IllegalStateException("Ponder group {} was registered outside of the load event".replace("{}", builder.id.toString()));

        if(PonderManager.idToGroup.containsKey(builder.id)) throw new IllegalStateException("A ponder builder is already registered for {}".replace("{}", builder.id.toString()));
        PonderManager.idToGroup.put(builder.id, builder);
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

}
