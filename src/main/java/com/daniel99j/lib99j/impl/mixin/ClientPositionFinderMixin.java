package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.ponder.api.PonderCoordUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class ClientPositionFinderMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;

    @Unique
    private boolean isDialog = false;
    @Unique
    private final WidgetTooltipHolder tooltipHolder = new WidgetTooltipHolder();
    @Unique
    private Vector2i pos = new Vector2i(0);

    @Inject(method = "init()V", at = @At("TAIL"))
    private void lib99j$cursorPosA2dd(CallbackInfo ci) {
        //noinspection ConstantValue
        isDialog = ((Object) this) instanceof DialogScreen<?> dialogScreen && dialogScreen.getTitle().getContents() instanceof PlainTextContents plainTextContents && plainTextContents.text().equals("Ponder GUI Editor");
    }

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void lib99j$removeBackground(CallbackInfo ci) {
        if(isDialog && Minecraft.getInstance().hasShiftDown()) ci.cancel();
    }

    @Inject(method = "keyPressed", at = @At("TAIL"))
    private void lib99j$cursorPosAdd(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if(isDialog && keyEvent.isCopy()) {
            Vector2i convertedPos = convertPos(pos);
            Minecraft.getInstance().keyboardHandler.setClipboard("new Vector2i("+convertedPos.x+", "+convertedPos.y+")");
            Minecraft.getInstance().getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.literal("Copied pos"), null));
        };
        if(isDialog && keyEvent.key() == InputConstants.KEY_EQUALS) {
            Minecraft.getInstance().getWindow().setWindowed(854, 480);
        };
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void lib99j$cursorPos(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if(isDialog && Minecraft.getInstance().hasShiftDown()) {
            pos = new Vector2i(mouseX, mouseY);
            Vector2i convertedPos = convertPos(pos);
            this.tooltipHolder.set(Tooltip.create(Component.literal("Pos: "+convertedPos.x+", "+convertedPos.y+"\nOpposite pos: "+(PonderCoordUtil.SCREEN_SIZE.x-convertedPos.x)+", "+convertedPos.y)));

            //cancelling makes it not render the rest of the ui
            ci.cancel();
        }
        if(isDialog) {
            if(Minecraft.getInstance().getWindow().getWidth() != 854 || Minecraft.getInstance().getWindow().getHeight() != 480) this.tooltipHolder.set(Tooltip.create(Component.literal("You must have the window at the default size and be not in fullscreen to use the pos convertor. Press = to set size and F11 to toggle fullscreen")));
            this.tooltipHolder.refreshTooltipForNextRenderPass(guiGraphics, (int) pos.x, (int) pos.y+10,true, true, new ScreenRectangle(0, 0, 20, 20));
        }
    }

    @Unique
    private static Vector2i convertPos(Vector2i pos) {
        return new Vector2i((int) ((float) pos.x/Minecraft.getInstance().getWindow().getGuiScaledWidth()*PonderCoordUtil.SCREEN_SIZE.x), (int) ((float) pos.y/Minecraft.getInstance().getWindow().getGuiScaledHeight()*PonderCoordUtil.SCREEN_SIZE.y));
    }
}