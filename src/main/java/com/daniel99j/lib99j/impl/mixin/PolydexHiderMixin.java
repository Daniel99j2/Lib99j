package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.ponder.api.PonderManager;
import eu.pb4.polydex.impl.display.PolydexTargetImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PolydexTargetImpl.class)
public abstract class PolydexHiderMixin {
    @Shadow
    public abstract ServerPlayer player();

    @Shadow
    private Entity entity;

    @Shadow
    private BlockState cachedBlockState;

    @Shadow
    private @Nullable BlockEntity cachedBlockEntity;

    @Shadow
    private @Nullable HitResult hitResult;

    @Inject(method = "updateRaycast", at = @At("HEAD"), cancellable = true)
    private void lib99j$hideOverlay(CallbackInfo ci) {
        if(PonderManager.isPondering(this.player())) {
            this.hitResult = BlockHitResult.miss(Vec3.ZERO, Direction.NORTH, BlockPos.ZERO);
            this.cachedBlockState = Blocks.AIR.defaultBlockState();
            this.cachedBlockEntity = null;
            this.entity = null;
            ci.cancel();
        }
    }
}