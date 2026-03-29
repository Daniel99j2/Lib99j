package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.gui.DefaultGuiTextures;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.ponder.api.PonderBuilder;
import com.daniel99j.lib99j.ponder.api.PonderGroup;
import com.daniel99j.lib99j.ponder.api.PonderManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.ItemBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PonderMenu {
    public static void buildBaseMenu(ServerPlayer player, Identifier current) {
        if(!PonderManager.isPondering(player) || !PonderManager.activeScenes.get(player).builder.getId().equals(current)) {
            if(PonderManager.isPondering(player)) {
                PonderManager.activeScenes.get(player).stopPonderingSafelyWithoutVfx();
                PonderManager.activeScenes.get(player).runOnceDone = () -> {
                    PonderManager.idToBuilder.get(current).startPondering(player);
                    PonderManager.activeScenes.get(player).openMenu();
                };
            } else {
                PonderManager.idToBuilder.get(current).startPondering(player);
                PonderManager.activeScenes.get(player).openMenu();
            }
            return;
        }
        PonderBuilder builder = PonderManager.idToBuilder.get(current);

        MutableComponent title = builder.title.copy();
        if(title.getStyle().getHoverEvent() == null) GuiUtils.styleText(title, title.getStyle().withHoverEvent(new HoverEvent.ShowText(builder.description)), false);

        List<DialogBody> body = new ArrayList<>();
        body.add(new ItemBody(ItemStackTemplate.fromNonEmptyStack(PonderGuiTextures.PONDERING_ABOUT_ITEM.create()), Optional.of(new PlainMessage(Component.translatable("ponder.scene.currently_pondering_about").withColor(ChatFormatting.GRAY.getColor()), 200)), false, false, 16, 16));
        body.add(renderBuilder(builder, false));
        body.add(spacer());
        if(!builder.getGroups().isEmpty()) body.add(new ItemBody(ItemStackTemplate.fromNonEmptyStack(DefaultGuiTextures.INVISIBLE.create()), Optional.of(new PlainMessage(Component.translatable("ponder.scene.associated_groups").withColor(ChatFormatting.GRAY.getColor()), 200)), false, false, 16, 16));

        for (Identifier group : builder.getGroups()) {
            body.add(renderGroup(PonderManager.idToGroup.get(group)));
        }

        player.openDialog(Holder.direct(new NoticeDialog(
                new CommonDialogData(Component.translatable("ponder.scene.menu_title"), Optional.empty(), true, false, DialogAction.WAIT_FOR_RESPONSE, body, List.of()), new ActionButton(new CommonButtonData(Component.translatable("gui.done"), 150), Optional.of(new StaticAction(new ClickEvent.Custom(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "close_ponder_menu"), Optional.empty()))))
        )));
    }

    public static void buildGroupMenu(ServerPlayer player, Identifier id) {
        PonderGroup group = PonderManager.idToGroup.get(id);

        Item item = Lib99j.getServerOrThrow().overworld().registryAccess().getOrThrow(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(group.id.getNamespace().replace("_item_", ""), group.id.getPath()))).value();
        MutableComponent title = group.id.getNamespace().contains("_item_") ? MutableComponent.create(item.getName(item.getDefaultInstance()).getContents()) : Component.translatable("ponder.scene.group."+group.id.getNamespace()+"."+group.id.getPath());

        List<DialogBody> body = new ArrayList<>();
        body.add(new ItemBody(ItemStackTemplate.fromNonEmptyStack(PonderGuiTextures.PONDERING_ABOUT_ITEM.create()), Optional.of(new PlainMessage(Component.translatable("ponder.scene.currently_pondering_about").withColor(ChatFormatting.GRAY.getColor()), 200)), false, false, 16, 16));
        body.add(new ItemBody(group.icon, Optional.of(new PlainMessage(title, 200)), false, false, 16, 16));
        body.add(spacer());
        if(!group.builders.isEmpty()) body.add(new ItemBody(ItemStackTemplate.fromNonEmptyStack(DefaultGuiTextures.INVISIBLE.create()), Optional.of(new PlainMessage(Component.translatable("ponder.scene.associated_scenes").withColor(ChatFormatting.GRAY.getColor()), 200)), false, false, 16, 16));
        for (PonderBuilder builder : group.builders) {
            body.add(renderBuilder(builder, true));
        }

        body.add(spacer());
        if(!group.getRelatedGroups().isEmpty()) body.add(new ItemBody(ItemStackTemplate.fromNonEmptyStack(DefaultGuiTextures.INVISIBLE.create()), Optional.of(new PlainMessage(Component.translatable("ponder.scene.associated_groups").withColor(ChatFormatting.GRAY.getColor()), 200)), false, false, 16, 16));
        for (PonderGroup group1 : group.getRelatedGroups()) {
            body.add(renderGroup(group1));
        }

        player.openDialog(Holder.direct(new NoticeDialog(
                new CommonDialogData(Component.translatable("ponder.scene.menu_title"), Optional.empty(), true, false, DialogAction.WAIT_FOR_RESPONSE, body, List.of()), new ActionButton(new CommonButtonData(Component.translatable("gui.done"), 150), Optional.of(new StaticAction(new ClickEvent.Custom(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "close_ponder_menu"), Optional.empty()))))
        )));
    }

    private static ItemBody renderGroup(PonderGroup group) {
        Item item = Lib99j.getServerOrThrow().overworld().registryAccess().getOrThrow(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(group.id.getNamespace().replace("_item_", ""), group.id.getPath()))).value();
        MutableComponent title = group.id.getNamespace().contains("_item_") ? MutableComponent.create(item.getName(item.getDefaultInstance()).getContents()) : Component.translatable("ponder.scene.group."+group.id.getNamespace()+"."+group.id.getPath());

        CompoundTag tag = new CompoundTag();
        tag.putString("id", group.id.toString());
        if(title.getStyle().getClickEvent() == null) GuiUtils.styleText(title, title.getStyle().withClickEvent(new ClickEvent.Custom(Identifier.fromNamespaceAndPath("lib99j", "show_ponder_group"), Optional.of(tag))), false);

        return new ItemBody(group.icon, Optional.of(new PlainMessage(title, 200)), false, false, 16, 16);
    }

    private static ItemBody renderBuilder(PonderBuilder builder, boolean clickable) {
        MutableComponent title = builder.title.copy();
        if(title.getStyle().getHoverEvent() == null) GuiUtils.styleText(title, title.getStyle().withHoverEvent(new HoverEvent.ShowText(builder.description)), false);

        CompoundTag tag = new CompoundTag();
        tag.putString("id", builder.getId().toString());
        if(clickable && title.getStyle().getClickEvent() == null) GuiUtils.styleText(title, title.getStyle().withClickEvent(new ClickEvent.Custom(Identifier.fromNamespaceAndPath("lib99j", "ponder_about_menu"), Optional.of(tag))), false);

        return new ItemBody(builder.icon, Optional.of(new PlainMessage(title, 200)), false, false, 16, 16);
    }

    private static ItemBody spacer() {
        return new ItemBody(ItemStackTemplate.fromNonEmptyStack(DefaultGuiTextures.INVISIBLE.create()), Optional.of(new PlainMessage(Component.empty(), 200)), false, false, 16, 16);
    }
}
