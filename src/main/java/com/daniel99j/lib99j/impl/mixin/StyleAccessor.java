package com.daniel99j.lib99j.impl.mixin;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Style.class)
public interface StyleAccessor {
    @Accessor("color")
    @Nullable TextColor getColor();

    @Accessor("shadowColor")
    @Nullable Integer getShadowColor();

    @Accessor("bold")
    @Nullable Boolean getBold();

    @Accessor("italic")
    @Nullable Boolean getItalic();

    @Accessor("underlined")
    @Nullable Boolean getUnderlined();

    @Accessor("strikethrough")
    @Nullable Boolean getStrikethrough();

    @Accessor("obfuscated")
    @Nullable Boolean getObfuscated();
}