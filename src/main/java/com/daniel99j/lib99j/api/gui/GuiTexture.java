package com.daniel99j.lib99j.api.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class GuiTexture {
    public final Identifier path;
    public final int ascent;
    public final int height;
    public final int width;
    final char character;

    public GuiTexture(Identifier path, int ascent, int height, int width) {
        this.ascent = ascent;
        this.path = path;
        this.height = height;
        this.width = width;
        this.character = GuiUtils.getNextGuiChar();
        GuiUtils.FONT_TEXTURES.add(new FontTexture(Identifier.fromNamespaceAndPath(path.getNamespace(), "gui/" + path.getPath()), ascent, height, new char[][]{new char[]{character}}));
    }

    public MutableComponent text() {
        MutableComponent text = Component.literal(Character.toString(character)).withStyle(ChatFormatting.WHITE).withStyle(Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("lib99j", "gui"))));
        GuiUtils.appendSpace(-width, text);
        return text;
    }
}
