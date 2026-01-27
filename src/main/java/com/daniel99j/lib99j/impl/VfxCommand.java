package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.api.VFXUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class VfxCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> rootCommand = Commands.literal("vfx")
                .requires(Permissions.require("lib99j.command.vfx", 2))
                .then(Commands.literal("shake")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(-1, 1000))
                                .then(Commands.argument("strength", IntegerArgumentType.integer(1, 50))
                                        .executes(VfxCommand::shake))))
                .then(Commands.literal("generic")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(-1, 1000))
                                .then(Commands.argument("type", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            for (VFXUtils.GENERIC_SCREEN_EFFECT effect : VFXUtils.GENERIC_SCREEN_EFFECT.values()) {
                                                builder.suggest(effect.toString().toLowerCase());
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(VfxCommand::generic))))
                .then(Commands.literal("clear")
                        .executes(VfxCommand::clearAll)
                )
                .build();

        dispatcher.getRoot().addChild(rootCommand);
    }

    private static int shake(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        VFXUtils.shake(source.getPlayer(), IntegerArgumentType.getInteger(context, "ticks"), IntegerArgumentType.getInteger(context, "strength"), Identifier.withDefaultNamespace("command"));
        source.sendSuccess(() -> Component.literal("Started camerashake"), true);
        return 1;
    }

    private static int generic(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        VFXUtils.GENERIC_SCREEN_EFFECT type;
        try {
            type = VFXUtils.GENERIC_SCREEN_EFFECT.valueOf(StringArgumentType.getString(context, "type").toUpperCase());
        } catch (Exception ignored) {
            source.sendFailure(Component.literal("Could not find screen effect type \"" + StringArgumentType.getString(context, "type") + "\""));
            return 0;
        }
        VFXUtils.addGenericScreenEffect(source.getPlayer(), IntegerArgumentType.getInteger(context, "ticks"), type, Identifier.withDefaultNamespace("command"));
        source.sendSuccess(() -> Component.literal("Started vfx"), true);
        return 1;
    }


    private static int clearAll(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        VFXUtils.clearGenericScreenEffects(source.getPlayer());
        VFXUtils.stopAllShaking(source.getPlayer());
        source.sendSuccess(() -> Component.literal("Cleared vfx"), true);
        return 1;
    }
}
