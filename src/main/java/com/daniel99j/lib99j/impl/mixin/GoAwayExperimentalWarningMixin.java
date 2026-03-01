package com.daniel99j.lib99j.impl.mixin;

import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(BackupConfirmScreen.class)
public abstract class GoAwayExperimentalWarningMixin extends Screen {
    @Mutable
    @Shadow @Final
    protected final BackupConfirmScreen.Listener onProceed;

    @Unique
    private boolean shouldRemove = false;

    protected GoAwayExperimentalWarningMixin(Component title, BackupConfirmScreen.Listener callback) {
        super(title);
        this.onProceed = callback;
    }

    @Inject(method = "init()V", at = @At("HEAD"), cancellable = true)
    public void fuckOff(CallbackInfo ci) {
        if(Objects.equals(this.title, Component.translatable("selectWorld.backupQuestion.experimental"))) {
            this.onProceed.proceed(false, false);
            shouldRemove = true;
            ci.cancel();
        };
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void fuckOff1(CallbackInfo ci) {
        if(shouldRemove) ci.cancel();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void fuckOff2(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if(shouldRemove) cir.setReturnValue(super.keyPressed(keyEvent));
    }
}