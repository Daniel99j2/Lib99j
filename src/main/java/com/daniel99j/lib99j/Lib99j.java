package com.daniel99j.lib99j;

import com.daniel99j.lib99j.api.*;
import com.daniel99j.lib99j.api.gui.DefaultGuiTextures;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.daniel99j.lib99j.impl.ServerParticleCommand;
import com.daniel99j.lib99j.impl.ServerParticleManager;
import com.daniel99j.lib99j.impl.VfxCommand;
import com.daniel99j.lib99j.impl.datagen.AssetProvider;
import com.daniel99j.lib99j.ponder.api.PonderBuilder;
import com.daniel99j.lib99j.ponder.impl.GuiTextures;
import com.daniel99j.lib99j.ponder.impl.PonderManager;
import com.daniel99j.lib99j.ponder.impl.instruction.ExecuteCodeInstruction;
import com.daniel99j.lib99j.ponder.impl.instruction.ShowItemInstruction;
import com.daniel99j.lib99j.ponder.impl.instruction.ShowLineInstruction;
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
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A library for all of Daniel99j's mods
 * @see GameProperties Enabling content mod settings
 * @see com.daniel99j.lib99j.api External API's
 */
@ApiStatus.Internal
public class Lib99j implements ModInitializer {
    public static final String MOD_ID = "lib99j";
    public static final Logger LOGGER = LoggerFactory.getLogger("Lib99j");
    @Nullable
    private static MinecraftServer server = null;
    public static ArrayList<ServerPlayer> additionalPlayers = new ArrayList<>();

    public static @Nullable MinecraftServer getServer() {
        return server;
    }

    public static @NotNull MinecraftServer getServerOrThrow() {
        if (server == null) throw new IllegalStateException("Server not loaded yet");
        return server;
    }

