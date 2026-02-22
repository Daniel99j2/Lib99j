package com.daniel99j.lib99j.impl.mixin;

import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundBossEventPacket.AddOperation.class)
public interface BossbarAccessor2 {
    @Mutable
    @Accessor("color")
    BossEvent.BossBarColor getColor();

    @Mutable
    @Accessor("color")
    void setColor(BossEvent.BossBarColor color);
}