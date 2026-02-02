package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.*;
import com.daniel99j.lib99j.api.gui.DefaultGuiTextures;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.ponder.impl.PonderManager;
import com.daniel99j.lib99j.ponder.impl.PonderStep;
import com.daniel99j.lib99j.ponder.impl.instruction.PonderInstruction;
import com.mojang.serialization.Lifecycle;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.BossEvent;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PonderScene {
    public final ServerPlayer player;
    private final ServerLevel world;
    public final PonderBuilder builder;
    public final UUID uuid;
    private final ElementHolder elementHolder;
    private int currentStep = 0;
    private int currentInstructionInStep = 0;
    private final ArrayList<PonderStep> steps;
    private final ArrayList<PonderInstruction> activeInstructions = new ArrayList<>();
    public final ResourceKey<Level> worldKey;
    private final ServerPlayer packetRedirector;
    private final BlockPos origin;
    private boolean isToBeRemoved = false;
    private final ServerBossEvent displayTopBar;
    private Vec3 cameraPos = Vec3.ZERO;
    private Vec2 cameraRotation = new Vec2(45, -45);

    protected PonderScene(ServerPlayer player, PonderBuilder builder) {
        if (PonderManager.isPondering(player)) PonderManager.activeScenes.get(player).stopPondering();

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
            if(builder.y <= 2) player.sendSystemMessage(Component.literal("Warning: Ponder height is very low!").withColor(0xf5d442));
            if(builder.y <= 2) player.sendSystemMessage(Component.literal("Warning: This will cause issues in non-overworld dimensions!").withColor(0xf5d442));

            if(builder.y >= 250-builder.y) player.sendSystemMessage(Component.literal("Warning: Ponder height is very high!").withColor(0xf5d442));
            if(builder.y >= 250-builder.y) player.sendSystemMessage(Component.literal("Warning: This will cause issues in non-overworld dimensions!").withColor(0xf5d442));
        }

        this.player = player;
        this.player.connection.send(new ClientboundSetTitleTextPacket(Component.nullToEmpty("FADE EFFECT")));
        this.player.connection.send(new ClientboundSetSubtitleTextPacket(GuiUtils.colourText(DefaultGuiTextures.BIG_WHITE_SQUARE.text(), 0x000000)));
        this.player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 10000, 5));

        this.builder = builder;
        ArrayList<PonderStep> copiedSteps = new ArrayList<>();
        for (PonderStep step : builder.steps) {
            copiedSteps.add(step.clone());
        }
        this.steps = copiedSteps;

        this.uuid = UUID.randomUUID();

        //START THE JANKFEST!

        VFXUtils.addGenericScreenEffect(player, -1, VFXUtils.GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS, Identifier.fromNamespaceAndPath("ponder", "ponder_lock"));
        VFXUtils.addGenericScreenEffect(player, -1, VFXUtils.GENERIC_SCREEN_EFFECT.NIGHT_VISION, Identifier.fromNamespaceAndPath("ponder", "ponder_bright"));

        BlockPos farPos = EntityUtils.getFarPos(player);
        this.origin = new BlockPos(farPos.getX(), builder.y, farPos.getZ());
        this.cameraPos = Vec3.atCenterOf(this.origin).add(-builder.sizeX/4.0f, builder.sizeY/2.0f, -builder.sizeZ/4.0f);
        VFXUtils.setCameraPos(player, this.cameraPos);
        VFXUtils.setCameraPitch(player, this.cameraRotation.x);
        VFXUtils.setCameraYaw(player, this.cameraRotation.y);

        this.worldKey = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("ponder", "scene_" + this.uuid));
        this.world = new ServerLevel(Lib99j.getServerOrThrow(), Lib99j.getServerOrThrow().executor, Lib99j.getServerOrThrow().storageSource, new PrimaryLevelData(new LevelSettings("Ponder (Temporary world)", GameType.CREATIVE, player.level().getLevelData().isHardcore(), player.level().getDifficulty(), true, player.level().getGameRules(), WorldDataConfiguration.DEFAULT), new WorldOptions(0, false, false), PrimaryLevelData.SpecialWorldProperty.FLAT, Lifecycle.experimental()), worldKey, new LevelStem(player.level().dimensionTypeRegistration(), new EmptyChunkGenerator(Biomes.THE_VOID, 0)), false, 0, List.of(), true, new RandomSequences()) {
            @Override
            public boolean noSave() {
                return true;
            }
        };

        Lib99j.getServerOrThrow().levels.put(worldKey, world);

        this.packetRedirector = EntityUtils.fakeServerPlayerAddToWorld(world, BlockPos.ZERO, GameType.CREATIVE, UUID.randomUUID(), "ponder_scene_", true);

        this.packetRedirector.updateOptions(new ClientInformation("ponder", 32, ChatVisiblity.HIDDEN, false, 0, HumanoidArm.RIGHT, true, false, ParticleStatus.ALL));

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

        CommonListenerCookie connectedClientData = CommonListenerCookie.createInitial(this.packetRedirector.getGameProfile(), false);

