package com.daniel99j.lib99j.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;

public class PonderChunkGenerator extends EmptyChunkGenerator {
    private final int roof;

    public PonderChunkGenerator(ResourceKey<Biome> filledBiome, int roof) {
        super(filledBiome);
        this.roof = roof;
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState noiseConfig, ChunkAccess chunk) {
        for (int x = 0; x < 15; x++) {
            for (int z = 0; z < 15; z++) {
                chunk.setBlockState(new BlockPos(x, roof, z), Blocks.BARRIER.defaultBlockState());
            }
        }
    }
}
