package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.impl.BossBarVisibility;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientboundBossEventPacket.class)
public abstract class BossbarPacketMixin implements BossBarVisibility {
    @Unique
    private boolean lib99j$visible = true;

    @Inject(method = "createAddPacket", at = @At("TAIL"))
    private static void hideIfInvisible(BossEvent bossEvent, CallbackInfoReturnable<ClientboundBossEventPacket> cir) {
        if(bossEvent instanceof BossBarVisibility visibility) {
            ((BossBarVisibility) cir.getReturnValue()).lib99j$setVisible(visibility.lib99j$isVisible());
        }
    }

    @Inject(method = "createUpdateStylePacket", at = @At("TAIL"))
    private static void hideIfInvisible1(BossEvent bossEvent, CallbackInfoReturnable<ClientboundBossEventPacket> cir) {
        if(bossEvent instanceof BossBarVisibility visibility) {
            ((BossBarVisibility) cir.getReturnValue()).lib99j$setVisible(visibility.lib99j$isVisible());
        }
    }

    @Override
    public boolean lib99j$isVisible() {
        return this.lib99j$visible;
    }

    @Override
    public void lib99j$setVisible(boolean visible) {
        this.lib99j$visible = visible;
    }
}