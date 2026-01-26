package com.daniel99j.lib99j.impl.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Set;

@Mixin(ServerCommandSource.class)
public abstract class ServerCommandSourceMixin {
    @Inject(method = "getWorldKeys", at = @At("TAIL"), cancellable = true)
    private static void lib99j$hideDataDimension(CallbackInfoReturnable<Set<RegistryKey<World>>> cir) {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) return;
        var edited = cir.getReturnValue();
        edited.removeIf((key) -> Objects.equals(key.getValue().getNamespace(), "ponder"));
        cir.setReturnValue(edited);
    }
}