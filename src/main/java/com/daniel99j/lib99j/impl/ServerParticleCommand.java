package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.api.ServerParticle;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ServerParticleCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> rootCommand = Commands.literal("server-particle")
                .requires(Permissions.require("lib99j.command.server_particle", 2))
                .then(Commands.argument("particle", IdentifierArgument.id())
                        .suggests((context, builder) -> {
                            for (ServerParticleManager.ServerParticleType argument : ServerParticleManager.particleTypes) {
                                builder.suggest(String.valueOf(argument.id()));
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("x", FloatArgumentType.floatArg())
                                .then(Commands.argument("y", FloatArgumentType.floatArg())
                                        .then(Commands.argument("z", FloatArgumentType.floatArg())
                                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 10000))
                                                        .executes(ServerParticleCommand::spawn)
                                                        .then(Commands.argument("nbt", StringArgumentType.greedyString())
                                                                .executes(ServerParticleCommand::spawn)
                                                        )))))
                ).build();

        dispatcher.getRoot().addChild(rootCommand);
    }

    private static int spawn(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Identifier type = IdentifierArgument.getId(context, "particle");
        ServerParticle p;
        for (ServerParticleManager.ServerParticleType argument : ServerParticleManager.particleTypes) {
            if (argument.id().equals(type)) {
                for (int i = 0; i < IntegerArgumentType.getInteger(context, "count"); i++) {
                    p = argument.spawner().accept(source.getLevel().getLevel(), source.getPosition().x() + FloatArgumentType.getFloat(context, "x"), source.getPosition().y() + FloatArgumentType.getFloat(context, "y"), source.getPosition().z() + FloatArgumentType.getFloat(context, "z"));
                    try {
                        p.readNbt(TagParser.parseCompoundFully(StringArgumentType.getString(context, "nbt")));
                    } catch (Exception ignored) {
                    }
                }
                source.sendSuccess(() -> Component.literal("Spawned particle"), false);
                return 1;
            }
        }
        source.sendFailure(Component.literal("No particle type found"));
        return 0;
    }
}
