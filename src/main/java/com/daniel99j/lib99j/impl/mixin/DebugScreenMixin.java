package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.impl.DebugArgForScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(DebugOptionsScreen.OptionList.class)
public abstract class DebugScreenMixin extends ContainerObjectSelectionList<DebugOptionsScreen.AbstractOptionEntry> {
    public DebugScreenMixin(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    @Inject(method = "updateSearch", at = @At(value = "TAIL", shift = At.Shift.BY, by = -1))
    public void lib99j$addDebugArgs(String search, CallbackInfo ci) {
        ArrayList<DebugArgForScreen> args = new ArrayList<>();
        Lib99j.debugArgForScreens.forEach((string, _) -> {
            if(string.contains(search)) args.add(new DebugArgForScreen(string));
        });

        if(!args.isEmpty()) this.addEntry(new DebugOptionsScreen.AbstractOptionEntry() {
            @Override
            public List<? extends GuiEventListener> children() {
                return List.of();
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
                graphics.centeredText(Minecraft.getInstance().font, Component.translatable("lib99j.debug.debug_args"), this.getContentX() + this.getContentWidth() / 2, this.getContentY() + 5, -1);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of();
            }

            @Override
            public void refreshEntry() {

            }
        });

        for (DebugArgForScreen arg : args) {
            this.addEntry(arg);
        }
    }
}