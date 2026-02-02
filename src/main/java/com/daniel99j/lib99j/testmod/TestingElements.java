package com.daniel99j.lib99j.testmod;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.FixedPolymerBlockItem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Function;

public class TestingElements {
    public static final Block TEST = registerBlock(
            "test_block",
            TestBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).pushReaction(PushReaction.DESTROY).instabreak().noCollision().mapColor(MapColor.SNOW).sound(SoundType.WOOL)
    );

    public static void init() {}

    public static Block register(ResourceKey<Block> key, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        if(!FabricLoader.getInstance().isDevelopmentEnvironment()) return null;
        Block block = (Block)factory.apply(settings.setId(key));

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, key.identifier().getPath()));

        Registry.register(BuiltInRegistries.ITEM, itemKey, new FixedPolymerBlockItem(block, new net.minecraft.world.item.Item.Properties().setId(itemKey)));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    private static ResourceKey<Block> keyOf(String id) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, id));
    }

    private static Block registerBlock(String id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        return register(keyOf(id), factory, settings);
    }
}
