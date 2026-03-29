package com.daniel99j.lib99j.api.gui;

import com.daniel99j.lib99j.Lib99j;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.UseCooldown;

import java.util.LinkedHashSet;
import java.util.Optional;

public class DefaultGuiTextures {
    public static final ItemStackTemplate INVISIBLE;
    public static final ItemStackTemplate SOLID_COLOUR = GuiUtils.generateColourableTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ui/solid_colour"));
    public static final ItemStackTemplate SOLID_COLOUR_BOX = GuiUtils.generateItemModel(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ui/solid_colour_box"), ItemAsset.Properties.DEFAULT);

    //textures from polydex
    public static final ItemStackTemplate HEAD_PREVIOUS_PAGE = GuiUtils.head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzEwODI5OGZmMmIyNjk1MWQ2ODNlNWFkZTQ2YTQyZTkwYzJmN2M3ZGQ0MWJhYTkwOGJjNTg1MmY4YzMyZTU4MyJ9fX0");
    public static final ItemStackTemplate HEAD_PREVIOUS_PAGE_BLOCKED = GuiUtils.head("ewogICJ0aW1lc3RhbXAiIDogMTY0MDYxNjE5MjE0MiwKICAicHJvZmlsZUlkIiA6ICJmMjc0YzRkNjI1MDQ0ZTQxOGVmYmYwNmM3NWIyMDIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJIeXBpZ3NlbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81MDgyMGY3NmUzZTA0MWM3NWY3NmQwZjMwMTIzMmJkZjQ4MzIxYjUzNGZlNmE4NTljY2I4NzNkMjk4MWE5NjIzIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=");
    public static final ItemStackTemplate HEAD_NEXT_PAGE = GuiUtils.head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg2MTg1YjFkNTE5YWRlNTg1ZjE4NGMzNGYzZjNlMjBiYjY0MWRlYjg3OWU4MTM3OGU0ZWFmMjA5Mjg3In19fQ");
    public static final ItemStackTemplate HEAD_NEXT_PAGE_BLOCKED = GuiUtils.head("ewogICJ0aW1lc3RhbXAiIDogMTY0MDYxNjExMDQ4OCwKICAicHJvZmlsZUlkIiA6ICIxZjEyNTNhYTVkYTQ0ZjU5YWU1YWI1NmFhZjRlNTYxNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3RNaUt5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdlNTc3MjBhNDg3OGM4YmNhYjBlOWM5YzQ3ZDllNTUxMjhjY2Q3N2JhMzQ0NWE1NGE5MWUzZTFlMWEyNzM1NmUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
    public static final ItemStackTemplate HEAD_ADD = GuiUtils.head("ewogICJ0aW1lc3RhbXAiIDogMTY0MjM2Mzc1NDQxMCwKICAicHJvZmlsZUlkIiA6ICJkODAwZDI4MDlmNTE0ZjkxODk4YTU4MWYzODE0Yzc5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVCTFJ4eCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80OTQ0YTI5ZjY4Yzg4NmNmYWY2N2UxNTI1YmQyYWNjMmEzZDRlNDBjMDE3NzVjNzIyMTQwZjA4YjY5ZDVkNjliIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=");

    public static final SimpleFontTexture BIG_WHITE_SQUARE = new SimpleFontTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "solid_colour"), 256, 1280, 2560, 0);


    static void load() {
    }

    static {
        DataComponentPatch patch = DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 1).set(DataComponents.ITEM_MODEL, Identifier.withDefaultNamespace("air")).set(DataComponents.USE_COOLDOWN, new UseCooldown(0.001f, Optional.of(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "invisible_model")))).set(DataComponents.ITEM_NAME, Component.literal("==NOT SET==")).set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, new LinkedHashSet<>())).build();
        INVISIBLE = new ItemStackTemplate(Items.BARRIER, patch);
    }
}