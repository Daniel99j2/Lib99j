package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.config.ConfigContext;
import com.daniel99j.lib99j.api.config.ConfigHolder;
import com.daniel99j.lib99j.api.config.ConfigManager;
import com.daniel99j.lib99j.api.config.ModConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.permissions.PermissionLevel;

import java.net.URI;
import java.util.ArrayList;

public class Lib99jCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> rootCommand = Commands.literal("lib99j")
                .executes(Lib99jCommand::base)
                .then(Commands.literal("config")
                        .then(Commands.argument("mod-id", StringArgumentType.string())
                                .requires(Permissions.require("command.lib99j.config", PermissionLevel.GAMEMASTERS))
                                .suggests((context, builder) -> {
                                    for (String s : ConfigManager.configs.keySet()) {
                                        builder.suggest(s);
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(Lib99jCommand::modConfig)
                        .then(Commands.literal("reload").executes(Lib99jCommand::reloadModConfig))
                        .then(Commands.literal("reset").then(Commands.argument("confirmation", StringArgumentType.string()).executes(Lib99jCommand::resetModConfig))
                )))
                .build();

        dispatcher.getRoot().addChild(rootCommand);
    }

    private static int reloadModConfig(CommandContext<CommandSourceStack> context) {
        ModConfig config = ConfigManager.configs.get(StringArgumentType.getString(context, "mod-id"));

        config.reloadAvailable();

        context.getSource().sendSuccess(() -> Component.translatable("commands.lib99j.config.reload"), true);

        return 1;
    }

    private static int resetModConfig(CommandContext<CommandSourceStack> context) {
        if(!StringArgumentType.getString(context, "confirmation").equals("--force-reset")) context.getSource().sendFailure(Component.translatable("commands.lib99j.config.force_reset"));
        ModConfig config = ConfigManager.configs.get(StringArgumentType.getString(context, "mod-id"));

        for (ConfigHolder<?> availableConfig : config.getAvailableConfigs()) {
            availableConfig.reset();
        }

        config.reloadAvailable();

        context.getSource().sendSuccess(() -> Component.translatable("commands.lib99j.config.reset"), true);

        return 1;
    }

    private static int base(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ArrayList<Component> about = new ArrayList<>();
        about.add(Component.literal(""));
        about.add(Component.literal("Lib99j").setStyle(Style.EMPTY.withColor(0x69c6db).withBold(true)));
        about.add(Component.literal("Version: ").setStyle(Style.EMPTY.withColor(0xf7e1a7))
                .append(Component.literal(FabricLoader.getInstance().getModContainer(Lib99j.MOD_ID).get().getMetadata().getVersion().getFriendlyString()).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))));
        about.add(
                Component.literal("[").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)).append(Component.literal("Github").setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withClickEvent(new ClickEvent.OpenUrl(URI.create("https://github.com/Daniel99j2/Lib99j/")))).append(Component.literal("] ").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY))))
                        .append(Component.literal("[").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)).append(Component.literal("Modrinth").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent.OpenUrl(URI.create("https://modrinth.com/mod/lib99j")))).append(Component.literal("]").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)))))
                );
        about.add(Component.literal(""));

        for (Component component : about) {
            context.getSource().sendSuccess(() -> component, true);
        }

        return 1;
    }

    private static int modConfig(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ModConfig config = ConfigManager.configs.get(StringArgumentType.getString(context, "mod-id"));

        ModConfigDialog.openDialog(config, context.getSource().getPlayerOrException(), ConfigContext.COMMON);
        return 1;
    }
}