//        this.packetRedirector.networkHandler.connection.transitionOutbound(PlayStateFactories.S2C.bind(RegistryByteBuf.makeFactory(Lib99j.getServerOrThrow().getRegistryManager())));
//        this.packetRedirector.networkHandler.connection.transitionInbound(ConfigurationStates.C2S, new ServerConfigurationNetworkHandler(Lib99j.getServerOrThrow(), this.packetRedirector.networkHandler.connection, connectedClientData));

        ServerConfigurationPacketListenerImpl serverConfigurationNetworkHandler = new ServerConfigurationPacketListenerImpl(Lib99j.getServerOrThrow(), this.packetRedirector.connection.connection, connectedClientData);

        //player.networkHandler.sendPacket(PlayerListS2CPacket.entryFromPlayer(List.of(this.packetRedirector)));

        //world.getChunkManager().chunkLoadingManager.playerChunkWatchingManager.add(this.packetRedirector, false);

        //world.addPlayer(this.packetRedirector);

        //player.networkHandler.sendPacket(PlayerPositionLookS2CPacket.of(-1, new EntityPosition(Vec3d.of(renderPos), Vec3d.ZERO, 0, 0), Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z)));

        //player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(player.createCommonPlayerSpawnInfo(serverWorld), PlayerRespawnS2CPacket.KEEP_ALL));

        for (int i = 0; i < builder.sizeX; i++) {
            for (int j = 0; j < builder.sizeZ; j++) {
                boolean checker = (i % 2 == 0) != (j % 2 == 1);
                world.setBlockAndUpdate(getOrigin().offset(new BlockPos(i, -1, j)), checker ? builder.state1 : builder.state2);
            }
        }

        //player.networkHandler.sendPacket(new CloseScreenS2CPacket(0));

        this.displayTopBar = new ServerBossEvent(this.builder.title, BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_20);

        this.elementHolder = new ElementHolder();
        this.elementHolder.setAttachment(ChunkAttachment.ofTicking(this.elementHolder, this.getWorld(), Vec3.ZERO));

        PonderManager.activeScenes.put(this.player, this);

        //Lib99j.getServerOrThrow().getPlayerManager().onPlayerConnect(this.packetRedirector.networkHandler.connection, this.packetRedirector, connectedClientData);


        //BEGIN RE-CREATION
        ServerPlayer player1 = this.player;

        Connection connection = new Connection(PacketFlow.SERVERBOUND);

        //connection.setInitialPacketListener(new ServerHandshakeNetworkHandler(Lib99j.getServerOrThrow(), connection));

        this.packetRedirector.connection = new ServerGamePacketListenerImpl(Lib99j.getServerOrThrow(),
                connection,
                this.packetRedirector,
                CommonListenerCookie.createInitial(this.packetRedirector.getGameProfile(), false)
        ) {

            @Override
            public boolean isAcceptingMessages() {
                return true;
            }

            @Override
            public void send(@NonNull Packet<?> packet) {
                PlayPacketUtils.PacketInfo info = PlayPacketUtils.getInfo(packet);
                if (info == null || !player1.connection.isAcceptingMessages() || !(info.hasTag(PlayPacketUtils.PacketTag.WORLD) && !info.hasTag(PlayPacketUtils.PacketTag.PLAYER_CLIENT))) {
                    return;
                }
                ;
                player1.connection.send(new Lib99j.BypassPacket(packet));
            }
        };

        this.packetRedirector.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());

        world.addDuringTeleport(this.packetRedirector);

