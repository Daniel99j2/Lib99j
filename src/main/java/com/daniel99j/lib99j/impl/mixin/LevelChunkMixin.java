package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.impl.LevelChunkAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess implements LevelChunkAccessor {
    public LevelChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, PalettedContainerFactory palettedContainerFactory, long l, LevelChunkSection @Nullable [] levelChunkSections, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, palettedContainerFactory, l, levelChunkSections, blendingData);
    }

    @Override
    public void lib99j$setBlockReallyUnsafeDoNotUse(BlockPos blockPos, BlockState blockState) {
        int m;
        int j = blockPos.getY();
        LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndex(j));
        int k = blockPos.getX() & 0xF;
        BlockState blockState2 = levelChunkSection.setBlockState(k, j & 0xF, m = blockPos.getZ() & 0xF, blockState);
        if (blockState2 == blockState) {
            return;
        }
        //dont some heightmaps bc they dont affect ponder (afaik)
//        ((Heightmap)this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING)).update(k, j, m, blockState);
//        ((Heightmap)this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES)).update(k, j, m, blockState);
        ((Heightmap)this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR)).update(k, j, m, blockState);
//        ((Heightmap)this.heightmaps.get(Heightmap.Types.WORLD_SURFACE)).update(k, j, m, blockState);
    }
}