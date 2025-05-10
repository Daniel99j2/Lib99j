package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.api.VFXUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class VfxCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> rootCommand = CommandManager.literal("vfx")
                .requires(Permissions.require("lib99j.command.vfx", 2))
                .then(CommandManager.literal("shake")
                        .then(CommandManager.argument("ticks", IntegerArgumentType.integer(-1, 1000))
                                .then(CommandManager.argument("strength", IntegerArgumentType.integer(1, 50))
                                        .executes(VfxCommand::shake))))
                .then(CommandManager.literal("generic")
                        .then(CommandManager.argument("ticks", IntegerArgumentType.integer(-1, 1000))
                                .then(CommandManager.argument("type", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            for (VFXUtils.GENERIC_SCREEN_EFFECT effect : VFXUtils.GENERIC_SCREEN_EFFECT.values()) {
                                                builder.suggest(effect.toString().toLowerCase());
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(VfxCommand::generic))))
                .then(CommandManager.literal("clear")
                        .executes(VfxCommand::clearAll)
                )
                .build();

        dispatcher.getRoot().addChild(rootCommand);
    }

    private static int shake(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        VFXUtils.shake(source.getPlayer(), IntegerArgumentType.getInteger(context, "ticks"), IntegerArgumentType.getInteger(context, "strength"), Identifier.ofVanilla("command"));
        source.sendFeedback(() -> Text.literal("Started camerashake"), true);
        return 1;
    }

    private static int generic(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        VFXUtils.GENERIC_SCREEN_EFFECT type;
        try {
            type = VFXUtils.GENERIC_SCREEN_EFFECT.valueOf(StringArgumentType.getString(context, "type").toUpperCase());
        } catch (Exception ignored) {
            source.sendError(Text.literal("Could not find screen effect type \""+StringArgumentType.getString(context, "type")+"\""));
            return 0;
        }
        VFXUtils.addGenericScreenEffect(source.getPlayer(), IntegerArgumentType.getInteger(context, "ticks"), type, Identifier.ofVanilla("command"));
        source.sendFeedback(() -> Text.literal("Started vfx"), true);
        return 1;
    }


    private static int clearAll(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        VFXUtils.clearGenericScreenEffects(source.getPlayer());
        VFXUtils.stopAllShaking(source.getPlayer());
        source.sendFeedback(() -> Text.literal("Cleared vfx"), true);
        return 1;
    }
}
