package com.daniel99j.lib99j.api;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;

@SuppressWarnings({"unused"})
public class ItemUtils {
    public static ItemStack setModel(ItemStack stack, Identifier model) {
        return setModel(stack, model, true);
    }

    public static ItemStack setModelUnBridged(ItemStack stack, Identifier model) {
        return setModel(stack, model, false);
    }

    private static ItemStack setModel(ItemStack stack, Identifier model, boolean bridged) {
        Identifier identifier = Identifier.fromNamespaceAndPath(model.getNamespace(), bridged ? "-/" + model.getPath() : model.getPath());
        if (identifier.getPath().contains("blocks/")) throw new IllegalArgumentException("Use block/ not blocks/");
        if (identifier.getPath().contains("items/")) throw new IllegalArgumentException("Use item/ not items/");
        stack.set(DataComponents.ITEM_MODEL, identifier);
        return stack;
    }

    public static void setColor(ItemStack stack, int color) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
    }

    public static int getRepairCost(ItemStack stack) {
        if (stack.get(DataComponents.REPAIR_COST) != null) return stack.get(DataComponents.REPAIR_COST);
        return 0;
    }

    public static void setCustomName(ItemStack stack, Component name) {
        stack.set(DataComponents.CUSTOM_NAME, name);
    }

    public static Component getCustomName(ItemStack stack) {
        if (stack.get(DataComponents.CUSTOM_NAME) != null) return stack.get(DataComponents.CUSTOM_NAME);
        return null;
    }

    public static boolean hasCustomName(ItemStack stack) {
        return stack.get(DataComponents.CUSTOM_NAME) != null;
    }

    public static Item getRandomItem() {
        return BuiltInRegistries.ITEM.stream().toList().get(NumberUtils.getRandomInt(1, BuiltInRegistries.ITEM.stream().toList().size()) - 1);
    }

    public static Item getBasicModelItem() {
        return Items.OMINOUS_TRIAL_KEY;
    }

    public static ItemStack getBasicModelItemStack() {
        return getBasicModelItem().getDefaultInstance();
    }
}
