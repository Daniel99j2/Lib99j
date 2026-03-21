package com.daniel99j.lib99j.impl.mixin.dev;

import com.daniel99j.lib99j.Lib99j;
import net.minecraft.server.dedicated.ServerWatchdog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWatchdog.class)
public abstract class DogBoneMixin {
    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private static void noWatchDog(CallbackInfo ci) {
        if(Lib99j.isDevelopingLib99j) ci.cancel();
    }
}