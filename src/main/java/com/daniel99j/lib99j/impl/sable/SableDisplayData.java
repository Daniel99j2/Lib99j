package com.daniel99j.lib99j.impl.sable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record SableDisplayData(int blockId, Integer[] collisionIds, Vec3[] collisionOffsets, BlockPos pos, Vec3 offset, List<CustomSableDisplay> customSableDisplayList) {
}
