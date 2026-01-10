package com.daniel99j.lib99j.api.gui;

import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class BackgroundTexture {
    final char character = GuiUtils.getNextGuiChar();
    final int width;
    final Identifier path;

    public BackgroundTexture(Identifier path, int width) {
        this.width = width;
        this.path = path;
        ResourcePackExtras.forDefault().addBridgedModelsFolder(Identifier.of(path.getNamespace(), "gui"));
        GuiUtils.FONT_TEXTURES.add(new FontTexture(Identifier.of(path.getNamespace(), "gui/" + path.getPath()), 13, 256, new char[][]{new char[]{character}}));
    }

    public MutableText text() {
        MutableText text = GuiUtils.getSpace(-8, Text.literal(""));
        text.append(Text.literal(Character.toString(character)).formatted(Formatting.WHITE).fillStyle(Style.EMPTY.withFont(new StyleSpriteSource.Font(Identifier.of("lib99j", "gui")))));
        GuiUtils.getSpace(8, text);
        GuiUtils.getSpace(-width, text);
        return text;
    }
}
