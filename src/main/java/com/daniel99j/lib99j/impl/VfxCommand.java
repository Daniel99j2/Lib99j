package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.GenericScreenEffect;
import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.api.config.ConfigContext;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public class VfxCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if(!((Lib99jCommonConfig) Lib99j.CONFIG.getOrThrow(ConfigContext.COMMON).getOrThrow()).enableLib99jCommands) return;

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
                                            for (GenericScreenEffect effect : GenericScreenEffect.values()) {
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

    private static int shake(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        VFXUtils.shake(source.getPlayerOrException(), IntegerArgumentType.getInteger(context, "ticks"), IntegerArgumentType.getInteger(context, "strength"), Identifier.withDefaultNamespace("command"));
        source.sendSuccess(() -> Component.translatable("commands.lib99j.camerashake.success", Objects.requireNonNull(source.getPlayer().getName())), true);
        return 1;
    }

    private static int generic(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        GenericScreenEffect type;
        try {
            type = GenericScreenEffect.valueOf(StringArgumentType.getString(context, "type").toUpperCase());
        } catch (Exception ignored) {
            source.sendFailure(Component.translatable("commands.lib99j.vfx.could_not_find", StringArgumentType.getString(context, "type")));
            return 0;
        }
        VFXUtils.addGenericScreenEffect(source.getPlayerOrException(), IntegerArgumentType.getInteger(context, "ticks"), type, Identifier.withDefaultNamespace("command"));
        source.sendSuccess(() -> Component.translatable("commands.lib99j.vfx.success", Objects.requireNonNull(source.getPlayer().getName())), true);
        return 1;
    }


    private static int clearAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        VFXUtils.clearGenericScreenEffects(source.getPlayerOrException());
        VFXUtils.stopAllShaking(source.getPlayer());
        source.sendSuccess(() -> Component.translatable("commands.lib99j.vfx.success_clear", Objects.requireNonNull(source.getPlayer().getName())), true);
        return 1;
    }
}
