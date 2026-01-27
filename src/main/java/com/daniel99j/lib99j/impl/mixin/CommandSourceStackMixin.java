package com.daniel99j.lib99j.impl.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Set;

@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin {
    @Inject(method = "levels", at = @At("TAIL"), cancellable = true)
    private static void lib99j$hideDataDimension(CallbackInfoReturnable<Set<ResourceKey<Level>>> cir) {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) return;
        var edited = cir.getReturnValue();
        edited.removeIf((key) -> Objects.equals(key.identifier().getNamespace(), "ponder"));
        cir.setReturnValue(edited);
    }
}