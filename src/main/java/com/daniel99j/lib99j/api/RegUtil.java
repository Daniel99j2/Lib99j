package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.Lib99j;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.function.BiFunction;
import java.util.function.Function;

public class RegUtil {
    private static String currentModNamespace = null;

    public static String currentNamespace() {
        return currentModNamespace;
    }

    public static String currentNamespaceSafe() {
        if (currentModNamespace == null) throw new IllegalStateException("The current namespace is not set");
        return currentModNamespace;
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(currentNamespaceSafe(), path);
    }

    public static void currentModNamespace(String namespace) {
        if(currentModNamespace != null) {
            throw new IllegalStateException("The current namespace is already set (trying to set to {})".replace("{}", currentModNamespace));
        } else {
            currentModNamespace = namespace;
            Lib99j.debug("Setting namespace to {}".replace("{}", currentModNamespace));
        }
    }

    public static void finishedWithNamespace(String namespace) {
        if(currentModNamespace == null) {
            throw new IllegalStateException("The current namespace is not set (trying to finish with {})".replace("{}", namespace));
        } else if(!currentModNamespace.equals(namespace)) {
            throw new IllegalStateException("The current namespace %s1 does not match with expected namespace %s2".replace("%s1", currentModNamespace).replace("%s2", namespace));
        } else {
            currentModNamespace = null;
            Lib99j.debug("Done with namespace {}".replace("{}", namespace));
        }
    }



    // ITEM MODIFIERS

    public static <T extends LootItemFunction> LootItemFunctionType<T> registerItemModifier(String id, MapCodec<T> codec) {
        LootItemFunctionType<T> out = (LootItemFunctionType) Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Identifier.fromNamespaceAndPath(RegUtil.currentNamespaceSafe(), id), new LootItemFunctionType(codec));
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.LOOT_FUNCTION_TYPE, out);
        return out;
    }

    // ITEMS

    public static Function<Item.Properties, Item> createBlockItemWithCustomItemName(Block block) {
        return (properties) -> new BlockItem(block, properties.useItemDescriptionPrefix());
    }

    private static ResourceKey<Item> namespaceItemId(String string) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(currentNamespaceSafe(), string));
    }

    private static ResourceKey<Item> blockIdToItemId(ResourceKey<Block> resourceKey) {
        return ResourceKey.create(Registries.ITEM, resourceKey.identifier());
    }

    public static Item registerSpawnEgg(EntityType<?> entityType) {
        return registerItem(ResourceKey.create(Registries.ITEM, EntityType.getKey(entityType).withSuffix("_spawn_egg")), SpawnEggItem::new, (new Item.Properties()).spawnEgg(entityType));
    }

    public static Item registerBlockItem(Block block) {
        return registerBlockItem(block, PolymerBlockItem::new);
    }

    public static Item registerBlockItem(Block block, Item.Properties properties) {
        return registerBlockItem(block, PolymerBlockItem::new, properties);
    }

    public static Item registerBlockItem(Block block, Block... blocks) {
        Item item = registerBlockItem(block);

        for(Block block2 : blocks) {
            Item.BY_BLOCK.put(block2, item);
        }

        return item;
    }

    public static Item registerBlockItem(Block block, BiFunction<Block, Item.Properties, Item> biFunction) {
        return registerBlockItem(block, biFunction, new Item.Properties());
    }

    public static Item registerBlockItem(Block block, BiFunction<Block, Item.Properties, Item> biFunction, Item.Properties properties) {
        return registerItem((ResourceKey)blockIdToItemId(block.builtInRegistryHolder().key()), (propertiesx) -> (Item)biFunction.apply(block, propertiesx), properties.useBlockDescriptionPrefix());
    }

    public static Item registerItem(String string, Function<Item.Properties, Item> function) {
        return registerItem(namespaceItemId(string), function, new Item.Properties());
    }

    public static Item registerItem(String string, Function<Item.Properties, Item> function, Item.Properties properties) {
        return registerItem(namespaceItemId(string), function, properties);
    }

    public static Item registerItem(String string, Item.Properties properties) {
        return registerItem(namespaceItemId(string), SimplePolymerItem::new, properties);
    }

    public static Item registerItem(String string) {
        return registerItem(namespaceItemId(string), SimplePolymerItem::new, new Item.Properties());
    }

    public static Item registerItem(ResourceKey<Item> resourceKey, Function<Item.Properties, Item> function) {
        return registerItem(resourceKey, function, new Item.Properties());
    }

    public static Item registerItem(ResourceKey<Item> resourceKey, Function<Item.Properties, Item> function, Item.Properties properties) {
        Item item = (Item)function.apply(properties.setId(resourceKey));
        if (item instanceof BlockItem blockItem) {
            blockItem.registerBlocks(Item.BY_BLOCK, item);
        }

        return (Item)Registry.register(BuiltInRegistries.ITEM, resourceKey, item);
    }

    // BLOCKS
    public static Block registerStair(String string, Block block) {
        return registerBlock(string, (properties) -> new StairBlock(block.defaultBlockState(), properties), BlockBehaviour.Properties.ofFullCopy(block));
    }

    public static Block registerBlock(ResourceKey<Block> resourceKey, Function<BlockBehaviour.Properties, Block> function, BlockBehaviour.Properties properties) {
        Block block = (Block)function.apply(properties.setId(resourceKey));
        return (Block)Registry.register(BuiltInRegistries.BLOCK, resourceKey, block);
    }

    private static ResourceKey<Block> namespaceBlockId(String string) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(currentNamespaceSafe(), string));
    }

    public static Block registerBlock(String string, Function<BlockBehaviour.Properties, Block> function, BlockBehaviour.Properties properties) {
        return registerBlock(namespaceBlockId(string), function, properties);
    }

    public static Block registerBlock(String string, BlockBehaviour.Properties properties, Block polymerBlock) {
        return registerBlock(string, (properties1 -> new SimplePolymerBlock(properties1, polymerBlock)), properties);
    }

    // ENTITY TYPE
    public static <T extends Entity> EntityType<T> register(String path, FabricEntityTypeBuilder<T> item) {
        var x = Registry.register(BuiltInRegistries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(RegUtil.currentNamespaceSafe(), path), item.build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Identifier.fromNamespaceAndPath(RegUtil.currentNamespaceSafe(), path))));
        PolymerEntityUtils.registerType(x);
        return x;
    }

    // POTIONS
    public static Holder<Potion> registerPotion(String name, Potion out, Item in1, Holder<Potion> in2) {
        Holder<Potion> registry = Registry.registerForHolder(BuiltInRegistries.POTION, Identifier.fromNamespaceAndPath(RegUtil.currentNamespaceSafe(), name), out);
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.addMix(in2, in1, registry));
        return registry;
    }

    public static Holder<MobEffect> registerEffect(String name, MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(RegUtil.currentNamespaceSafe(), name), effect);
    }

}
