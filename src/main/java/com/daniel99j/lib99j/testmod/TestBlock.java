package com.daniel99j.lib99j.testmod;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Objects;

@ApiStatus.Internal
public class TestBlock extends Block implements PolymerBlock, BlockWithElementHolder {
    public TestBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any());
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, initialBlockState);
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.TRIPWIRE.defaultBlockState();
    }

    public static final class Model extends BlockModel {
        private final ItemDisplayElement mainElement;

        private Model(ServerLevel world, BlockState state) {
            this.mainElement = LodItemDisplayElement.createSimple(Items.TNT.getDefaultInstance(), this.getUpdateRate(), 0.3f, 0.6f);
            this.mainElement.setTeleportDuration(0);
            this.addElement(this.mainElement);
            this.updateAnimation(world);
        }

        private int getUpdateRate() {
            return 1;
        }


        private void updateAnimation(Level world) {
            this.mainElement.setItem(Items.TNT.getDefaultInstance());
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
            var tick = Objects.requireNonNull(this.blockAware()).getWorld().getGameTime();
            if (tick % this.getUpdateRate() == 0) {
                this.updateAnimation(Objects.requireNonNull(Objects.requireNonNull(this.blockAware()).getWorld()));
                this.mainElement.startInterpolationIfDirty();
            }
        }
    }
}