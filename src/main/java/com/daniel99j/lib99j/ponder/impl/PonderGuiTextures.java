package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.GameProperties;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.api.gui.SimpleFontTexture;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;

public class PonderGuiTextures {
    static {
        GameProperties.throwIfPonderNotEnabled("Ponder has not been enabled! Use GameProperties.enablePonder() to use ponder GUI textures");
    }

    public static final int FILLED_BAR_WIDTH = 176;

    public static final SimpleFontTexture STEP_BAR_BACKGROUND = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/step_bar_back"), 182*2, 0, 9*2, 0);
    public static final SimpleFontTexture STEP_BAR_FILLER = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/step_bar_1"), 1, -3*2, 3*2, 0);
    public static final SimpleFontTexture STEP_BAR_STEP = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/step_bar_step"), 1, -3*2, 3*2, 0);
    public static final SimpleFontTexture STEP_BAR_STEP_SELECTED = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/step_bar_step_selected"), 5, -3*2, 6*2, 0);

    public static final SimpleFontTexture PAUSE_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/pause"), 28*2, 28*2-6, 28*2, 360/2+28);
    public static final SimpleFontTexture RESUME_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/resume"), 28*2, 28*2-6, 28*2, 360/2+28);
    public static final SimpleFontTexture MENU_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/menu"), 28*2, 28*2-6, 28*2, 360);
    public static final SimpleFontTexture LEFT_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/left"), 28*2, 28*2-6, 28*2, 360/2+28+28*2);
    public static final SimpleFontTexture RIGHT_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/right"), 28*2, 28*2-6, 28*2, 360/2+28-28*2+2);
    public static final SimpleFontTexture EXIT_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/exit"), 28*2, 28*2-6, 28*2, 28*2);

    public static final SimpleFontTexture IDENTIFY_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/identify"), 28*2+1, 28*2, 28*2, 28*2);
    public static final SimpleFontTexture IDENTIFY_BUTTON_SELECTED = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/active"), 28*2+1, 28*2, 28*2, 28*2);
    public static final SimpleFontTexture STEP_SELECT_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/step_select"), 28*2+1, 0, 28*2, 28*2);
    public static final SimpleFontTexture STEP_GOTO_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/go_to_step"), 28*2+1, -28*2, 28*2, 28*2);
    public static final SimpleFontTexture RESTART_BUTTON = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/restart"), 28*2+1, -28*2*2, 28*2, 28*2);

    public static final ItemStackTemplate PONDERING_ABOUT_ITEM = GuiUtils.generateItemModel(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ui/ponder/pondering_about"), new ItemAsset.Properties(false, true));

    public static final SimpleFontTexture WHITE_LINE = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/text/white_line"), 1, 10, 16, 0);

    public static final SimpleFontTexture TEXT_BACKGROUND_TOP_LEFT_CORNER = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/text/top_left"), 1, 10, 16, 0);
    public static final SimpleFontTexture TEXT_BACKGROUND_TOP_RIGHT_CORNER = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/text/top_right"), 1, 10, 16, 0);
    public static final SimpleFontTexture TEXT_BACKGROUND_TOP = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/text/top"), 1, 10, 16, 0);
    public static final SimpleFontTexture TEXT_BACKGROUND_LEFT = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/text/left"), 1, 10, 16, 0);
    public static final SimpleFontTexture TEXT_BACKGROUND_RIGHT = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/text/right"), 1, 10, 16, 0);
    public static final SimpleFontTexture TEXT_BACKGROUND_BOTTOM_LEFT_CORNER = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/text/bottom_left"), 1, 10, 16, 0);
    public static final SimpleFontTexture TEXT_BACKGROUND_BOTTOM_RIGHT_CORNER = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/text/bottom_right"), 1, 10, 16, 0);
    public static final SimpleFontTexture TEXT_BACKGROUND_BOTTOM = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/text/bottom"), 1, 0, 16, 0);
    public static final SimpleFontTexture TEXT_BACKGROUND_SINGLE_LEFT = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/text/single_left"), 1, 10, 16, 0);
    public static final SimpleFontTexture TEXT_BACKGROUND_SINGLE_RIGHT = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/text/single_right"), 1, 10, 16, 0);
    public static final SimpleFontTexture TEXT_BACKGROUND_SINGLE = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ponder/text/single"), 1, 10, 16, 0);


    public static void load() {
    }
}