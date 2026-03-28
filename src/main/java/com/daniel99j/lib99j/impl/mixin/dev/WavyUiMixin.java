package com.daniel99j.lib99j.impl.mixin.dev;

import com.daniel99j.lib99j.api.ServerConfigCopy;
import net.minecraft.client.gui.components.AbstractWidget;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractWidget.class)
public abstract class WavyUiMixin {
    @Shadow
    private int x;

    @Shadow
    private int y;

    @Inject(method = "getX", at = @At("HEAD"), cancellable = true)
    private void lib99j$wavyUi(CallbackInfoReturnable<Integer> cir) {
        if(ServerConfigCopy.getConfigOption("lib99j", "wavy_ui", Boolean.class, false)) {
            cir.setReturnValue((int) (Math.sin(GLFW.glfwGetTime()+this.hashCode())*100)+this.x);
        }
    }

    @Inject(method = "getY", at = @At("HEAD"), cancellable = true)
    private void lib99j$wavyUi1(CallbackInfoReturnable<Integer> cir) {
        if(ServerConfigCopy.getConfigOption("lib99j", "wavy_ui", Boolean.class, false)) {
            cir.setReturnValue((int) (Math.cos(GLFW.glfwGetTime()+this.hashCode())*100)+this.y);
        }
    }
}