package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.ponder.api.PonderManager;
import eu.pb4.polydex.api.v1.recipe.PolydexEntry;
import eu.pb4.polydex.api.v1.recipe.PolydexPage;
import eu.pb4.polydex.impl.book.ui.ExtendedGui;
import eu.pb4.polydex.impl.book.ui.PageViewerGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(PageViewerGui.class)
public abstract class PolydexPageMixin extends ExtendedGui {
    @Shadow
    @Final
    @Nullable
    protected PolydexEntry entry;

    @Shadow
    @Final
    protected List<PolydexPage> pages;

    @Shadow
    public abstract int getPage();

    public PolydexPageMixin(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots) {
        super(type, player, manipulatePlayerSlots);
    }

    @Inject(method = "setupNavigator", at = @At("TAIL"))
    private void lib99j$addPonderButton(CallbackInfo ci) {
        if(this.entry != null) {
            Item item = this.pages.get(this.getPage()).entryIcon(this.entry, this.getPlayer()).getItem();
            if(PonderManager.itemToBuilders.containsKey(item)) this.setSlot(52, new GuiElementBuilder(Items.LIGHT.getDefaultInstance()).noDefaults().setComponent(DataComponents.BLOCK_STATE, new BlockItemStateProperties(Map.of("level", "15"))).hideDefaultTooltip().setItemName(Component.translatable("ponder.scene.ponder_about")).setCallback(() -> {
                this.close();
                PonderManager.idToBuilder.get(PonderManager.itemToBuilders.get(item).getFirst()).startPondering(this.getPlayer());
            }));
        }
    }
}