package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.CommandSourceAccessor;
import com.daniel99j.lib99j.api.CooldownManager;
import com.daniel99j.lib99j.api.EntityUtils;
import com.daniel99j.lib99j.api.Lib99jArgumentTypes;
import com.daniel99j.lib99j.ponder.api.PonderBuilder;
import com.daniel99j.lib99j.ponder.api.PonderManager;
import com.daniel99j.lib99j.ponder.api.PonderScene;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
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
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PonderCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        if(PonderManager.idToBuilder.isEmpty()) return;
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
                            context.getSource().sendSuccess(() -> Component.translatable("commands.lib99j.ponder.pondering_from_id", id.toString()), true);
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
                    context.getSource().sendSuccess(() -> Component.translatable("commands.lib99j.ponder.pondering_from_id", id.toString()), true);
                    builder.startPondering(context.getSource().getPlayerOrException());
                    return 1;
                }))))
        );
        root.then(itemBuilder);

        LiteralArgumentBuilder<CommandSourceStack> scene = Commands.literal("scene");

        scene.then(Commands.literal("pause").executes((context) -> {
            if (PonderManager.isPondering(context.getSource().getPlayerOrException())) {
                PonderScene currentScene = PonderManager.activeScenes.get(context.getSource().getPlayerOrException());
                currentScene.setMode(PonderSceneMode.PAUSED);
                context.getSource().sendSuccess(() -> Component.translatable("commands.lib99j.ponder.scene.paused"), true);
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.scene.no_scene"));
            return 0;
        }));

        scene.then(Commands.literal("unpause").executes((context) -> {
            if (PonderManager.isPondering(context.getSource().getPlayerOrException())) {
                PonderScene currentScene = PonderManager.activeScenes.get(context.getSource().getPlayerOrException());
                currentScene.setMode(PonderSceneMode.PLAYING);
                context.getSource().sendSuccess(() -> Component.translatable("commands.lib99j.ponder.scene.unpaused"), true);
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.scene.no_scene"));
            return 0;
        }));

        scene.then(Commands.literal("exit").executes((context) -> {
            if (PonderManager.isPondering(context.getSource().getPlayerOrException())) {
                PonderScene currentScene = PonderManager.activeScenes.get(context.getSource().getPlayerOrException());
                currentScene.stopPonderingSafely();
                context.getSource().sendSuccess(() -> Component.translatable("commands.lib99j.ponder.scene.exited"), true);
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.scene.no_scene"));
            return 0;
        }));

        root.then(scene);

        LiteralArgumentBuilder<CommandSourceStack> dev = Commands.literal("dev");
        dev.then(Commands.literal("ui_creator").executes((context) -> {
            if (PonderManager.isPondering(context.getSource().getPlayerOrException())) {
                PonderScene currentScene = PonderManager.activeScenes.get(context.getSource().getPlayerOrException());
                currentScene.stopPonderingSafelyWithoutVfx();
                currentScene.runOnceDone = () -> {
                    new PonderGuiCreator(currentScene.player, currentScene.builder, currentScene, currentScene.getStep());
                };
                context.getSource().sendSuccess(() -> Component.literal("Entered UI creator mode. Open the menu [L] to start editing"), true);
                context.getSource().sendSuccess(() -> Component.literal("If Lib99j is running on your client, hold [LEFT-SHIFT] to see the position you are hovering over whilst in the menu [L], then use [CTRL-C] to copy the position").withStyle(ChatFormatting.BLUE), true);
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.scene.no_scene"));
            return 0;
        }));

        dev.then(Commands.literal("ui_creator_from").then(root));

        dev.then(Commands.literal("get_edits").executes((context) -> {
            if (PonderManager.isPondering(context.getSource().getPlayerOrException())) {
                PonderScene currentScene = PonderManager.activeScenes.get(context.getSource().getPlayerOrException());
                if (currentScene.ponderDevEdits.blockEdits.isEmpty()) {
                    context.getSource().sendSuccess(() -> Component.literal("You have no block edits. Use /ponder dev toggle_free_movement and place a block down to add to the list"), true);
                    return 1;
                }
                context.getSource().sendSuccess(() -> Component.literal("Current edits:").withColor(ChatFormatting.GRAY.getColor()), true);
                currentScene.ponderDevEdits.blockEdits.forEach((blockEdit -> {
                    String type = blockEdit.type() == PonderDevEdits.EditType.ADD ? "+ " : blockEdit.type() == PonderDevEdits.EditType.REMOVE ? "- " : "= ";
                    int colour = blockEdit.type() == PonderDevEdits.EditType.ADD ? ChatFormatting.GREEN.getColor() : blockEdit.type() == PonderDevEdits.EditType.REMOVE ? ChatFormatting.RED.getColor() : ChatFormatting.AQUA.getColor();

                    context.getSource().sendSuccess(() -> Component.literal(type).withStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent.CopyToClipboard("scene.setBlockAndUpdate(new BlockPos(" + blockEdit.pos().getX() + ", " + blockEdit.pos().getY() + ", " + blockEdit.pos().getZ() + "), ModBlocks." + BuiltInRegistries.BLOCK.getKey(blockEdit.block()).getPath().toUpperCase() + ".defaultBlockState())")
                    ).withColor(colour).withHoverEvent(new HoverEvent.ShowText(Component.literal("[Click to copy]")))).append(blockEdit.block().getName()).append(" at ").append(blockEdit.pos().toShortString()), true);
                }));
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.scene.no_scene"));
            return 0;
        }));

        dev.then(Commands.literal("toggle_free_movement").executes((context) -> {
            if (PonderManager.isPondering(context.getSource().getPlayerOrException())) {
                PonderScene currentScene = PonderManager.activeScenes.get(context.getSource().getPlayerOrException());
                if (currentScene.getMode() != PonderSceneMode.DEV_EDITING) {
                    currentScene.setMode(PonderSceneMode.DEV_EDITING);
                } else {
                    currentScene.setMode(PonderSceneMode.PLAYING);
                }
                context.getSource().sendSuccess(() -> Component.literal("Toggled free movement"), true);
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.scene.no_scene"));
            return 0;
        }));

        if(Lib99j.isDevelopingLib99j) dev.then(Commands.literal("go_to_real_ponder_world").executes((context) -> {
            if (PonderManager.isPondering(context.getSource().getPlayerOrException())) {
                PonderScene currentScene = PonderManager.activeScenes.get(context.getSource().getPlayerOrException());
                PonderLevel level = currentScene.getLevel();
                currentScene.stopPondering(true);
                context.getSource().getPlayerOrException().teleportTo(level, currentScene.getOrigin().getX(), currentScene.getOrigin().getY(), currentScene.getOrigin().getZ(), Set.of(), 1, 1, true);
                context.getSource().sendSuccess(() -> Component.literal("Teleported to real ponder world"), true);
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.scene.no_scene"));
            return 0;
        }));

        if(Lib99j.isDevelopmentEnvironment) root.then(dev);

        dispatcher.getRoot().addChild(root.build());
    }

    private static boolean checkIfOnGround(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        //it feels 'wrong' but its less cheaty than being able to have a friend place water below when you fall
        //if this didnt exist, the player would still take fall damage but only when exiting ponder
        if (context.getSource().getPlayerOrException().getVehicle() == null && !context.getSource().getPlayerOrException().isInWater() && context.getSource().getPlayerOrException().gameMode.getGameModeForPlayer() != GameType.CREATIVE && !context.getSource().getPlayerOrException().onGround() && !context.getSource().getPlayerOrException().isFallFlying() && !context.getSource().getPlayerOrException().getAbilities().flying && (context.getSource().getPlayerOrException().fallDistance > 3 || EntityUtils.getDistanceToGround(context.getSource().getPlayerOrException(), 10) > 3)) {
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.not_on_ground"));
            return true;
        }
        return false;
    }


    private static void registerIdSuggestions(Set<Identifier> ids, LiteralArgumentBuilder<CommandSourceStack> root) {
        List<Identifier> allowedIds = new ArrayList<>();
        ids.forEach((id) -> {
            if(!PonderManager.idToBuilder.get(id).shouldHideFromCommands()) allowedIds.add(id);
        });
        //root.then(Commands.argument("id", Lib99jArgumentTypes.of(List.of(Identifier.withDefaultNamespace("vanilla"), Identifier.withDefaultNamespace("hello"), Identifier.fromNamespaceAndPath("poo", "pee"), Identifier.fromNamespaceAndPath("poo", "test")))).executes((context) -> openPonderFromId(context, IdentifierArgument.getId(context, "id"))));

        root.then(Lib99jArgumentTypes.identifierList(allowedIds).executes((context) -> openPonderFromId(context, IdentifierArgument.getId(context, "id"))));
    }

    private static int openPonderFromId(CommandContext<CommandSourceStack> context, Identifier id) throws CommandSyntaxException {
        if(checkIfOnGround(context)) return 0;
        if (CooldownManager.isOnCooldown(context.getSource().getPlayerOrException(), Identifier.fromNamespaceAndPath("ponder", "command"))) {
            context.getSource().sendFailure(Component.translatable("commands.lib99j.on_cooldown"));
            return 0;
        }
        if(!((CommandSourceAccessor) context.getSource()).lib99j$isFromPacket()) {
            throw CommandSourceStack.ERROR_NOT_PLAYER.create();
        }

        CooldownManager.setCooldown(context.getSource().getPlayerOrException(), Identifier.fromNamespaceAndPath("ponder", "command"), 10);

        PonderBuilder builder = PonderManager.idToBuilder.get(id);
        if (builder == null || (!Lib99j.isDevelopmentEnvironment && builder.shouldHideFromCommands())) {
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.id_not_found", id.toString()));
            return 0;
        }
        if (builder.shouldHideFromCommands()) {
            context.getSource().sendFailure(Component.translatable("commands.lib99j.ponder.hidden_from_commands", id.toString(), Component.translatable("commands.lib99j.ponder.id_not_found", id.toString())));
            return 0;
        }
        //send before so it isn't blocked by packet redirection
        context.getSource().sendSuccess(() -> Component.translatable("commands.lib99j.ponder.pondering_from_id", id.toString()), true);

        //if you try and ponder from a command block it causes a problem!
        try {
            builder.startPondering(context.getSource().getPlayerOrException());
        } catch (Exception e) {
            Lib99j.LOGGER.error("Player {} tried to ponder about {}, but an exception occurred", context.getSource().getPlayerOrException().getPlainTextName(), builder.getId().toString(), e);
            throw new SimpleCommandExceptionType(Component.translatable("commands.lib99j.ponder.failed_to_ponder")).create();
        }

        if(Lib99j.isDevelopmentEnvironment && context.getInput().contains("ui_creator_from")) {
            context.getSource().getPlayerOrException().connection.handleChatCommand(new ServerboundChatCommandPacket("ponder dev ui_creator"));
        }
        return 1;
    }
}
