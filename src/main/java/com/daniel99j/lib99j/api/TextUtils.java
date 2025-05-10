package com.daniel99j.lib99j.api;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;

public class TextUtils {
    public static final Text ENABLED = text("enabled").formatted(Formatting.GREEN);
    public static final Text DISABLED = text("disabled").formatted(Formatting.RED);

    public static MutableText gui(String path, Object... args) {
        return Text.translatable("gui.magmanetwork." + path, args);
    }


    public static MutableText text(String path, Object... args) {
        return Text.translatable("text.magmanetwork." + path, args);
    }

    public static MutableText command(String path, Object... args) {
        return Text.translatable("command.magmanetwork." + path, args);
    }

    public static MutableText direction(Direction from) {
        return Text.translatable("text.magmanetwork.dir." + from.getId());
    }
}