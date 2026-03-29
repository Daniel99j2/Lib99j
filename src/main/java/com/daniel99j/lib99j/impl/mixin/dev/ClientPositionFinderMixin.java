package com.daniel99j.lib99j.impl.mixin.dev;

import com.daniel99j.lib99j.ponder.api.PonderCoordUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
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

import java.util.List;

@Mixin(Screen.class)
public abstract class ClientPositionFinderMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;

    @Shadow
    @Final
    private List<Renderable> renderables;
    @Shadow
    public int width;
    @Shadow
    public int height;
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

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
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
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void lib99j$cursorPos(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if(isDialog && Minecraft.getInstance().hasShiftDown()) {
            pos = new Vector2i(mouseX, mouseY);
            Vector2i convertedPos = convertPos(pos);
            this.tooltipHolder.set(Tooltip.create(Component.literal("Pos: "+convertedPos.x+", "+convertedPos.y+"\nOpposite pos: "+(PonderCoordUtil.SCREEN_SIZE.x-convertedPos.x)+", "+convertedPos.y)));

            //cancelling makes it not render the rest of the ui
            ci.cancel();
        }
        if(isDialog) {
            this.tooltipHolder.refreshTooltipForNextRenderPass(graphics, (int) pos.x, (int) pos.y+10,true, true, new ScreenRectangle(0, 0, 20, 20));
        }
    }

    @Unique
    private static Vector2i convertPos(Vector2i pos) {
        return new Vector2i((int) ((float) pos.x/Minecraft.getInstance().getWindow().getGuiScaledWidth()*PonderCoordUtil.SCREEN_SIZE.x), (int) ((float) pos.y/Minecraft.getInstance().getWindow().getGuiScaledHeight()*PonderCoordUtil.SCREEN_SIZE.y));
    }
}