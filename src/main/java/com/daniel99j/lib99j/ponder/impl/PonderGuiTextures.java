package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.GameProperties;
import com.daniel99j.lib99j.api.gui.SimpleFontTexture;
import net.minecraft.resources.Identifier;

public class PonderGuiTextures {
    static {
        GameProperties.throwIfPonderNotEnabled("Ponder has not been enabled! Use GameProperties.enablePonder() to use ponder GUI textures");
    }

    public static final int FILLED_BAR_WIDTH = 176;

    public static final SimpleFontTexture STEP_BAR_BACKGROUND = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/step_bar_back"), 182*2, 0, 9*2, 0);
    public static final SimpleFontTexture STEP_BAR_1 = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/step_bar_"+ 1), 1, -3*2, 3*2, 0);

    public static final SimpleFontTexture PAUSE_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/pause"), 28*2, 28*2-6, 28*2, 360/2+28);
    public static final SimpleFontTexture RESUME_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/resume"), 28*2, 28*2-6, 28*2, 360/2+28);
    public static final SimpleFontTexture MENU_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/menu"), 28*2, 28*2-6, 28*2, 360);
    public static final SimpleFontTexture LEFT_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/left"), 28*2, 28*2-6, 28*2, 360/2+28+28*2);
    public static final SimpleFontTexture RIGHT_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/right"), 28*2, 28*2-6, 28*2, 360/2+28-28*2+2);
    public static final SimpleFontTexture EXIT_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/exit"), 28*2, 28*2-6, 28*2, 28*2);

    public static void load() {
    }
}