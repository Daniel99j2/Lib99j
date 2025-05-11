package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.api.ServerParticle;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ServerParticleCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> rootCommand = CommandManager.literal("server-particle")
                .requires(Permissions.require("lib99j.command.server_particle", 2))
                .then(CommandManager.argument("particle", IdentifierArgumentType.identifier())
                        .suggests((context, builder) -> {
                            for (ServerParticleManager.ServerParticleType argument : ServerParticleManager.particleTypes) {
                                builder.suggest(String.valueOf(argument.id()));
                            }
                            return builder.buildFuture();
                        })
                        .then(CommandManager.argument("x", FloatArgumentType.floatArg())
                                .then(CommandManager.argument("y", FloatArgumentType.floatArg())
                                        .then(CommandManager.argument("z", FloatArgumentType.floatArg())
                                                .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 10000))
                                                        .executes(ServerParticleCommand::spawn)
                                                        .then(CommandManager.argument("nbt", StringArgumentType.greedyString())
                                                                .executes(ServerParticleCommand::spawn)
                                                        )))))
                ).build();

        dispatcher.getRoot().addChild(rootCommand);
    }

    private static int spawn(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        Identifier type = IdentifierArgumentType.getIdentifier(context, "particle");
        ServerParticle p;
        for (ServerParticleManager.ServerParticleType argument : ServerParticleManager.particleTypes) {
            if (argument.id().equals(type)) {
                for (int i = 0; i < IntegerArgumentType.getInteger(context, "count"); i++) {
                    p = argument.spawner().accept(source.getWorld().toServerWorld(), source.getPosition().getX() + FloatArgumentType.getFloat(context, "x"), source.getPosition().getY() + FloatArgumentType.getFloat(context, "y"), source.getPosition().getZ() + FloatArgumentType.getFloat(context, "z"));
                    try {
                        p.readNbt(StringNbtReader.readCompound(StringArgumentType.getString(context, "nbt")));
                    } catch (Exception ignored) {
                    }
                }
                source.sendFeedback(() -> Text.literal("Spawned particle"), false);
                return 1;
            }
        }
        source.sendError(Text.literal("No particle type found"));
        return 0;
    }
}
