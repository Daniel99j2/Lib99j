package com.daniel99j.lib99j.impl.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfirmScreen.class)
public abstract class CommandExecutionScreenMixin {
    @Shadow
    @Final
    private Component message;

    @Shadow
    @Final
    protected LinearLayout layout;

    @Shadow
    @Final
    protected BooleanConsumer callback;

    @Inject(method = "addButtons", at = @At(value = "TAIL"))
    private void lib99j$addCopyButton(final LinearLayout buttonLayout, CallbackInfo ci) {
        if(((ConfirmScreen) (Object) this).getTitle().getContents() instanceof TranslatableContents translatableContents) {
            if(translatableContents.getKey().equals("multiplayer.confirm_command.title") && this.message.getContents() instanceof TranslatableContents messageTranslation) {
                buttonLayout.addChild(Button.builder(Component.translatable("chat.copy"), (button) -> {
                    Minecraft.getInstance().keyboardHandler.setClipboard(messageTranslation.getArgument(0).getString());
                    this.callback.accept(false);
                }).build());
            }
        }
    }
}