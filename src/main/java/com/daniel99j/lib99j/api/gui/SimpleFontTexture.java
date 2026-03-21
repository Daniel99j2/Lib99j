package com.daniel99j.lib99j.api.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public class SimpleFontTexture {
    final char character = GuiUtils.getNextGuiChar();
    final int width;
    final Identifier path;
    final int offset;
    final String text;

    public SimpleFontTexture background(Identifier path, int width) {
        return new SimpleFontTexture(path, width, 13, 256, 8);
    }

    public SimpleFontTexture(Identifier path, int widthCorrection, int ascent, int height, int offset) {
        this.width = widthCorrection;
        this.path = path;
        this.offset = offset;
        GuiUtils.FONT_TEXTURES.add(new FontTexture(Identifier.fromNamespaceAndPath(path.getNamespace(), "ui/" + path.getPath()), ascent, height, new char[][]{new char[]{character}}));

        MutableComponent text1 = GuiUtils.appendSpace(-offset, Component.empty());
        text1.append(Component.literal(Character.toString(this.character)).withStyle(ChatFormatting.WHITE));
        GuiUtils.appendSpace(offset, text1);
        GuiUtils.appendSpace(-width, text1);
        text = GuiUtils.compactText(text1).getString();
    }

    public String string() {
        return text;
    }

    public MutableComponent text() {
        return Component.literal(text).withStyle(GuiUtils.GUI_FONT_STYLE);
    }
}
