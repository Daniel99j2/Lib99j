package com.daniel99j.lib99j;

import com.daniel99j.lib99j.api.*;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.impl.*;
import com.daniel99j.lib99j.impl.datagen.AssetProvider;
import com.daniel99j.lib99j.ponder.api.PonderBuilder;
import com.daniel99j.lib99j.ponder.api.PonderManager;
import com.daniel99j.lib99j.ponder.impl.PonderCommand;
import com.daniel99j.lib99j.testmod.TestingElements;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * A library for all of Daniel99j's mods
 * @see GameProperties Enabling content mod settings
 * @see com.daniel99j.lib99j.api External API's
 * @see TestingElements Example usage
 */
@ApiStatus.Internal
public class Lib99j implements ModInitializer {
    public static final String MOD_ID = "lib99j";
    public static final Logger LOGGER = LoggerFactory.getLogger("Lib99j");
    @Nullable
    private static MinecraftServer server = null;
    public static ArrayList<ServerPlayer> additionalPlayers = new ArrayList<>();

    public static final List<String> SUPPORTED_LANGUAGES = List.of("en_us", "en_au", "en_ca", "en_gb", "en_nz", "en_pt", "en_ud", "en_us", "enws", "lol_us");

    public static boolean isDevelopingLib99j = false;
    public static boolean isDevelopmentEnvironment = FabricLoader.getInstance().isDevelopmentEnvironment() || Files.exists(FabricLoader.getInstance().getConfigDir().resolve("lib99j_force_dev.txt"));
    public static boolean personalFeatures = System.getenv("DANIEL99J_MODE") != null;

    private static boolean hasLoadedLib99j = false;

    public static @Nullable MinecraftServer getServer() {
        return server;
    }

    public static @NotNull MinecraftServer getServerOrThrow() {
        if (server == null) throw new IllegalStateException("Server not loaded yet");
        return server;
    }

    public static void debug(String s) {
        if(isDevelopmentEnvironment) {
            Lib99j.LOGGER.info(s);
        }
    }

    public static void debug(String s, Object o) {
        if(isDevelopmentEnvironment) {
            Lib99j.LOGGER.info(s, o);
        }
    }

    @Override
    public void onInitialize() {
        ensureLoaded();
    }

