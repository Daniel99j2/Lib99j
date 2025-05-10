package com.daniel99j.lib99j.api;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public class BlockUtils {

    public static BlockState resetMultiProperty(BlockState state) {
        BlockState state1 = state;
        state1 = state1.with(Properties.UP, false);
        state1 = state1.with(Properties.DOWN, false);
        state1 = state1.with(Properties.NORTH, false);
        state1 = state1.with(Properties.SOUTH, false);
        state1 = state1.with(Properties.EAST, false);
        state1 = state1.with(Properties.WEST, false);
        return state1;
    }

    public static BlockState directionToMultiProperty(BlockState state, Direction facing) {
        BlockState state1 = state;
        if (facing == Direction.UP) state1 = state1.with(Properties.UP, true);
        if (facing == Direction.DOWN) state1 = state1.with(Properties.DOWN, true);
        if (facing == Direction.NORTH) state1 = state1.with(Properties.NORTH, true);
        if (facing == Direction.SOUTH) state1 = state1.with(Properties.SOUTH, true);
        if (facing == Direction.EAST) state1 = state1.with(Properties.EAST, true);
        if (facing == Direction.WEST) state1 = state1.with(Properties.WEST, true);
        return state1;
    }
}
