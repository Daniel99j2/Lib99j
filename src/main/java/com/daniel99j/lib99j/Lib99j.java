package com.daniel99j.lib99j;

import com.daniel99j.lib99j.api.CustomEvents;
import com.daniel99j.lib99j.api.GuiUtils;
import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.impl.ServerParticleCommand;
import com.daniel99j.lib99j.impl.ServerParticleManager;
import com.daniel99j.lib99j.impl.VfxCommand;
import com.daniel99j.lib99j.impl.datagen.AssetProvider;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Lib99j implements ModInitializer {
	public static final String MOD_ID = "lib99j";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Nullable
	private static MinecraftServer server = null;

	@Override
	public void onInitialize() {
		LOGGER.info("Ready to rumble!");

		ServerLifecycleEvents.SERVER_STARTED.register((server1) -> server = server1);
		ServerLifecycleEvents.SERVER_STOPPED.register((server1) -> server = null);

		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
				CustomEvents.GAME_LOADED.invoke();
			});
		} else if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			ServerLifecycleEvents.SERVER_STARTED.register((client) -> {
				CustomEvents.GAME_LOADED.invoke();
			});
		}

		PolymerResourcePackUtils.markAsRequired();
		PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register((builder -> AssetProvider.runWriters(builder::addData)));

		CustomEvents.GAME_LOADED.register(() -> {
			ServerParticleManager.load();
			GuiUtils.load();
		});

		ServerTickEvents.END_SERVER_TICK.register((server) -> {
			ServerParticleManager.tick();
			VFXUtils.tick();
		});

		ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
			ServerParticleManager.clearParticles();
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			if(!ServerParticleManager.particleTypes.isEmpty()) ServerParticleCommand.register(dispatcher);
			VfxCommand.register(dispatcher);
		});
	}

	public static @Nullable MinecraftServer getServer() {
		return server;
	}

	public static @NotNull MinecraftServer getServerOrThrow() {
		if(server == null) throw new NullPointerException();
		return server;
	}
}