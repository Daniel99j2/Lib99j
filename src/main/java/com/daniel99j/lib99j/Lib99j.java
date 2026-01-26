package com.daniel99j.lib99j;

import com.daniel99j.lib99j.api.*;
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
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.entity.EntityPosition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Items;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A library for all of Daniel99j's mods
 * @see GameProperties Enabling content mod settings
 * @see com.daniel99j.lib99j.api External API's
 * @see <a href="https://github.com/Daniel99j2/Starbound">Starbound, a good example of usage</a>
 */
public class Lib99j implements ModInitializer {
    public static final String MOD_ID = "lib99j";
    public static final Logger LOGGER = LoggerFactory.getLogger("Lib99j");
    @Nullable
    private static MinecraftServer server = null;

    public static @Nullable MinecraftServer getServer() {
        return server;
    }

    public static @NotNull MinecraftServer getServerOrThrow() {
        if (server == null) throw new IllegalStateException("Server not loaded yet");
        return server;
    }

    @Override
    public void onInitialize() {
        String[] list = FabricLoader.getInstance().getLaunchArguments(true);
        for (int i = 0; i < list.length; i++) {
            if(Objects.equals(list[i], "--gameDir") && list.length > i+1 && list[i+1].endsWith("\\build\\datagen")) {
                GameProperties.runningDataGen = true;
                break;
            };
        }

        ServerParticleManager.load();
        GuiUtils.load();

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) TestingElements.init();

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
            if(server.getTickManager().isFrozen()) return;
            ServerParticleManager.tick();
            VFXUtils.tick();
            PonderManager.tick();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
            ServerParticleManager.clearParticles();
            Lib99j.server = null;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (!ServerParticleManager.particleTypes.isEmpty() && GameProperties.contentModsLoaded) ServerParticleCommand.register(dispatcher);
            if (GameProperties.contentModsLoaded) VfxCommand.register(dispatcher);
            dispatcher.getRoot().addChild(CommandManager.literal("translationcheck")
                    .executes((context) -> {
                        GuiUtils.doesPlayerHaveMods(context.getSource().getPlayer(), Map.of("vanilla", "controls.reset", "hacks", "x13.mod.xray", "test", "controls.reset", "vanilla1", "addServer.add", "hacks1", "x13.mod.xray", "test1", "controls.reset", "vanilla2", "addServer.add", "hacks2", "x13.mod.xray", "test2", "controls.reset"), (e) -> {
                            for (String s : e.matches()) context.getSource().getPlayer().sendMessage(Text.literal("match: " + s));
                            for (String s : e.misses()) context.getSource().getPlayer().sendMessage(Text.literal("miss: " + s));
                            context.getSource().getPlayer().sendMessage(Text.literal("failed: " + e.checkFailed()));
                            context.getSource().getPlayer().sendMessage(Text.literal("blocked: " + e.translationCheckBlocked()));
                        });
                        return 1;
                    })
                    .build());

            dispatcher.getRoot().addChild(CommandManager.literal("toast")
                            .then(CommandManager.argument("icon", ItemStackArgumentType.itemStack(registryAccess))
                                    .then(CommandManager.argument("title", TextArgumentType.text(registryAccess))
                                            .then(CommandManager.argument("desc", TextArgumentType.text(registryAccess))
                    .executes((context) -> {
                        GuiUtils.toast(context.getSource().getPlayer(), ItemStackArgumentType.getItemStackArgument(context, "icon").createStack(2,  true), TextArgumentType.getTextArgument(context, "title"), TextArgumentType.getTextArgument(context, "desc"), Identifier.ofVanilla("test"));
                        return 1;
                    })))).build());

            dispatcher.getRoot().addChild(CommandManager.literal("clientexplode")
                    .executes((context) -> {
                        ArrayList<ServerPlayerEntity> players = new ArrayList<>();
                        players.add(context.getSource().getPlayer());
                        VFXUtils.clientSideExplode(players, new ExplosionBehavior(), context.getSource().getPosition().getX(), context.getSource().getPosition().getY(), context.getSource().getPosition().getZ(), 5, true, ParticleTypes.EXPLOSION_EMITTER, ParticleTypes.EXPLOSION, SoundEvents.ENTITY_GENERIC_EXPLODE, true);
                        return 1;
                    })
                    .build());

