package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.Lib99jClient;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Unique
    private static boolean isEnabled = false;
    @Unique
    private static boolean hasChecked = false;

    @Inject(
            method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V",
            at = @At("RETURN")
    )    public void lib99j$storeItemInTab(Holder<?> item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        if(!hasChecked) {
            isEnabled = Lib99j.CONFIG.getClient().extraItemGroups;
            hasChecked = true;
        }
        if(isEnabled && Lib99j.CONFIG.getClient().storeItems && PolymerCommonUtils.isClientNetworkingThread()) {
            Lib99jClient.addItem(((ItemStack) (Object) this));
        }
    }
}