    public static void debug(String s) {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Lib99j.LOGGER.info(s);
        }
    }

    public static void debug(String s, Object o) {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Lib99j.LOGGER.info(s, o);
        }
    }

    @Override
    public void onInitialize() {
        String[] list = FabricLoader.getInstance().getLaunchArguments(true);
        for (int i = 0; i < list.length; i++) {
            if(Objects.equals(list[i], "--gameDir") && list.length > i+1 && list[i+1].endsWith("\\build\\datagen")) {
                GameProperties.markRunningDataGen();
                break;
            };
        }

        ServerParticleManager.load();
        GuiUtils.load();

        CustomEvents.GAME_LOADED.register(() -> {
            if(RegUtil.currentNamespace() != null) {
                throw new IllegalStateException("The current namespace was not cleared (currently set to {}".replace("{}", RegUtil.currentNamespace()));
            }
        });

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
            RegUtil.currentModNamespace("lib99jtestelements");
            TestingElements.init();
            RegUtil.finishedWithNamespace("lib99jtestelements");
        };

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
        });

        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
            ServerParticleManager.clearParticles();
            Lib99j.server = null;
        });

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
            RegistryPacketUtils.addRegistryModification(Registries.BIOME.registry(), (id, tag) -> {
                if(id.equals(Identifier.withDefaultNamespace("plains"))) {
                    if (tag instanceof CompoundTag compound) {
                        compound.putFloat("temperature", 0.0F);
                    }
                    return true;
                }
                return false;
            });
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (!ServerParticleManager.particleTypes.isEmpty() && GameProperties.areContentModsLoaded()) ServerParticleCommand.register(dispatcher);
            if (GameProperties.areContentModsLoaded() || FabricLoader.getInstance().isDevelopmentEnvironment()) VfxCommand.register(dispatcher);

            LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("lib99j-dev");
            
            if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
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
                            context.getSource().getPlayer().getInventory().add(DefaultGuiTextures.TEST_UI.asStack());
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("ghostblock")
                        .executes((context) -> {
                            context.getSource().getPlayer().connection.send(new ClientboundBlockUpdatePacket(BlockPos.containing(context.getSource().getPosition()), TestingElements.TEST.defaultBlockState()));
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("reloadworld")
                        .executes((context) -> {
                            PolymerUtils.reloadWorld(context.getSource().getPlayer());
                            return 1;
                        })
                        .build());

                builder.then(Commands.literal("ponder")
                        .executes((context) -> {
                            context.getSource().sendSystemMessage(Component.nullToEmpty("Pondering"));
                            PonderBuilder.create().title("Dev ponder menu").size(20, 20, 20).defaultBiome(Biomes.BASALT_DELTAS)
                                    .waitFor(1)
                                    .instruction(new ExecuteCodeInstruction((scene) -> {
                                        scene.getWorld().setBlockAndUpdate(scene.getOrigin().offset(4, 0, 4), Blocks.PISTON.defaultBlockState());
                                    }))
                                    .waitFor(1)
                                    .instruction(new ExecuteCodeInstruction((scene) -> {
                                        scene.getWorld().setBlockAndUpdate(scene.getOrigin().offset(4, 1, 4), Blocks.REDSTONE_BLOCK.defaultBlockState());
                                        Creeper creeper = new Creeper(EntityType.CREEPER, scene.getWorld());
                                        creeper.setPosRaw(scene.getOrigin().getX() + 5, scene.getOrigin().getY() + 10, scene.getOrigin().getZ() + 5);
                                        creeper.setPersistenceRequired();
                                        scene.getWorld().addFreshEntity(creeper);
                                        //scene.fastForwardUntil(2);
                                    }))
                                    .waitFor(1)
                                    .instruction(new ShowItemInstruction(1, Items.PISTON.getDefaultInstance()))
                                    .instruction(new ShowLineInstruction(1, 0xFF0000, Vec2.ZERO, new Vec2(10, 10), 10))
                                    .waitFor(2)
                                    .finishStep("creeper_boom")
                                    .waitFor(1)
                                    .instruction(new ShowItemInstruction(1, Items.STICKY_PISTON.getDefaultInstance()))
                                    .instruction(new ShowLineInstruction(1, 0x00FF00, Vec2.ZERO, new Vec2(20, 20), 5))
                                    .instruction(new ExecuteCodeInstruction((scene) -> {
                                        Cow creeper = new Cow(EntityType.COW, scene.getWorld());
                                        creeper.setPosRaw(scene.getOrigin().getX() + 5, scene.getOrigin().getY() + 10, scene.getOrigin().getZ() + 5);
                                        creeper.setPersistenceRequired();
                                        scene.getWorld().addFreshEntity(creeper);
                                    }))
                                    .waitFor(2)
                                    .finishStep("cow_moo")
                                    .waitFor(1)
                                    .instruction(new ShowItemInstruction(1, Items.TNT.getDefaultInstance()))
                                    .instruction(new ShowLineInstruction(1, 0x00FF00, Vec2.ZERO, new Vec2(20, 20), 5))
                                    .instruction(new ExecuteCodeInstruction((scene) -> {
                                        Chicken creeper = new Chicken(EntityType.CHICKEN, scene.getWorld());
                                        creeper.setPosRaw(scene.getOrigin().getX() + 5, scene.getOrigin().getY() + 10, scene.getOrigin().getZ() + 5);
                                        creeper.setPersistenceRequired();
                                        scene.getWorld().addFreshEntity(creeper);
                                    }))
                                    .waitFor(30)
                                    .finishStep("chicken_yum")
                                    .build()
                                    .startPondering(context.getSource().getPlayerOrException());
                            return 1;
                        })
                        .build());

                dispatcher.getRoot().addChild(builder.build());
            }
        });

        GuiTextures.init();

        GameProperties.enableHideableBossBar();

        GameProperties.enableCustomEffectBadLuck();

        LOGGER.info("Ready to rumble!");
    }

    public record BypassPacket(Packet<?> packet) implements Packet {
        @Override
        public PacketType<? extends Packet> type() {
            return null;
        }

        @Override
        public void handle(PacketListener listener) {
            throw new UnsupportedOperationException();
        }
    }
}