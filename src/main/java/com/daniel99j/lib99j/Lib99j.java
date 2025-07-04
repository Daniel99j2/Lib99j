package com.daniel99j.lib99j;

import com.daniel99j.lib99j.api.*;
import com.daniel99j.lib99j.impl.ServerParticleCommand;
import com.daniel99j.lib99j.impl.ServerParticleManager;
import com.daniel99j.lib99j.impl.VfxCommand;
import com.daniel99j.lib99j.impl.datagen.AssetProvider;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A library for all of Daniel99j's mods
 * @see GameProperties Enabling content mod settings
 * @see com.daniel99j.lib99j.api External API's
 * @see <a href="https://github.com/Daniel99j2/Starbound">Starbound, a good example of usage</a>
 */
public class Lib99j implements ModInitializer {
    public static final String MOD_ID = "lib99j";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Nullable
    private static MinecraftServer server = null;

    public static @Nullable MinecraftServer getServer() {
        return server;
    }

    public static @NotNull MinecraftServer getServerOrThrow() {
        if (server == null) throw new NullPointerException();
        return server;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Ready to rumble!");
        String[] list = FabricLoader.getInstance().getLaunchArguments(true);
        for (int i = 0; i < list.length; i++) {
            if(Objects.equals(list[i], "--gameDir") && list.length > i+1 && list[i+1].endsWith("\\build\\datagen")) {
                GameProperties.runningDataGen = true;
                break;
            };
        }

        ServerParticleManager.load();
        GuiUtils.load();

        ServerLifecycleEvents.SERVER_STARTED.register((server1) -> server = server1);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
                CustomEvents.GAME_LOADED.invoke();
            });
        } else if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            ServerLifecycleEvents.SERVER_STARTED.register((client) -> {
                CustomEvents.GAME_LOADED.invoke();
            });
        }

        PolymerResourcePackUtils.markAsRequired();
        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register((builder -> AssetProvider.runWriters(builder::addData)));

        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            ServerParticleManager.tick();
            VFXUtils.tick();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
            ServerParticleManager.clearParticles();
            Lib99j.server = null;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (!ServerParticleManager.particleTypes.isEmpty()) ServerParticleCommand.register(dispatcher);
            VfxCommand.register(dispatcher);
        });
    }
}