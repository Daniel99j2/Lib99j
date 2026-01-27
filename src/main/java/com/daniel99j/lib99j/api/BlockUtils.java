package com.daniel99j.lib99j.api;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

@SuppressWarnings({"unused"})
public class BlockUtils {
    public static BlockState resetMultiProperty(BlockState state) {
        BlockState state1 = state;
        state1 = state1.setValue(BlockStateProperties.UP, false);
        state1 = state1.setValue(BlockStateProperties.DOWN, false);
        state1 = state1.setValue(BlockStateProperties.NORTH, false);
        state1 = state1.setValue(BlockStateProperties.SOUTH, false);
        state1 = state1.setValue(BlockStateProperties.EAST, false);
        state1 = state1.setValue(BlockStateProperties.WEST, false);
        return state1;
    }

    public static BlockState directionToMultiProperty(BlockState state, Direction facing) {
        BlockState state1 = state;
        if (facing == Direction.UP) state1 = state1.setValue(BlockStateProperties.UP, true);
        if (facing == Direction.DOWN) state1 = state1.setValue(BlockStateProperties.DOWN, true);
        if (facing == Direction.NORTH) state1 = state1.setValue(BlockStateProperties.NORTH, true);
        if (facing == Direction.SOUTH) state1 = state1.setValue(BlockStateProperties.SOUTH, true);
        if (facing == Direction.EAST) state1 = state1.setValue(BlockStateProperties.EAST, true);
        if (facing == Direction.WEST) state1 = state1.setValue(BlockStateProperties.WEST, true);
        return state1;
    }
}
