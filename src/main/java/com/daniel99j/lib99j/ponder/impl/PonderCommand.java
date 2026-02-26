package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.ponder.api.PonderManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class PonderCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("ponder");

        root.then(buildNamedPonders());

        root.then(Commands.literal("config")).then(Commands.literal("reload")).executes((ctx) -> {
            return 1;
        });

        dispatcher.getRoot().addChild(root.build());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildNamedPonders() {
        LiteralArgumentBuilder<CommandSourceStack> branch = Commands.literal("name");

        PonderManager.idToBuilder.forEach(((identifier, builder) -> {
            branch.then(Commands.literal(identifier.toString()).executes((context) -> {
                //send before so it isn't blocked by packet redirection
                context.getSource().sendSystemMessage(Component.translatable("commands.lib99j.ponder.pondering_name", identifier.toString()));
                builder.startPondering(context.getSource().getPlayerOrException());
                return 1;
            }));
        }));

        return branch;
    }
}
