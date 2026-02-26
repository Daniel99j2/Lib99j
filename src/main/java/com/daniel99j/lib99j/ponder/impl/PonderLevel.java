package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.EmptyChunkGenerator;
import com.daniel99j.lib99j.ponder.api.PonderScene;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class PonderLevel extends ServerLevel {
    private final PonderScene scene;
    private static final int MAX_ALLOWED_DISTANCE = 150;

    public PonderLevel(PonderScene scene, ServerLevel playerLevel, ResourceKey<Level> resourceKey, ResourceKey<Biome> biomeResourceKey) {
        super(Lib99j.getServerOrThrow(), Lib99j.getServerOrThrow().executor, Lib99j.getServerOrThrow().storageSource, new PrimaryLevelData(new LevelSettings("Ponder (Temporary world)", GameType.CREATIVE, playerLevel.getLevelData().isHardcore(), playerLevel.getDifficulty(), true, playerLevel.getGameRules(), WorldDataConfiguration.DEFAULT), new WorldOptions(0, false, false), PrimaryLevelData.SpecialWorldProperty.FLAT, Lifecycle.experimental()), resourceKey, new LevelStem(playerLevel.dimensionTypeRegistration(), new EmptyChunkGenerator(biomeResourceKey, 0)), false, 0, List.of(), true, new RandomSequences());
        this.scene = scene;
    }

    @Override
    public boolean noSave() {
        return true;
    }

    @Override // only tick when I want it to
    public void tick(BooleanSupplier booleanSupplier) {

    }

    public void runTick() {
        super.tick(() -> true);
    }

    //These make it so a 0,0,0 position can be used if wanted
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
        return super.setBlock(newPos(blockPos), blockState, i, j);
    }

    @Override
    public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos blockPos, BlockEntityType<T> blockEntityType) {
        return super.getBlockEntity(newPos(blockPos), blockEntityType);
    }

    @Override
    public boolean destroyBlock(BlockPos blockPos, boolean var2, @Nullable Entity var3, int var4) {
        return super.destroyBlock(newPos(blockPos), var2, var3, var4);
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
