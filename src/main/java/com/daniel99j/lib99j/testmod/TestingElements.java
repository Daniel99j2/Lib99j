package com.daniel99j.lib99j.testmod;

import com.daniel99j.lib99j.api.RegUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class TestingElements {
    public static final Block TEST = RegUtil.registerBlock(
            "test_block",
            TestBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).pushReaction(PushReaction.DESTROY).instabreak().noCollision().mapColor(MapColor.SNOW).sound(SoundType.WOOL)
    );

    public static final Item TEST_BLOCKITEM = RegUtil.registerBlockItem(
            TEST
    );

    public static void init() {}
}
