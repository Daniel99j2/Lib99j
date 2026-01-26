package com.daniel99j.lib99j.testmod;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Objects;


public class TestBlock extends Block implements PolymerBlock, BlockWithElementHolder {
    public TestBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState());
    }

    @Override
    public MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, initialBlockState);
    }

    @Override
    public boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.TRIPWIRE.getDefaultState();
    }

    public static final class Model extends BlockModel {
        private final ItemDisplayElement mainElement;

        private Model(ServerWorld world, BlockState state) {
            this.mainElement = LodItemDisplayElement.createSimple(Items.TNT.getDefaultStack(), this.getUpdateRate(), 0.3f, 0.6f);
            this.mainElement.setTeleportDuration(0);
            this.addElement(this.mainElement);
            this.updateAnimation(world);
        }

        private int getUpdateRate() {
            return 1;
        }


        private void updateAnimation(World world) {
            this.mainElement.setItem(Items.TNT.getDefaultStack());
            this.mainElement.setInterpolationDuration(0);
            this.mainElement.setTeleportDuration(0);
            this.mainElement.setInvisible(true);
            this.mainElement.startInterpolation();
        }

        @Override
        protected void onTick() {
            if (!Objects.requireNonNull(this.blockAware()).isPartOfTheWorld()) {
                return;
            }
            var tick = Objects.requireNonNull(this.blockAware()).getWorld().getTime();
            if (tick % this.getUpdateRate() == 0) {
                this.updateAnimation(Objects.requireNonNull(Objects.requireNonNull(this.blockAware()).getWorld()));
                this.mainElement.startInterpolationIfDirty();
            }
        }
    }
}