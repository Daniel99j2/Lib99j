package com.daniel99j.lib99j.api;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import xyz.nucleoid.packettweaker.PacketContext;

public class AutoPolymerItem extends Item implements PolymerItem {
    public AutoPolymerItem(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return ItemUtils.getBasicModelItem();
    }
}