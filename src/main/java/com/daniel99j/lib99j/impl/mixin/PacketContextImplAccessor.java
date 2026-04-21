package com.daniel99j.lib99j.impl.mixin;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.networking.context.PacketContextImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(PacketContextImpl.class)
public interface PacketContextImplAccessor {
    @Mutable
    @Accessor("contextMap")
    Map<PacketContext.Key<?>, Object> getMap();
}