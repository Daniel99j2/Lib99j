package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99jClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.CustomCreativeSlot.class)
public class CreativeSlotMixin extends Slot {
    public CreativeSlotMixin(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Inject(method = "mayPickup", at = @At("RETURN"), cancellable = true)
    public void lib99j$reloadStorage(Player player, CallbackInfoReturnable<Boolean> cir) {
        if(this.getItem().has(DataComponents.CUSTOM_DATA) && this.getItem().get(DataComponents.CUSTOM_DATA).copyTag().getBooleanOr("lib99j$reloadStorage", false)) {
            cir.setReturnValue(false);
            ((CreativeModeInventoryScreen) Minecraft.getInstance().screen).getSelectedTab().getDisplayItems().clear();
            ItemStack reload = Items.COMPASS.getDefaultInstance();
            reload.set(DataComponents.CREATIVE_SLOT_LOCK, Unit.INSTANCE);
            reload.set(DataComponents.ITEM_NAME, Component.literal("Reload list"));
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("lib99j$reloadStorage", true);
            reload.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            ((CreativeModeInventoryScreen) Minecraft.getInstance().screen).getSelectedTab().getDisplayItems().add(reload);

            ((CreativeModeInventoryScreen) Minecraft.getInstance().screen).getSelectedTab().getDisplayItems().addAll(Lib99jClient.storedItems);
        }
    }
}