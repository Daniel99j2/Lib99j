package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.Lib99j;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BarrelBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.storage.ReadView;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@SuppressWarnings({"unused"})
public class ItemUtils {
    public static ItemStack setModel(ItemStack stack, Identifier model) {
        return setModel(stack, model, true);
    }

    public static ItemStack setModelUnBridged(ItemStack stack, Identifier model) {
        return setModel(stack, model, false);
    }

    private static ItemStack setModel(ItemStack stack, Identifier model, boolean bridged) {
        Identifier identifier = Identifier.of(model.getNamespace(), bridged ? "-/" + model.getPath() : model.getPath());
        if (identifier.getPath().contains("blocks/")) throw new IllegalArgumentException("Use block/ not blocks/");
        if (identifier.getPath().contains("items/")) throw new IllegalArgumentException("Use item/ not items/");
        stack.set(DataComponentTypes.ITEM_MODEL, identifier);
        return stack;
    }

    public static void setColor(ItemStack stack, int color) {
        stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color));
    }

    public static int getRepairCost(ItemStack stack) {
        if (stack.get(DataComponentTypes.REPAIR_COST) != null) return stack.get(DataComponentTypes.REPAIR_COST);
        return 0;
    }

    public static void setCustomName(ItemStack stack, Text name) {
        stack.set(DataComponentTypes.CUSTOM_NAME, name);
    }

    public static Text getCustomName(ItemStack stack) {
        if (stack.get(DataComponentTypes.CUSTOM_NAME) != null) return stack.get(DataComponentTypes.CUSTOM_NAME);
        return null;
    }

    public static boolean hasCustomName(ItemStack stack) {
        return stack.get(DataComponentTypes.CUSTOM_NAME) != null;
    }

    public static Item getRandomItem() {
        return Registries.ITEM.stream().toList().get(NumberUtils.getRandomInt(1, Registries.ITEM.stream().toList().size()) - 1);
    }

    public static Item getBasicModelItem() {
        return Items.OMINOUS_TRIAL_KEY;
    }

    public static ItemStack getBasicModelItemStack() {
        return getBasicModelItem().getDefaultStack();
    }
}
