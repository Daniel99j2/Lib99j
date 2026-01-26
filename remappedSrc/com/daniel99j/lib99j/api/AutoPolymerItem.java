package com.daniel99j.lib99j.api;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import xyz.nucleoid.packettweaker.PacketContext;

@SuppressWarnings({"unused"})
public class AutoPolymerItem extends Item implements PolymerItem {
    public AutoPolymerItem(net.minecraft.item.Item.Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return ItemUtils.getBasicModelItem();
    }
}