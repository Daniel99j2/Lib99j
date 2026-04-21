package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.config.ConfigContext;
import com.daniel99j.lib99j.api.config.ConfigManager;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class DebugArgForScreen extends DebugOptionsScreen.AbstractOptionEntry {
    private static final int BUTTON_WIDTH = 60;
    private final String name;
    protected final List<AbstractWidget> children;
    private final CycleButton<Boolean> on;
    private final CycleButton<Boolean> off;

    public DebugArgForScreen(String name) {
        super();
        this.children = Lists.<AbstractWidget>newArrayList();
        this.name = name;

        this.on = CycleButton.booleanBuilder(
                        Component.translatable("debug.entry.on").withColor(-1), Component.translatable("debug.entry.on").withColor(-4539718), false
                )
                .displayOnlyValue()
                .create(10, 5, 60, 16, Component.literal(name), (button, newValue) -> this.setValue(name, true));

        this.off = CycleButton.booleanBuilder(
                        Component.translatable("debug.entry.off").withColor(-2142128), Component.translatable("debug.entry.off").withColor(-4539718), false
                )
                .displayOnlyValue()
                .create(10, 5, 60, 16, Component.literal(name), (button, newValue) -> this.setValue(name, false));
        this.children.add(this.on);
        this.children.add(this.off);

        this.refreshEntry();
    }

    private void setValue(String name, boolean value) {
        Lib99j.debugArgForScreens.get(name).setter().accept(value);

//        Minecraft.getInstance().debugEntries.setStatus(location, never);
//
//        for (Button profileButton : DebugOptionsScreen.profileButtons) {
//            profileButton.active = true;
//        }

        this.refreshEntry();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return this.children;
    }

    @Override
    public void extractContent(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final boolean hovered, final float a) {
        int x = this.getContentX();
        int y = this.getContentY();
        graphics.text(Minecraft.getInstance().font, this.name, x, y + 5, -1);
        int buttonsStartX = x + this.getContentWidth() - this.on.getWidth() - this.off.getWidth();

        this.on.setX(buttonsStartX);
        this.off.setX(this.on.getX() + this.on.getWidth());
        this.on.setY(y);
        this.off.setY(y);
        this.on.extractRenderState(graphics, mouseX, mouseY, a);
        this.off.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public void refreshEntry() {
        boolean value = ((Lib99jCommonConfig) ConfigManager.getConfig("lib99j").get(ConfigContext.COMMON).getOrThrow()).enabledDebugArgs.contains(name);
        this.on.setValue(value);
        this.off.setValue(!value);
        this.on.active = !this.on.getValue();
        this.off.active = !this.off.getValue();
    }
}