            dispatcher.getRoot().addChild(CommandManager.literal("tplockedcamera")
                    .executes((context) -> {
                        Vec3d pos = ((Lib99jPlayerUtilController) context.getSource().getPlayerOrThrow()).lib99j$getCameraWorldPos();
                        VFXUtils.clearGenericScreenEffects(context.getSource().getPlayerOrThrow());
                        context.getSource().getPlayerOrThrow().networkHandler.sendPacket(PlayerPositionLookS2CPacket.of(-1, new EntityPosition(pos, Vec3d.ZERO, 0, 0), Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z)));
                        return 1;
                    })
                    .build());

            dispatcher.getRoot().addChild(CommandManager.literal("noponder")
                    .executes((context) -> {
                        VFXUtils.removeGenericScreenEffect(context.getSource().getPlayer(), Identifier.of("ponder", "ponder_lock"));
                        VFXUtils.removeGenericScreenEffect(context.getSource().getPlayer(), Identifier.of("ponder", "ponder_bright"));
                        PolymerUtils.reloadWorld(context.getSource().getPlayer());
                        return 1;
                    })
                    .build());

            dispatcher.getRoot().addChild(CommandManager.literal("ghostblock")
                    .executes((context) -> {
                        context.getSource().getPlayer().networkHandler.sendPacket(new BlockUpdateS2CPacket(BlockPos.ofFloored(context.getSource().getPosition()), TestingElements.TEST.getDefaultState()));
                        return 1;
                    })
                    .build());

            dispatcher.getRoot().addChild(CommandManager.literal("reloadworld")
                    .executes((context) -> {
                        PolymerUtils.reloadWorld(context.getSource().getPlayer());
                        return 1;
                    })
                    .build());

            dispatcher.getRoot().addChild(CommandManager.literal("singleponder")
                    .executes((context) -> {
                        VFXUtils.addGenericScreenEffect(context.getSource().getPlayer(), -1, VFXUtils.GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS, Identifier.of("ponder", "ponder_lock"));
                        VFXUtils.addGenericScreenEffect(context.getSource().getPlayer(), -1, VFXUtils.GENERIC_SCREEN_EFFECT.NIGHT_VISION, Identifier.of("ponder", "ponder_bright"));

                        ServerPlayerEntity realPlayer = context.getSource().getPlayer();

                        BlockPos renderPos = new BlockPos((int) (100000), 60, 0);
                        VFXUtils.setCameraPos(realPlayer, Vec3d.of(renderPos));
                        realPlayer.networkHandler.sendPacket(new ChunkRenderDistanceCenterS2CPacket(ChunkSectionPos.getSectionCoord(renderPos.getX()), ChunkSectionPos.getSectionCoord(renderPos.getZ()) ));
                        realPlayer.networkHandler.sendPacket(PlayerPositionLookS2CPacket.of(-69, new EntityPosition(Vec3d.of(renderPos), Vec3d.ZERO, 0, 0), Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z)));

                        ServerWorld serverWorld = Lib99j.getServerOrThrow().getWorld(World.NETHER);

                        CreeperEntity creeper = new CreeperEntity(EntityType.CREEPER, serverWorld) {
                            @Override
                            public void tick() {
                                super.tick();
                                this.age++;
                                if(this.age == 200) {
                                    ChunkSenderUtil.sendRegion(realPlayer, new ChunkPos(ChunkSectionPos.getSectionCoord(renderPos.getX()) - 1, ChunkSectionPos.getSectionCoord(renderPos.getZ()) - 1), new ChunkPos(ChunkSectionPos.getSectionCoord(renderPos.getX()) + 1, ChunkSectionPos.getSectionCoord(renderPos.getZ()) + 1), serverWorld);
                                    this.discard();
                                }
                            }
                        };
                        creeper.setPos(realPlayer.getEntityPos().getX(), realPlayer.getEntityPos().getY(), realPlayer.getEntityPos().getZ());
                        creeper.setPersistent();
                        creeper.setNoGravity(true);
                        realPlayer.getEntityWorld().spawnEntity(creeper);

                        context.getSource().sendMessage(Text.of("done"));

                        return 1;
                    }).build());

            //old

            dispatcher.getRoot().addChild(CommandManager.literal("ponder")
                    .executes((context) -> {
                        context.getSource().sendMessage(Text.of("Pondering"));
                        PonderBuilder.create().title("Dev ponder menu").size(20, 20, 20).floorBlocks(Blocks.TNT.getDefaultState(), Blocks.DIAMOND_BLOCK.getDefaultState()).defaultBiome(BiomeKeys.BASALT_DELTAS)
                                .wait(100)
                                .instruction(new ExecuteCodeInstruction((scene) -> {
                                    scene.getWorld().setBlockState(scene.getOrigin(), Blocks.PISTON.getDefaultState());
                                }))
                                .wait(100)
                                .instruction(new ExecuteCodeInstruction((scene) -> {
                                    scene.getWorld().setBlockState(scene.getOrigin().add(0, 1, 0), Blocks.REDSTONE_BLOCK.getDefaultState());
                                    CreeperEntity creeper = new CreeperEntity(EntityType.CREEPER, scene.getWorld());
                                    creeper.setPos(scene.getOrigin().getX()-5, scene.getOrigin().getY()+10, scene.getOrigin().getZ()-5);
                                    creeper.setPersistent();
                                    scene.getWorld().spawnEntity(creeper);
                                }))
                                .wait(100)
                                .instruction(new ShowItemInstruction(1000, Items.PISTON.getDefaultStack()))
                                .instruction(new ShowLineInstruction(1000, 0xFF0000, Vec2f.ZERO, new Vec2f(10, 10), 10))
                                .wait(200)
                                .finishStep(Text.literal("Step Title"), Text.literal("Step Description"))
                                .wait(100)
                                .instruction(new ShowItemInstruction(1000, Items.STICKY_PISTON.getDefaultStack()))
                                .instruction(new ShowLineInstruction(1000, 0x00FF00, Vec2f.ZERO, new Vec2f(20, 20), 5))
                                .wait(200)
                                .finishStep(Text.literal("Step Title2"), Text.literal("Step Description2"))
                                .build()
                                .startPondering(context.getSource().getPlayerOrThrow());
                        return 1;
                    })
                    .build());
        });

        LOGGER.info("Ready to rumble!");

        GuiTextures.init();
    }

    public record BypassPacket(Packet<?> packet) implements Packet {
        @Override
        public PacketType<? extends Packet> getPacketType() {
            return null;
        }

        @Override
        public void apply(PacketListener listener) {
            throw new UnsupportedOperationException();
        }
    }
}