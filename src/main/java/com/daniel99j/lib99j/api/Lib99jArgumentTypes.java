package com.daniel99j.lib99j.api;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class Lib99jArgumentTypes {
    public static ArgumentBuilder<CommandSourceStack, RequiredArgumentBuilder<CommandSourceStack, Identifier>> identifierList(List<Identifier> validIds) {
        List<String> allowedIds = new ArrayList<>();
        for (Identifier validId : validIds) {
            allowedIds.add(validId.toString());
        }

        return Commands.argument("id", IdentifierArgument.id()).suggests((context, builder) -> {
            String input = builder.getRemaining().toLowerCase();

            // Sort so "startsWith" matches come first
            List<String> sorted = allowedIds.stream()
                    .sorted((a, b) -> {
                        boolean aStarts = a.startsWith(input);
                        boolean bStarts = b.startsWith(input);

                        if (aStarts && !bStarts) return -1;
                        if (!aStarts && bStarts) return 1;
                        return a.compareTo(b);
                    })
                    .toList();

            for (String id : sorted) {
                if (id.contains(input)) {
                    builder.suggest(id);
                }
            }

            return builder.buildFuture();
        });
    }
}