package com.daniel99j.lib99j.ponder.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record AnimatedRebuildChange(BlockPos blockPos, BlockState blockState, @Block.UpdateFlags int i, int j) {
}
