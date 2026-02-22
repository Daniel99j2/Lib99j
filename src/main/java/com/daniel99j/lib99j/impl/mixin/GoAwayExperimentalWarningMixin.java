package com.daniel99j.lib99j.impl.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(BackupConfirmScreen.class)
public abstract class GoAwayExperimentalWarningMixin extends Screen {
    @Mutable
    @Shadow @Final
    protected final BackupConfirmScreen.Listener onProceed;

    protected GoAwayExperimentalWarningMixin(Component title, BackupConfirmScreen.Listener callback) {
        super(title);
        this.onProceed = callback;
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    public void fuckOff(CallbackInfo ci) {
        if(Objects.equals(this.title, Component.translatable("selectWorld.backupQuestion.experimental"))) this.onProceed.proceed(false, false);
    }
}