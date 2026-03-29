package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.GoToThenStopGoal;
import com.daniel99j.lib99j.impl.LevelChunkAccessor;
import com.daniel99j.lib99j.impl.mixin.GoalSelectorAccessor;
import com.daniel99j.lib99j.ponder.api.PonderScene;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
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
                new PonderLevelData(), resourceKey, new LevelStem(Holder.direct(new DimensionType(
                        true,
                        true,
                        false,
                        false,
                        1,
                        playerLevel.dimensionTypeRegistration().value().minY(),
                        playerLevel.dimensionTypeRegistration().value().height(),
                        playerLevel.dimensionTypeRegistration().value().logicalHeight(),
                        Lib99j.getServerOrThrow().overworld().dimensionType().infiniburn(),
                        1.0f, new DimensionType.MonsterSettings(ConstantInt.of(0), 0), DimensionType.Skybox.NONE, CardinalLighting.Type.DEFAULT, EnvironmentAttributeMap.EMPTY, HolderSet.empty(), Lib99j.getServerOrThrow().overworld().dimensionType().defaultClock())), new PonderChunkGenerator(biomeResourceKey, playerLevel.getMaxY())),
                false,
                0,
                List.of(),
                true
        );
        this.scene = scene;
    }

    @Override
    public boolean noSave() {
        return true;
    }

    @Override // only tick when I want it to
    public void tick(@NonNull BooleanSupplier booleanSupplier) {

    }

    @ApiStatus.Internal
    public void runTick() {
        try {
            super.tick(() -> true);
        } catch (Exception e) {
            this.scene.player.sendSystemMessage(Component.translatable("ponder.scene.error_ticking_world").withStyle(ChatFormatting.RED));
            this.scene.stopPonderingSafely();
            Lib99j.LOGGER.error("Error whilst pondering in scene {}", this.scene.builder.getId(), e);
        }
    }

    public void syncRain() {
        if (this.isRaining()) {
            this.scene.packetRedirector.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
        } else {
            this.scene.packetRedirector.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F));
        }

        this.scene.packetRedirector.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, this.rainLevel));
        this.scene.packetRedirector.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, this.thunderLevel));
    }

    public void fillBlocksAndUpdate(BlockPos start, BlockPos end, BlockState blockState) {
        fillBlocks(start, end, blockState, false);
    }

    public void fillBlocks(BlockPos start, BlockPos end, BlockState blockState, boolean skipAllUpdates) {
        //Enable filling blocks outside as this is fully intentional and cannot be called by the world
        boolean old = this.scene.canEscapeBounds;
        this.scene.canEscapeBounds = true;

        for (int x = start.getX(); x <= end.getX(); x++) {
            for (int y = start.getY(); y <= end.getY(); y++) {
                for (int z = start.getZ(); z <= end.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!skipAllUpdates) setBlockAndUpdate(pos, blockState);
                    else {
                        ((LevelChunkAccessor) this.getChunkAt(pos)).lib99j$setBlockReallyUnsafeDoNotUse(pos, blockState);
                    }
                }
            }
        }

        this.scene.canEscapeBounds = old;
    }

    public void removeBlockWithParticles(BlockPos pos) {
        pos = newPos(pos);
        BlockState old = this.getBlockState(pos);
        old.getBlock().playerWillDestroy(this, pos, old, scene.packetRedirector);
        this.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    public void makeEntityDumb(Mob entity) {
        entity.removeFreeWill();
    }

    public void makeEntityPathfindTo(PathfinderMob entity, BlockPos pos) {
        ((GoalSelectorAccessor) entity).getGoalSelector().addGoal(-100, new GoToThenStopGoal(entity, newPos(pos)));
    }

    //These make it so a 0,0,0 origin can be used if wanted
    @Override
    public boolean addFreshEntity(Entity entity) {
        Vec3 newPos = newPos(entity.position());
        //when adding entities this allows them to drop in
        if (!isValidPos(BlockPos.containing(newPos), true)) {
            if(Lib99j.isDevelopmentEnvironment) Lib99j.LOGGER.warn("Tried to add entity outside scene at {}: {}", entity.position(), entity.getType());
            return false;
        };
        entity.setPos(newPos);
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
        if (!isValidPos(newPos, false)) {
            if(Lib99j.isDevelopmentEnvironment) Lib99j.LOGGER.warn("Tried to set block outside scene at {}: {}", blockPos, blockState);
            return false;
        };
        return super.setBlock(newPos, blockState, i, j);
    }

    @Override
    public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos blockPos, BlockEntityType<T> blockEntityType) {
        return super.getBlockEntity(newPos(blockPos), blockEntityType);
    }

    @Override
    public boolean destroyBlock(BlockPos blockPos, boolean var2, @Nullable Entity var3, int var4) {
        BlockPos newPos = newPos(blockPos);
        if (!isValidPos(newPos, false)) {
            if(Lib99j.isDevelopmentEnvironment) Lib99j.LOGGER.warn("Tried to destroy block outside scene at {}", blockPos);
            return false;
        };
        return super.destroyBlock(newPos, var2, var3, var4);
    }

    private Vec3 newPos(Vec3 position) {
        if (position.length() < MAX_ALLOWED_DISTANCE) {
            return position.add(Vec3.atLowerCornerOf(this.scene.getOrigin()));
        }
        return position;
    }

    private BlockPos newPos(BlockPos position) {
        if (Math.sqrt(position.getX() * position.getX() + position.getY() * position.getY() + position.getZ() * position.getZ()) < MAX_ALLOWED_DISTANCE) {
            return position.offset(this.scene.getOrigin());
        }
        return position;
    }

    private boolean isValidPos(BlockPos newPos, boolean allowOverY) {
        return scene.canEscapeBounds || (
                newPos.getX() >= scene.getOrigin().getX()
                && newPos.getX() <= scene.getOrigin().getX()+scene.builder.getSize().getX()
                && newPos.getY() >= scene.getOrigin().getY()
                && (newPos.getY() <= scene.getOrigin().getY()+scene.builder.getSize().getY() || allowOverY)
                && newPos.getZ() >= scene.getOrigin().getZ()
                && newPos.getZ() <= scene.getOrigin().getZ()+scene.builder.getSize().getZ());
    }

    public PonderScene getScene() {
        return scene;
    }
}
