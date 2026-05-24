package com.daniel99j.lib99j.impl.mixin;

import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LodItemDisplayElement.class)
public interface LodDisplayAccessor {
    @Mutable
    @Accessor("nearDistanceSquared")
    void setNear(double nearDistanceSquared);

    @Mutable
    @Accessor("farDistanceSquared")
    void setFar(float farDistanceSquared);
}