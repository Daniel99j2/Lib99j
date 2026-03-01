package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.EntityUtils;
import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.daniel99j.lib99j.ponder.api.PonderBuilder;
import com.daniel99j.lib99j.ponder.api.PonderManager;
import com.daniel99j.lib99j.ponder.api.PonderScene;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PonderCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("ponder");

        LiteralArgumentBuilder<CommandSourceStack> all = Commands.literal("all");

        registerIdSuggestions(PonderManager.idToBuilder.keySet(), all);

        root.then(all);

        LiteralArgumentBuilder<CommandSourceStack> itemBuilder = Commands.literal("item");

        itemBuilder.then(Commands.literal("hand")
                .executes(context -> {
                    if(checkIfOnGround(context)) return 0;
                    ItemStack stack = context.getSource().getPlayerOrException().getMainHandItem();
                    if (stack.isEmpty()) {
                        context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.not_holding_item"));
                        return 0;
                    }
                    Item currentItem = stack.getItem();
                    if (PonderManager.itemToBuilders.containsKey(currentItem) && !PonderManager.itemToBuilders.get(currentItem).isEmpty()) {
                        return openPonderFromId(context, PonderManager.itemToBuilders.get(currentItem).getFirst());
                    } else {
                        context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.item_not_found", BuiltInRegistries.ITEM.getKey(currentItem).toString()));
                        return 0;
                    }
                })
                .then(Commands.argument("id", IdentifierArgument.id())
                        .suggests((context, builder) -> {
                            ItemStack stack = context.getSource().getPlayerOrException().getMainHandItem();
                            if (stack.isEmpty()) return builder.buildFuture();
                            Item currentItem = stack.getItem();
                            List<Identifier> ids = PonderManager.itemToBuilders.getOrDefault(currentItem, List.of());
                            return SharedSuggestionProvider.suggest(ids.stream().map(Identifier::toString).toList(), builder);
                        })
                        .executes(context -> {
                            if(checkIfOnGround(context)) return 0;
                            ItemStack stack = context.getSource().getPlayerOrException().getMainHandItem();
                            if (stack.isEmpty()) {
                                context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.not_holding_item"));
                                return 0;
                            }
                            Item currentItem = stack.getItem();
                            Identifier id = IdentifierArgument.getId(context, "id");
                            PonderBuilder builder = PonderManager.idToBuilder.get(id);
                            if (builder == null) {
                                context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.item_id_not_found", BuiltInRegistries.ITEM.getKey(currentItem).toString(), id.toString()));
                                return 0;
                            }
                            context.getSource().sendSystemMessage(Component.translatable("commands.lib99j.ponder.pondering_from_id", id.toString()));
                            builder.startPondering(context.getSource().getPlayerOrException());
                            return 1;
                        })
                )
        );

        itemBuilder.then(Commands.argument("item", ItemArgument.item(registryAccess))
                .suggests((context, builder) -> {
                    PonderManager.itemToBuilders.keySet().forEach(item -> builder.suggest(BuiltInRegistries.ITEM.getKey(item).toString()));
                    return builder.buildFuture();
                })
                .executes(context -> {
                    if(checkIfOnGround(context)) return 0;
                    Item currentItem = ItemArgument.getItem(context, "item").getItem();
                    if (PonderManager.itemToBuilders.containsKey(currentItem) && !PonderManager.itemToBuilders.get(currentItem).isEmpty()) {
                        return openPonderFromId(context, PonderManager.itemToBuilders.get(currentItem).getFirst());
                    } else {
                        context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.item_not_found", BuiltInRegistries.ITEM.getKey(currentItem).toString()));
                        return 0;
                    }
                })
                .then(Commands.literal("scene").then(Commands.argument("id", IdentifierArgument.id()).suggests(((context, builder) -> {
                    List<String> allowedIds = new ArrayList<>();
                    Item currentItem = ItemArgument.getItem(context, "item").getItem();
                    if (PonderManager.itemToBuilders.containsKey(currentItem)) {
                        PonderManager.itemToBuilders.get(currentItem).forEach((id) -> allowedIds.add(id.toString()));
                    }
                    return SharedSuggestionProvider.suggest(allowedIds, builder);
                })).executes((context -> {
                    if(checkIfOnGround(context)) return 0;
                    Identifier id = IdentifierArgument.getId(context, "id");
                    Item currentItem = ItemArgument.getItem(context, "item").getItem();

                    PonderBuilder builder = PonderManager.idToBuilder.get(id);
                    if (builder == null) {
                        context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.item_id_not_found", BuiltInRegistries.ITEM.getKey(currentItem).toString(), id.toString()));
                        return 0;
                    }
                    //send before so it isn't blocked by packet redirection
                    context.getSource().sendSystemMessage(Component.translatable("commands.lib99j.ponder.pondering_from_id", id.toString()));
                    builder.startPondering(context.getSource().getPlayerOrException());
                    return 1;
                }))))
        );
        root.then(itemBuilder);

        LiteralArgumentBuilder<CommandSourceStack> scene = Commands.literal("scene");

        scene.then(Commands.literal("pause").executes((context) -> {
            if (PonderManager.isPondering(context.getSource().getPlayerOrException())) {
                PonderScene currentScene = PonderManager.activeScenes.get(context.getSource().getPlayerOrException());
                currentScene.pause();
                context.getSource().sendSystemMessage(Component.translatable("commands.lib99j.ponder.scene.paused"));
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.scene.no_scene"));
            return 0;
        }));

        scene.then(Commands.literal("unpause").executes((context) -> {
            if (PonderManager.isPondering(context.getSource().getPlayerOrException())) {
                PonderScene currentScene = PonderManager.activeScenes.get(context.getSource().getPlayerOrException());
                currentScene.unpause();
                context.getSource().sendSystemMessage(Component.translatable("commands.lib99j.ponder.scene.unpaused"));
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.scene.no_scene"));
            return 0;
        }));

        scene.then(Commands.literal("exit").executes((context) -> {
            if (PonderManager.isPondering(context.getSource().getPlayerOrException())) {
                PonderScene currentScene = PonderManager.activeScenes.get(context.getSource().getPlayerOrException());
                currentScene.stopPonderingSafely();
                context.getSource().sendSystemMessage(Component.translatable("commands.lib99j.ponder.scene.exited"));
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.scene.no_scene"));
            return 0;
        }));

        root.then(scene);

        LiteralArgumentBuilder<CommandSourceStack> dev = Commands.literal("dev");
        dev.then(Commands.literal("ui_creator").executes((context) -> {
            return 1;
        }));

        dev.then(Commands.literal("get_edits").executes((context) -> {
            if (PonderManager.isPondering(context.getSource().getPlayerOrException())) {
                PonderScene currentScene = PonderManager.activeScenes.get(context.getSource().getPlayerOrException());
                if (currentScene.ponderDevEdits.blockEdits.isEmpty()) {
                    context.getSource().sendSystemMessage(Component.literal("You have no block edits. Use /ponder dev toggle_free_movement and place a block down to add to the list"));
                    return 1;
                }
                context.getSource().sendSystemMessage(Component.literal("Current edits:").withColor(ChatFormatting.GRAY.getColor()));
                currentScene.ponderDevEdits.blockEdits.forEach((blockEdit -> {
                    String type = blockEdit.type() == PonderDevEdits.EditType.ADD ? "+ " : blockEdit.type() == PonderDevEdits.EditType.REMOVE ? "- " : "= ";
                    int colour = blockEdit.type() == PonderDevEdits.EditType.ADD ? ChatFormatting.GREEN.getColor() : blockEdit.type() == PonderDevEdits.EditType.REMOVE ? ChatFormatting.RED.getColor() : ChatFormatting.AQUA.getColor();

                    context.getSource().sendSystemMessage(Component.literal(type).withStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent.CopyToClipboard("scene.setBlockAndUpdate(new BlockPos(" + blockEdit.pos().getX() + ", " + blockEdit.pos().getY() + ", " + blockEdit.pos().getZ() + "), ModBlocks." + BuiltInRegistries.BLOCK.getKey(blockEdit.block()).getPath().toUpperCase() + ".defaultBlockState())")
                    ).withColor(colour).withHoverEvent(new HoverEvent.ShowText(Component.literal("[Click to copy]")))).append(blockEdit.block().getName()).append(" at ").append(blockEdit.pos().toShortString()));
                }));
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.scene.no_scene"));
            return 0;
        }));

        dev.then(Commands.literal("toggle_free_movement").executes((context) -> {
            if (PonderManager.isPondering(context.getSource().getPlayerOrException())) {
                PonderScene currentScene = PonderManager.activeScenes.get(context.getSource().getPlayerOrException());
                currentScene.ponderDevEdits.inBlocKEditMode = !currentScene.ponderDevEdits.inBlocKEditMode;
                if (currentScene.ponderDevEdits.inBlocKEditMode) {
                    currentScene.pause();
                    ((Lib99jPlayerUtilController) context.getSource().getPlayer()).lib99j$unlockCamera();
                    context.getSource().getPlayer().connection.send(new Lib99j.BypassPacket(new ClientboundPlayerPositionPacket(-100, new PositionMoveRotation(currentScene.getOrigin().above(1).getBottomCenter(), Vec3.ZERO, 0, 0), Set.of())));
                } else {
                    currentScene.unpause();
                    VFXUtils.removeGenericScreenEffect(context.getSource().getPlayerOrException(), Identifier.fromNamespaceAndPath("ponder", "ponder_lock"));
                    VFXUtils.addGenericScreenEffect(context.getSource().getPlayerOrException(), -1, VFXUtils.GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS, Identifier.fromNamespaceAndPath("ponder", "ponder_lock"));
                }
                context.getSource().sendSystemMessage(Component.literal("Toggled free movement"));
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.scene.no_scene"));
            return 0;
        }));

        root.then(dev);

        dispatcher.getRoot().addChild(root.build());
    }

    private static boolean checkIfOnGround(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        //enable elytra pondering. it might be buggy but its less cheaty than being able to have a friend place water below when you fall
        //if this didnt exist, the player would still take fall damage but only when exiting ponder
        if (!context.getSource().getPlayerOrException().onGround() && !context.getSource().getPlayerOrException().isFallFlying() && !context.getSource().getPlayerOrException().getAbilities().flying && EntityUtils.getDistanceToGround(context.getSource().getPlayerOrException(), 3) > 10) {
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.not_on_ground"));
            return true;
        }
        return false;
    }


    private static void registerIdSuggestions(Set<Identifier> ids, LiteralArgumentBuilder<CommandSourceStack> root) {
        List<String> allowedIds = new ArrayList<>();
        ids.forEach((id) -> allowedIds.add(id.toString()));
        root.then(Commands.argument("id", IdentifierArgument.id()).suggests((context, builder) -> SharedSuggestionProvider.suggest(allowedIds, builder)).executes((context) -> openPonderFromId(context, IdentifierArgument.getId(context, "id"))));
    }

    private static int openPonderFromId(CommandContext<CommandSourceStack> context, Identifier id) throws CommandSyntaxException {
        PonderBuilder builder = PonderManager.idToBuilder.get(id);
        if (builder == null) {
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.id_not_found", id.toString()));
            return 0;
        }
        //send before so it isn't blocked by packet redirection
        context.getSource().sendSystemMessage(Component.translatable("commands.lib99j.ponder.pondering_from_id", id.toString()));
        builder.startPondering(context.getSource().getPlayerOrException());
        return 1;
    }
}
