package com.daniel99j.lib99j.api.gui;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.ponder.impl.GuiTextures;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UseCooldownComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Optional;

public class DefaultGuiTextures {
    public static final GuiElement INVISIBLE;
    public static final GuiElementBuilder SOLID_COLOUR = GuiUtils.blank().model(Identifier.of(Lib99j.MOD_ID, "/gui/solid_colour")).setItemName(Text.of("==NOT SET=="));

    //textures from polydex
    public static final GuiElementBuilder HEAD_PREVIOUS_PAGE = GuiUtils.head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzEwODI5OGZmMmIyNjk1MWQ2ODNlNWFkZTQ2YTQyZTkwYzJmN2M3ZGQ0MWJhYTkwOGJjNTg1MmY4YzMyZTU4MyJ9fX0");
    public static final GuiElementBuilder HEAD_PREVIOUS_PAGE_BLOCKED = GuiUtils.head("ewogICJ0aW1lc3RhbXAiIDogMTY0MDYxNjE5MjE0MiwKICAicHJvZmlsZUlkIiA6ICJmMjc0YzRkNjI1MDQ0ZTQxOGVmYmYwNmM3NWIyMDIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJIeXBpZ3NlbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81MDgyMGY3NmUzZTA0MWM3NWY3NmQwZjMwMTIzMmJkZjQ4MzIxYjUzNGZlNmE4NTljY2I4NzNkMjk4MWE5NjIzIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=");
    public static final GuiElementBuilder HEAD_NEXT_PAGE = GuiUtils.head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg2MTg1YjFkNTE5YWRlNTg1ZjE4NGMzNGYzZjNlMjBiYjY0MWRlYjg3OWU4MTM3OGU0ZWFmMjA5Mjg3In19fQ");
    public static final GuiElementBuilder HEAD_NEXT_PAGE_BLOCKED = GuiUtils.head("ewogICJ0aW1lc3RhbXAiIDogMTY0MDYxNjExMDQ4OCwKICAicHJvZmlsZUlkIiA6ICIxZjEyNTNhYTVkYTQ0ZjU5YWU1YWI1NmFhZjRlNTYxNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3RNaUt5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdlNTc3MjBhNDg3OGM4YmNhYjBlOWM5YzQ3ZDllNTUxMjhjY2Q3N2JhMzQ0NWE1NGE5MWUzZTFlMWEyNzM1NmUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
    public static final GuiElementBuilder HEAD_ADD = GuiUtils.head("ewogICJ0aW1lc3RhbXAiIDogMTY0MjM2Mzc1NDQxMCwKICAicHJvZmlsZUlkIiA6ICJkODAwZDI4MDlmNTE0ZjkxODk4YTU4MWYzODE0Yzc5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVCTFJ4eCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80OTQ0YTI5ZjY4Yzg4NmNmYWY2N2UxNTI1YmQyYWNjMmEzZDRlNDBjMDE3NzVjNzIyMTQwZjA4YjY5ZDVkNjliIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=");


    public static final BackgroundTexture BIG_WHITE_SQUARE = new BackgroundTexture(Identifier.of(Lib99j.MOD_ID, "solid_colour"), 256, 1280, 2560, 0);


    static void load() {
    }

    static {
        ItemStack INVISIBLE_STACK = Items.BARRIER.getDefaultStack();
        INVISIBLE_STACK.set(DataComponentTypes.MAX_STACK_SIZE, 1);
        INVISIBLE_STACK.set(DataComponentTypes.ITEM_MODEL, Identifier.ofVanilla("air"));
        INVISIBLE_STACK.set(DataComponentTypes.USE_COOLDOWN, new UseCooldownComponent(0.001f, Optional.of(Identifier.of(Lib99j.MOD_ID, "invisible_model")))); // disable any item cooldown
        INVISIBLE = GuiElementBuilder.from(INVISIBLE_STACK).setItemName(Text.of("")).hideTooltip().build();
    }
}