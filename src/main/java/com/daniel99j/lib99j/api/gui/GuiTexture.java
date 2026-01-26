package com.daniel99j.lib99j.api.gui;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

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
        GuiUtils.FONT_TEXTURES.add(new FontTexture(Identifier.of(path.getNamespace(), "gui/" + path.getPath()), ascent, height, new char[][]{new char[]{character}}));
    }

    public MutableText text() {
        MutableText text = Text.literal(Character.toString(character)).formatted(Formatting.WHITE).fillStyle(Style.EMPTY.withFont(new StyleSpriteSource.Font(Identifier.of("lib99j", "gui"))));
        GuiUtils.appendSpace(-width, text);
        return text;
    }
}
