package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99jClient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Inject(method = "keyPressed", at = @At(value = "TAIL"))
    public void lib99j$addDebugArgs(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        Lib99jClient.handlePonderKey(Lib99jClient.ponderKey.matches(event));
    }
}