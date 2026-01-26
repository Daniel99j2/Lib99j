package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.*;
import com.daniel99j.lib99j.api.gui.DefaultGuiTextures;
import com.daniel99j.lib99j.impl.LocalChannelAccessor;
import com.daniel99j.lib99j.ponder.impl.PonderManager;
import com.daniel99j.lib99j.ponder.impl.PonderStep;
import com.daniel99j.lib99j.ponder.impl.instruction.PonderInstruction;
import com.mojang.serialization.Lifecycle;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.handler.LegacyQueryHandler;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.c2s.play.PlayerLoadedC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.server.network.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PonderScene {
    public final ServerPlayerEntity player;
    private final ServerWorld world;
    public final PonderBuilder builder;
    public final int floorHeight;
    public final UUID uuid;
    private final ElementHolder elementHolder;
    private int currentStep = 0;
    private int currentInstructionInStep = 0;
    private final ArrayList<PonderStep> steps;
    private final ArrayList<PonderInstruction> activeInstructions = new ArrayList<>();
    public final RegistryKey<World> worldKey;
    private final ServerPlayerEntity packetRedirector;
    private final BlockPos origin;
    private boolean isToBeRemoved = false;
    private final ServerBossBar displayTopBar;

    protected PonderScene(ServerPlayerEntity player, PonderBuilder builder) {
        if (PonderManager.isPondering(player)) PonderManager.activeScenes.get(player).stopPondering();
        this.player = player;
        this.player.networkHandler.sendPacket(new TitleS2CPacket(Text.of("FADE EFFECT")));
        this.player.networkHandler.sendPacket(new SubtitleS2CPacket(DefaultGuiTextures.BIG_WHITE_SQUARE.text().withColor(0xFF00FF)));
        this.player.networkHandler.sendPacket(new TitleFadeS2CPacket(0, 10000, 5));

        this.builder = builder;
        ArrayList<PonderStep> copiedSteps = new ArrayList<>();
        for (PonderStep step : builder.steps) {
            copiedSteps.add(step.clone());
        }
        this.steps = copiedSteps;

        this.uuid = UUID.randomUUID();

        //START THE JANKFEST!

        VFXUtils.addGenericScreenEffect(player, -1, VFXUtils.GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS, Identifier.of("ponder", "ponder_lock"));
        VFXUtils.addGenericScreenEffect(player, -1, VFXUtils.GENERIC_SCREEN_EFFECT.NIGHT_VISION, Identifier.of("ponder", "ponder_bright"));

        BlockPos farPos = EntityUtils.getFarPos(player);
        this.floorHeight = player.getEntityWorld().getHeight() / 2;
        this.origin = new BlockPos(farPos.getX(), this.getFloorHeight() + 1, farPos.getZ());
        VFXUtils.setCameraPos(player, Vec3d.of(this.origin).add(5, 5, 5));
        VFXUtils.setCameraPitch(player, 45);
        VFXUtils.setCameraYaw(player, -45);

        this.worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of("ponder", "scene_" + this.uuid));
        this.world = new ServerWorld(Lib99j.getServerOrThrow(), Lib99j.getServerOrThrow().workerExecutor, Lib99j.getServerOrThrow().session, new LevelProperties(new LevelInfo("Ponder (Temporary world)", GameMode.CREATIVE, player.getEntityWorld().getLevelProperties().isHardcore(), player.getEntityWorld().getDifficulty(), true, player.getEntityWorld().getGameRules(), DataConfiguration.SAFE_MODE), new GeneratorOptions(0, false, false), LevelProperties.SpecialProperty.FLAT, Lifecycle.experimental()), worldKey, new DimensionOptions(player.getEntityWorld().getDimensionEntry(), new EmptyChunkGenerator(BiomeKeys.THE_VOID, 0)), false, 0, List.of(), true, new RandomSequencesState()) {
            @Override
            public void save(@Nullable ProgressListener progressListener, boolean flush, boolean savingDisabled) {
            }
        };

        Lib99j.getServerOrThrow().worlds.put(worldKey, world);

        this.packetRedirector = EntityUtils.fakeServerPlayerAddToWorld(world, BlockPos.ORIGIN, GameMode.CREATIVE, UUID.randomUUID(), "ponder_scene_", true);

        this.packetRedirector.setClientOptions(new SyncedClientOptions("ponder", 32, ChatVisibility.HIDDEN, false, 0, Arm.RIGHT, true, false, ParticlesMode.ALL));

        try {
//            LocalChannel channel = new LocalChannel();
//            ((LocalChannelAccessor) channel).lib99j$setState("CONNECTED");
//
//            channel.unsafe().register(new DefaultEventLoop(), new DefaultChannelPromise(channel));

//            EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter() {
//                @Override
//                public void channelRead(ChannelHandlerContext ctx, Object msg) {
//                    // swallow
//                    ReferenceCountUtil.release(msg);
//                }
//            });
//            channel.pipeline().fireChannelActive();
//
//            this.packetRedirector.networkHandler.connection.channel = channel;
        } catch (Exception e) {
            throw e;
            //e.printStackTrace();
        }

        //Lib99j.getServerOrThrow().getPlayerManager().players.add(this.packetRedirector);
        //Lib99j.getServerOrThrow().getPlayerManager().playerMap.put(this.packetRedirector.getUuid(), this.packetRedirector);

        ConnectedClientData connectedClientData = ConnectedClientData.createDefault(this.packetRedirector.getGameProfile(), false);

//        this.packetRedirector.networkHandler.connection.transitionOutbound(PlayStateFactories.S2C.bind(RegistryByteBuf.makeFactory(Lib99j.getServerOrThrow().getRegistryManager())));
//        this.packetRedirector.networkHandler.connection.transitionInbound(ConfigurationStates.C2S, new ServerConfigurationNetworkHandler(Lib99j.getServerOrThrow(), this.packetRedirector.networkHandler.connection, connectedClientData));

        ServerConfigurationNetworkHandler serverConfigurationNetworkHandler = new ServerConfigurationNetworkHandler(Lib99j.getServerOrThrow(), this.packetRedirector.networkHandler.connection, connectedClientData);

        //player.networkHandler.sendPacket(PlayerListS2CPacket.entryFromPlayer(List.of(this.packetRedirector)));

        //world.getChunkManager().chunkLoadingManager.playerChunkWatchingManager.add(this.packetRedirector, false);

        //world.addPlayer(this.packetRedirector);

        //player.networkHandler.sendPacket(PlayerPositionLookS2CPacket.of(-1, new EntityPosition(Vec3d.of(renderPos), Vec3d.ZERO, 0, 0), Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z)));

        //player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(player.createCommonPlayerSpawnInfo(serverWorld), PlayerRespawnS2CPacket.KEEP_ALL));

        for (int i = 0; i < builder.sizeX; i++) {
            for (int j = 0; j < builder.sizeZ; j++) {
                boolean checker = (i % 2 == 0) != (j % 2 == 1);
                world.setBlockState(getOrigin().add(new BlockPos(i, -1, j)), checker ? builder.state1 : builder.state2);
            }
        }

        //player.networkHandler.sendPacket(new CloseScreenS2CPacket(0));

        this.displayTopBar = new ServerBossBar(this.builder.title, BossBar.Color.YELLOW, BossBar.Style.NOTCHED_20);

        this.elementHolder = new ElementHolder();
        this.elementHolder.setAttachment(ChunkAttachment.ofTicking(this.elementHolder, this.getWorld(), Vec3d.ZERO));

        PonderManager.activeScenes.put(this.player, this);

        //Lib99j.getServerOrThrow().getPlayerManager().onPlayerConnect(this.packetRedirector.networkHandler.connection, this.packetRedirector, connectedClientData);


        //BEGIN RE-CREATION
        ServerPlayerEntity player1 = this.player;

        ClientConnection connection = new ClientConnection(NetworkSide.SERVERBOUND);

        //connection.setInitialPacketListener(new ServerHandshakeNetworkHandler(Lib99j.getServerOrThrow(), connection));

        this.packetRedirector.networkHandler = new ServerPlayNetworkHandler(Lib99j.getServerOrThrow(),
                connection,
                this.packetRedirector,
                ConnectedClientData.createDefault(this.packetRedirector.getGameProfile(), false)
        ) {

            @Override
            public boolean isConnectionOpen() {
                return true;
            }

            @Override
            public void sendPacket(Packet<?> packet) {
                Lib99j.LOGGER.info("testing: " + packet);
                PlayPacketUtils.PacketInfo info = PlayPacketUtils.getInfo(packet);
                if (info == null || !player1.networkHandler.isConnectionOpen() || !(info.hasTag(PlayPacketUtils.PacketTag.WORLD) && !info.hasTag(PlayPacketUtils.PacketTag.PLAYER_CLIENT))) {
                    return;
                }
                ;
                player1.networkHandler.sendPacket(new Lib99j.BypassPacket(packet));
            }
        };

        this.packetRedirector.networkHandler.onPlayerLoaded(new PlayerLoadedC2SPacket());

        world.onDimensionChanged(this.packetRedirector);

//        connection.transitionInbound(
//                PlayStateFactories.C2S.bind(RegistryByteBuf.makeFactory(Lib99j.getServerOrThrow().getRegistryManager()), this.packetRedirector.networkHandler), this.packetRedirector.networkHandler
//        );
//
//        this.packetRedirector.networkHandler.enableFlush();

        //FINISH

        this.packetRedirector.networkHandler.sendPacket(new ChunkRenderDistanceCenterS2CPacket(ChunkSectionPos.getSectionCoord(this.origin.getX()), ChunkSectionPos.getSectionCoord(this.origin.getZ())));

        this.packetRedirector.setPos(this.origin.getX(), this.origin.getY(), this.origin.getZ());
        this.world.getChunkManager().chunkLoadingManager.updatePosition(this.packetRedirector); //instant update the pos
        this.packetRedirector.networkHandler.sendPacket(BossBarS2CPacket.add(this.displayTopBar));
        ChunkSenderUtil.sendRegion(this.packetRedirector, new ChunkPos(ChunkSectionPos.getSectionCoord(this.origin.getX()) - 5, ChunkSectionPos.getSectionCoord(this.origin.getZ()) - 5), new ChunkPos(ChunkSectionPos.getSectionCoord(this.origin.getX()) + 5, ChunkSectionPos.getSectionCoord(this.origin.getZ()) + 5), this.world);

        //PolymerUtils.reloadWorld(this.packetRedirector);

        this.player.networkHandler.sendPacket(new TitleFadeS2CPacket(0, 0, 10));

        this.elementHolder.startWatching(player);
    }

    public ElementHolder getElementHolder() {
        return this.elementHolder;
    }

    public ServerWorld getWorld() {
        return world;
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public int getFloorHeight() {
        return this.floorHeight;
    }

    public void stopPondering() {
        this.isToBeRemoved = true;
        VFXUtils.removeGenericScreenEffect(this.player, Identifier.of("ponder", "ponder_lock"));
        VFXUtils.removeGenericScreenEffect(this.player, Identifier.of("ponder", "ponder_bright"));
        this.elementHolder.destroy();
        this.world.removePlayer(this.packetRedirector, Entity.RemovalReason.DISCARDED);
        this.player.networkHandler.sendPacket(new ChunkRenderDistanceCenterS2CPacket(ChunkSectionPos.getSectionCoord(this.player.getX()), ChunkSectionPos.getSectionCoord(this.player.getZ())));
        PolymerUtils.reloadWorld(this.player);
        this.player.networkHandler.sendPacket(BossBarS2CPacket.remove(this.displayTopBar.getUuid()));
        this.player.networkHandler.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYaw(), this.player.getPitch());

        try {
            Files.walkFileTree(Path.of(this.world.getChunkManager().chunkLoadingManager.getSaveDir()), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (Exception e) {
            Lib99j.LOGGER.error("Failed to remove save data for temporary level " + this.worldKey.getValue(), e);
        }
    }

    public boolean isToBeRemoved() {
        return this.isToBeRemoved;
    }

    private PonderStep getCurrentStep() {
        return steps.size() - 1 < currentStep ? null : steps.get(currentStep);
    }

    private void addInstruction(PonderInstruction instruction) {
        activeInstructions.add(instruction);
        instruction.start(this);
    }

    private PonderInstruction getCurrentInstruction() {
        return getCurrentStep().instructions().get(this.currentInstructionInStep);
    }

    public void tick() {
        if (this.isToBeRemoved()) return;

        this.activeInstructions.removeIf((instruction) -> instruction.isComplete(this));

        AtomicBoolean blockedContinue = new AtomicBoolean(false);
        this.activeInstructions.forEach((instruction) -> {
            instruction.tick(this);
            if (instruction.preventContinue(this)) blockedContinue.set(true);
        });

        if (!blockedContinue.get()) {
            var step = getCurrentStep();

            if (step == null) {
                player.sendMessage(Text.of("It done"));
                stopPondering();
                return;
            }

            if (currentInstructionInStep + 1 < step.instructions().size()) {
                // next instruction
                currentInstructionInStep++;
                addInstruction(getCurrentInstruction());
                player.sendMessage(Text.of("Normal run " + getCurrentInstruction()));
            } else {
                // NEXT STEP!
                currentStep++;
                currentInstructionInStep = 0;

                step = getCurrentStep();
                if (step == null) {
                    player.sendMessage(Text.of("It done"));
                    stopPondering();
                } else {
                    player.sendMessage(Text.of("Next step " + step.title()));
                    addInstruction(getCurrentInstruction());
                    player.sendMessage(Text.of("Next step run " + getCurrentInstruction()));
                }
            }
        }
    }
}
