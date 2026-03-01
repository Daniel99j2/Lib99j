package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.PonderChunkGenerator;
import com.daniel99j.lib99j.ponder.api.PonderScene;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class PonderLevel extends ServerLevel {
    private final PonderScene scene;
    //distance of how far auto-offseting goes to
    private static final int MAX_ALLOWED_DISTANCE = 150;

    public PonderLevel(PonderScene scene, ServerLevel playerLevel, ResourceKey<Level> resourceKey, ResourceKey<Biome> biomeResourceKey) {
        super(Lib99j.getServerOrThrow(),
                Lib99j.getServerOrThrow().executor,
                Lib99j.getServerOrThrow().storageSource,
                new PonderLevelData(), resourceKey, new LevelStem(playerLevel.dimensionTypeRegistration(), new PonderChunkGenerator(biomeResourceKey, playerLevel.getHeight())),
                false,
                0,
                List.of(),
                true,
                new RandomSequences());
        this.scene = scene;
    }

    @Override
    public boolean noSave() {
        return true;
    }

    @Override // only tick when I want it to
    public void tick(BooleanSupplier booleanSupplier) {

    }

    @ApiStatus.Internal
    public void runTick() {
        super.tick(() -> true);
    }

    @ApiStatus.Internal
    public void syncRain() {
        this.oRainLevel = -1000;
        this.oThunderLevel = -1000;
    }

    public void fillBlocks(BlockPos start, BlockPos end, BlockState blockState) {
        for (int x = start.getX(); x < end.getX(); x++) {
            for (int y = start.getY(); y < end.getY(); y++) {
                for (int z = start.getZ(); z < end.getZ(); z++) {
                    setBlockAndUpdate(new BlockPos(x, y, z), blockState);
                }
            }
        }
    }

    //These make it so a 0,0,0 origin can be used if wanted
    @Override
    public boolean addFreshEntity(Entity entity) {
        entity.setPos(newPos(entity.position()));
        return super.addFreshEntity(entity);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos blockPos) {
        return super.getBlockEntity(newPos(blockPos));
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return super.getBlockState(newPos(blockPos));
    }

    @Override
    public boolean setBlock(BlockPos blockPos, BlockState blockState, @Block.UpdateFlags int i, int j) {
        BlockPos newPos = newPos(blockPos);
        if(!scene.canBreakFloor && newPos.getY() < scene.getOrigin().getY()) return false;
        return super.setBlock(newPos, blockState, i, j);
    }

    @Override
    public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos blockPos, BlockEntityType<T> blockEntityType) {
        return super.getBlockEntity(newPos(blockPos), blockEntityType);
    }

    @Override
    public boolean destroyBlock(BlockPos blockPos, boolean var2, @Nullable Entity var3, int var4) {
        BlockPos newPos = newPos(blockPos);
        if(!scene.canBreakFloor && newPos.getY() < scene.getOrigin().getY()) return false;
        return super.destroyBlock(newPos, var2, var3, var4);
    }

    private Vec3 newPos(Vec3 position) {
        if(position.length() < MAX_ALLOWED_DISTANCE) {
            return position.add(Vec3.atLowerCornerOf(this.scene.getOrigin()));
        }
        return position;
    }

    private BlockPos newPos(BlockPos position) {
        if(Math.sqrt(position.getX() * position.getX() + position.getY() * position.getY() + position.getZ() * position.getZ()) < MAX_ALLOWED_DISTANCE) {
            return position.offset(this.scene.getOrigin());
        }
        return position;
    }
}