    public static void ensureLoaded() {
        if(hasLoadedLib99j) return;
        hasLoadedLib99j = true;

        String[] list = FabricLoader.getInstance().getLaunchArguments(true);
        for (int i = 0; i < list.length; i++) {
            if (Objects.equals(list[i], "--gameDir") && list.length > i + 1) {
                if (list[i + 1].endsWith("\\build\\datagen")) {
                    GameProperties.markRunningDataGen();
                    break;

                    //If it is running on my machine, I enable all features so I can develop Lib99j
                    //If these were enabled in normal dev mode, it would mean other mods would sometimes only work in dev
                    //If you want to work on Lib99j, feel free to edit this so it also works for you
                }
            }
        }

        isDevelopingLib99j = System.getenv("DEVELOPING_LIB99J") != null && isDevelopmentEnvironment && FabricLoader.getInstance().getConfigDir().getParent().toString().contains("Lib99j");

        ServerParticleManager.load();
        GuiUtils.load();

        CustomEvents.GAME_LOADED.register(() -> {
            if(RegUtil.currentNamespace() != null) {
                throw new IllegalStateException("The current namespace was not cleared (currently set to {}".replace("{}", RegUtil.currentNamespace()));
            }
        });

        //only enable features on my machine so other mod developers do not forget to enable them for their mods
        if(isDevelopingLib99j) {
            GameProperties.enablePonder();
            GameProperties.enableCustomEffectBadLuck();

            RegUtil.currentModNamespace("lib99jtestelements");
            TestingElements.init();
            RegUtil.finishedWithNamespace("lib99jtestelements");

            //this one is just for me
            RegistryPacketUtils.addRegistryModification(Registries.BIOME.registry(), (id, tag) -> {
                if(id.equals(Identifier.withDefaultNamespace("plains"))) {
                    if (tag instanceof CompoundTag compound) {
                        compound.putFloat("temperature", 0.0F);
                    }
                    return true;
                }
                return false;
            });
        };

        ServerLifecycleEvents.SERVER_STARTING.register((server1) -> {
            server = server1;
        });

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

        ServerTickEvents.START_SERVER_TICK.register((server) -> {
            if(server.tickRateManager().isFrozen()) return;
            ServerParticleManager.tick();
            VFXUtils.tick();
            PonderManager.tick();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            PonderManager.activeScenes.forEach(((player, ponderScene) -> {
                ponderScene.stopPondering(true);
            }));
            PonderManager.activeScenes.clear();
            PonderManager.scenesAboutToStart.clear();
            RunCodeClickEvent.eventMap.clear();
            ServerParticleManager.clearParticles();

            Path ponderPath = Lib99j.getServerOrThrow().storageSource.getDimensionPath(ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("ponder", "delete"))).getParent();
            if(Files.exists(ponderPath)) try {
                Files.walkFileTree(ponderPath, new SimpleFileVisitor<>() {
                    @Override
                    public @NotNull FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public @NotNull FileVisitResult postVisitDirectory(@NonNull Path dir, IOException exc) throws IOException {
                        Files.deleteIfExists(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (Exception e) {
                Lib99j.LOGGER.error("Failed to remove save data for temporary ponder levels", e);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
            Lib99j.server = null;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (!ServerParticleManager.particleTypes.isEmpty() && GameProperties.areContentModsLoaded()) ServerParticleCommand.register(dispatcher);
            if (GameProperties.areContentModsLoaded() || Lib99j.isDevelopmentEnvironment) VfxCommand.register(dispatcher);
            PonderCommand.register(dispatcher, registryAccess);

            LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("lib99j-dev");

            if(Lib99j.isDevelopmentEnvironment) {
                builder.then(Commands.literal("translationcheck")
                        .executes((context) -> {
                            GuiUtils.doesPlayerHaveMods(context.getSource().getPlayer(), Map.of("vanilla", "controls.reset", "hacks", "x13.mod.xray"), (e) -> {
                                for (String s : e.matches())
                                    context.getSource().getPlayer().sendSystemMessage(Component.literal("match: " + s));
                                for (String s : e.misses())
                                    context.getSource().getPlayer().sendSystemMessage(Component.literal("miss: " + s));
                                context.getSource().getPlayer().sendSystemMessage(Component.literal("failed: " + e.checkFailed()));
                                context.getSource().getPlayer().sendSystemMessage(Component.literal("blocked: " + e.translationCheckBlocked()));
                            });
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("toast")
                        .then(Commands.argument("icon", ItemArgument.item(registryAccess))
                                .then(Commands.argument("title", ComponentArgument.textComponent(registryAccess))
                                        .executes((context) -> {
                                            GuiUtils.toast(context.getSource().getPlayer(), ItemArgument.getItem(context, "icon").createItemStack(1, true), ComponentArgument.getRawComponent(context, "title"), Identifier.withDefaultNamespace("test"));
                                            return 1;
                                        }))).build());

                builder.then(Commands.literal("clientexplode")
                        .executes((context) -> {
                            ArrayList<ServerPlayer> players = new ArrayList<>();
                            players.add(context.getSource().getPlayer());
                            VFXUtils.clientSideExplode(players, new ExplosionDamageCalculator(), context.getSource().getPosition().x(), context.getSource().getPosition().y(), context.getSource().getPosition().z(), 5, true, ParticleTypes.EXPLOSION_EMITTER, ParticleTypes.EXPLOSION, SoundEvents.GENERIC_EXPLODE, true);
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("garbagecollect")
                        .executes((context) -> {
                            System.gc();
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("clearparticles")
                        .executes((context) -> {
                            VFXUtils.clearParticles(context.getSource().getPlayer());
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("firework")
                        .executes((context) -> {
                            ArrayList<ServerPlayer> players = new ArrayList<>();
                            players.add(context.getSource().getPlayer());
                            VFXUtils.fireworkExplode(players, List.of(new FireworkExplosion(FireworkExplosion.Shape.BURST, IntList.of(0x0000ff), IntList.of(0xff0000), true, true)), context.getSource().getPosition());
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("fireworkangle")
                        .executes((context) -> {
                            ArrayList<ServerPlayer> players = new ArrayList<>();
                            players.add(context.getSource().getPlayer());
                            VFXUtils.fireworkExplode(players, List.of(new FireworkExplosion(FireworkExplosion.Shape.BURST, IntList.of(0x0000ff), IntList.of(0xff0000), true, true)), context.getSource().getPosition(), new Vec3(1, -1, 0));
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("lightning")
                        .executes((context) -> {
                            ArrayList<ServerPlayer> players = new ArrayList<>();
                            players.add(context.getSource().getPlayer());
                            VFXUtils.sendFakeEntity(players, context.getSource().getPosition(), EntityType.LIGHTNING_BOLT);
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("compacttext")
                        .executes((context) -> {
                            MutableComponent test = GuiUtils.compactText(Component.literal("testing").append("123").append(Component.literal("4")).append(Component.literal("5").withColor(0x00ff00)).append("6"));
                            context.getSource().sendSystemMessage(test);
                            context.getSource().sendSystemMessage(Component.literal(test.toString()));
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("tplockedcamera")
                        .executes((context) -> {
                            Vec3 pos = ((Lib99jPlayerUtilController) context.getSource().getPlayer()).lib99j$getCameraWorldPos();
                            ((Lib99jPlayerUtilController) context.getSource().getPlayer()).lib99j$unlockCamera();
                            context.getSource().getPlayer().connection.send(new BypassPacket(new ClientboundPlayerPositionPacket(-100, new PositionMoveRotation(pos, Vec3.ZERO, 0, 0), Set.of())));
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("testrandomcode")
                        .executes((context) -> {
                            context.getSource().getPlayer().getInventory().add(TestingElements.TEST_UI_ITEM.asStack());
                            context.getSource().getPlayer().getInventory().add(TestingElements.TEST_VANILLA_GUI_ITEM.asStack());
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("ghostblock")
                        .executes((context) -> {
                            context.getSource().getPlayer().connection.send(new ClientboundBlockUpdatePacket(BlockPos.containing(context.getSource().getPosition()), TestingElements.TEST.defaultBlockState()));
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("closeallmenus")
                        .executes((context) -> {
                            context.getSource().getPlayer().connection.send(new ClientboundContainerClosePacket(-1));
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("reloadworld")
                        .executes((context) -> {
                            PolymerUtils.reloadWorld(context.getSource().getPlayer());
                            return 1;
                        })
                        .build());

                dispatcher.getRoot().addChild(builder.build());
            }
        });

        PonderBuilder.hotswapExample();

        PonderManager.load();
        LOGGER.info("Ready to rumble!");
    }
}