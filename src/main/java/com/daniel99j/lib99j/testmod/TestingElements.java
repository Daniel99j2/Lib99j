package com.daniel99j.lib99j.testmod;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.FixedPolymerBlockItem;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.function.Function;

public class TestingElements {
    public static final Block TEST = registerBlock(
            "test_block",
            TestBlock::new,
            AbstractBlock.Settings.copy(Blocks.WHITE_WOOL).pistonBehavior(PistonBehavior.DESTROY).breakInstantly().noCollision().mapColor(MapColor.WHITE).sounds(BlockSoundGroup.WOOL)
    );

    public static void init() {}

    public static Block register(RegistryKey<Block> key, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        Block block = (Block)factory.apply(settings.registryKey(key));

        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Lib99j.MOD_ID, key.getValue().getPath()));

        Registry.register(Registries.ITEM, itemKey, new FixedPolymerBlockItem(block, new Item.Settings().registryKey(itemKey)));
        return Registry.register(Registries.BLOCK, key, block);
    }

    private static RegistryKey<Block> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Lib99j.MOD_ID, id));
    }

    private static Block registerBlock(String id, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        return register(keyOf(id), factory, settings);
    }
}