//        connection.transitionInbound(
//                PlayStateFactories.C2S.bind(RegistryByteBuf.makeFactory(Lib99j.getServerOrThrow().getRegistryManager()), this.packetRedirector.networkHandler), this.packetRedirector.networkHandler
//        );
//
//        this.packetRedirector.networkHandler.enableFlush();

        //FINISH

        this.packetRedirector.connection.send(new ClientboundSetChunkCacheCenterPacket(SectionPos.blockToSectionCoord(this.origin.getX()), SectionPos.blockToSectionCoord(this.origin.getZ())));

        this.packetRedirector.setPosRaw(this.origin.getX(), this.origin.getY(), this.origin.getZ());
        this.world.getChunkSource().chunkMap.move(this.packetRedirector); //instant update the pos
        this.packetRedirector.connection.send(ClientboundBossEventPacket.createAddPacket(this.displayTopBar));
        ChunkSenderUtil.sendRegion(this.packetRedirector, new ChunkPos(SectionPos.blockToSectionCoord(this.origin.getX()) - 5, SectionPos.blockToSectionCoord(this.origin.getZ()) - 5), new ChunkPos(SectionPos.blockToSectionCoord(this.origin.getX()) + 5, SectionPos.blockToSectionCoord(this.origin.getZ()) + 5), this.world);

        //PolymerUtils.reloadWorld(this.packetRedirector);

        this.player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 0, 10));

        this.elementHolder.startWatching(player);
    }

    public ElementHolder getElementHolder() {
        return this.elementHolder;
    }

    public ServerLevel getWorld() {
        return world;
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public void stopPondering() {
        this.isToBeRemoved = true;
        VFXUtils.removeGenericScreenEffect(this.player, Identifier.fromNamespaceAndPath("ponder", "ponder_lock"));
        VFXUtils.removeGenericScreenEffect(this.player, Identifier.fromNamespaceAndPath("ponder", "ponder_bright"));
        this.elementHolder.destroy();
        this.world.removePlayerImmediately(this.packetRedirector, Entity.RemovalReason.DISCARDED);
        this.player.connection.send(new ClientboundSetChunkCacheCenterPacket(SectionPos.posToSectionCoord(this.player.getX()), SectionPos.posToSectionCoord(this.player.getZ())));
        PolymerUtils.reloadWorld(this.player);
        this.player.connection.send(ClientboundBossEventPacket.createRemovePacket(this.displayTopBar.getId()));
        this.player.connection.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());

        try {
            Files.walkFileTree(Lib99j.getServer().storageSource.getDimensionPath(worldKey), new SimpleFileVisitor<>() {
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
            Lib99j.LOGGER.error("Failed to remove save data for temporary level " + this.worldKey.identifier(), e);
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
                player.sendSystemMessage(Component.nullToEmpty("It done"));
                stopPondering();
                return;
            }

            if (currentInstructionInStep + 1 < step.instructions().size()) {
                // next instruction
                currentInstructionInStep++;
                addInstruction(getCurrentInstruction());
                player.sendSystemMessage(Component.nullToEmpty("Normal run " + getCurrentInstruction()));
            } else {
                // NEXT STEP!
                currentStep++;
                currentInstructionInStep = 0;

                step = getCurrentStep();
                if (step == null) {
                    player.sendSystemMessage(Component.nullToEmpty("It done"));
                    stopPondering();
                    return;
                } else {
                    player.sendSystemMessage(Component.nullToEmpty("Next step " + step.title()));
                    addInstruction(getCurrentInstruction());
                    player.sendSystemMessage(Component.nullToEmpty("Next step run " + getCurrentInstruction()));
                }
            }
        }

        VFXUtils.setCameraInterpolation(player, 10);
        VFXUtils.setCameraPos(player, this.cameraPos);
        VFXUtils.setCameraPitch(player, this.cameraRotation.x);
        VFXUtils.setCameraYaw(player, this.cameraRotation.y);
    }
}
