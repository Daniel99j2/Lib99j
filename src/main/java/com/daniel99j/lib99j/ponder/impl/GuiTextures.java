package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.resources.Identifier;

public class GuiTextures {
    public static final GuiElementBuilder TEST = GuiUtils.generateTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "test"));

    public static void init() {

    }
}
