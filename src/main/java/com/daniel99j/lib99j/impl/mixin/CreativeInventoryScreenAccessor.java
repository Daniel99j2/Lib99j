package com.daniel99j.lib99j.impl.mixin;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;

@Mixin({CreativeModeInventoryScreen.class})
public interface CreativeInventoryScreenAccessor {
    @Invoker
    void callRefreshCurrentTabContents(Collection<ItemStack> displayList);
}