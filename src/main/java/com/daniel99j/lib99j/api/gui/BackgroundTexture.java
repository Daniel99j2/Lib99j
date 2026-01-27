package com.daniel99j.lib99j.api.gui;

import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class BackgroundTexture {
    final char character = GuiUtils.getNextGuiChar();
    final int width;
    final Identifier path;
    final int offset;

    public BackgroundTexture(Identifier path, int width) {
        this(path, width, 13, 256, 8);
    }

    public BackgroundTexture(Identifier path, int width, int ascent, int height, int offset) {
        this.width = width;
        this.path = path;
        this.offset = offset;
        ResourcePackExtras.forDefault().addBridgedModelsFolder(Identifier.fromNamespaceAndPath(path.getNamespace(), "gui"));
        GuiUtils.FONT_TEXTURES.add(new FontTexture(Identifier.fromNamespaceAndPath(path.getNamespace(), "gui/" + path.getPath()), ascent, height, new char[][]{new char[]{character}}));
    }

    public MutableComponent text() {
        MutableComponent text = GuiUtils.appendSpace(-offset, Component.empty());
        text.append(Component.literal(Character.toString(character)).withStyle(ChatFormatting.WHITE).withStyle(Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("lib99j", "gui")))));
        GuiUtils.appendSpace(offset, text);
        GuiUtils.appendSpace(-width, text);
        return text;
    }
}
