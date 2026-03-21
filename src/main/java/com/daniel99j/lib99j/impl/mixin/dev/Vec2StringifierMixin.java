package com.daniel99j.lib99j.impl.mixin.dev;

import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec2.class)
public abstract class Vec2StringifierMixin {
    @Shadow
    @Final
    public float x;

    @Shadow
    @Final
    public float y;

    @Override
    public String toString() {
        return "Vec2{x="+this.x+",y="+this.y+"}";
    }
}