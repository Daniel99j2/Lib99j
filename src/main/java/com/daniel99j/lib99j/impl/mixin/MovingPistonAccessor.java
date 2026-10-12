package com.daniel99j.lib99j.impl.mixin;

import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PistonMovingBlockEntity.class)
public interface MovingPistonAccessor {
    @Mutable
    @Accessor("progressO")
    void setProgressO(float value);
}