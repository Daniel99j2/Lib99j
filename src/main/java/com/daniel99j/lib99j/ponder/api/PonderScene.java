package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.EntityUtils;
import com.daniel99j.lib99j.api.GameProperties;
import com.daniel99j.lib99j.api.PlayPacketUtils;
import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.api.gui.DefaultGuiTextures;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.impl.BossBarVisibility;
import com.daniel99j.lib99j.impl.LevelChunkAccessor;
import com.daniel99j.lib99j.impl.PlayerListAdder;
import com.daniel99j.lib99j.ponder.api.instruction.PonderInstruction;
import com.daniel99j.lib99j.ponder.api.instruction.WaitInstruction;
import com.daniel99j.lib99j.ponder.impl.PonderDevEdits;
import com.daniel99j.lib99j.ponder.impl.PonderGuiTextures;
import com.daniel99j.lib99j.ponder.impl.PonderLevel;
import com.daniel99j.lib99j.ponder.impl.PonderStep;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Brightness;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * See {@link PonderBuilder} for more details
 */
public class PonderScene {
    public final ServerPlayer player;
    private final PonderLevel level;
    public final PonderBuilder builder;
    public final UUID uuid;
    private final ElementHolder elementHolder;
    private int currentStep = 0;
    private int currentInstructionInStep = 0;
    private final ArrayList<PonderStep> steps;
    private final ArrayList<PonderInstruction> activeInstructions = new ArrayList<>();
    public final ResourceKey<Level> levelKey;
    private final ServerPlayer packetRedirector;
    private final BlockPos origin;
    private boolean isToBeRemoved = false;
    private boolean isToBeStopped = false;
    private final ServerBossEvent titleTopBar;
    private final ServerBossEvent subtitleTopBar;
    private Vec3 cameraPos = Vec3.ZERO;
    private Vec2 cameraRotation = new Vec2(45, -45);
    private int stepFFTo = -1;
    private boolean showingLoadingScreen;
    @ApiStatus.Internal
    public boolean canBreakFloor = false;
    public PonderDevEdits ponderDevEdits = new PonderDevEdits();

    int test = 0;

    protected int inputCooldown = 0;

    protected int selectedStep = 0;

    private boolean paused = false;

    private int tickSyncVerify = 0;

    protected PonderScene(ServerPlayer player, PonderBuilder builder, PonderScene oldAfterStep, int goTo) {
        GameProperties.throwIfPonderNotEnabled("This code should never be reached");

        if ((goTo == -1) != (oldAfterStep == null)) throw new IllegalStateException("oldAfterStep is for fast-forward");
        if (oldAfterStep != null) {
            this.showingLoadingScreen = oldAfterStep.showingLoadingScreen;
            this.paused = oldAfterStep.paused;
            this.inputCooldown = oldAfterStep.inputCooldown;
            this.selectedStep = oldAfterStep.selectedStep;
        }

        boolean checkTime = FabricLoader.getInstance().isDevelopmentEnvironment() && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;

        double time = GLFW.glfwGetTime();
        if (PonderManager.isPondering(player)) PonderManager.activeScenes.get(player).stopPondering(true);

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            if (builder.y <= 2)
                player.sendSystemMessage(Component.literal("Warning: Ponder height is very low!").withColor(0xf5d442));
            if (builder.y <= 2)
                player.sendSystemMessage(Component.literal("Warning: This will cause issues in some dimensions!").withColor(0xf5d442));

            if (builder.y >= 250 - builder.y)
                player.sendSystemMessage(Component.literal("Warning: Ponder height is very high!").withColor(0xf5d442));
            if (builder.y >= 250 - builder.y)
                player.sendSystemMessage(Component.literal("Warning: This will cause issues in some dimensions!").withColor(0xf5d442));
        }

        this.player = player;
        this.player.getInventory().setSelectedSlot(4);
        this.player.connection.send(new ClientboundSetHeldSlotPacket(4));
        this.showLoadingScreen();

        this.builder = builder;
        ArrayList<PonderStep> copiedSteps = new ArrayList<>();
        for (PonderStep step : builder.steps) {
            copiedSteps.add(step.clone());
        }
        this.steps = copiedSteps;

        this.uuid = UUID.randomUUID();

        if (checkTime) {
            Lib99j.LOGGER.info("Stage {} took {}", "Steps", GLFW.glfwGetTime() - time);
            time = GLFW.glfwGetTime();
        }

        //START THE JANKFEST!

        VFXUtils.addGenericScreenEffect(player, -1, VFXUtils.GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS, Identifier.fromNamespaceAndPath("ponder", "ponder_lock"));
        //VFXUtils.addGenericScreenEffect(player, -1, VFXUtils.GENERIC_SCREEN_EFFECT.NIGHT_VISION, Identifier.fromNamespaceAndPath("ponder", "ponder_bright"));

        //this.player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("lib99j.q_to_stop_pondering", Component.keybind("key.drop"))));

        BlockPos farPos = EntityUtils.getFarPos(player, player.level.getWorldBorder());
        this.origin = new BlockPos(farPos.getX(), builder.y, farPos.getZ());
        this.cameraPos = Vec3.atCenterOf(this.origin).add(-builder.sizeX / 4.0f, builder.sizeY / 2.0f, -builder.sizeZ / 4.0f);
        VFXUtils.setCameraPos(player, this.cameraPos);
        VFXUtils.setCameraPitch(player, this.cameraRotation.x);
        VFXUtils.setCameraYaw(player, this.cameraRotation.y);

        this.levelKey = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("ponder", "scene_" + this.uuid));
        this.level = new PonderLevel(this, player.level(), this.levelKey, builder.defaultBiome);

        this.level.getGameRules().set(GameRules.ADVANCE_TIME, false, null);
        this.level.getGameRules().set(GameRules.ADVANCE_WEATHER, false, null);
        this.level.getGameRules().set(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS, false, null);
        this.level.getGameRules().set(GameRules.SPAWNER_BLOCKS_WORK, true, null);
        this.level.getGameRules().set(GameRules.SPAWN_MOBS, false, null);
        this.level.getGameRules().set(GameRules.SPECTATORS_GENERATE_CHUNKS, true, null);

        this.level.syncRain();

        if (checkTime) {
            Lib99j.LOGGER.info("Stage {} took {}", "World create", GLFW.glfwGetTime() - time);
            time = GLFW.glfwGetTime();
        }

        Lib99j.getServerOrThrow().levels.put(this.levelKey, this.level);

        this.packetRedirector = EntityUtils.fakeServerPlayer(this.level, BlockPos.ZERO, GameType.CREATIVE, UUID.randomUUID(), "ponder_scene_", true);

        this.packetRedirector.setGameMode(GameType.SURVIVAL);

        ((PlayerListAdder) this.packetRedirector).lib99j$setAddToPlayerList(true);

        this.packetRedirector.updateOptions(new ClientInformation("ponder", 32, ChatVisiblity.HIDDEN, false, 0, HumanoidArm.RIGHT, true, false, ParticleStatus.ALL));

        if (checkTime) {
            Lib99j.LOGGER.info("Stage {} took {}", "Player add", GLFW.glfwGetTime() - time);
            time = GLFW.glfwGetTime();
        }

        int roof = player.level.getMaxY() - 1;

        for (int x = 0; x < builder.sizeX; x++) {
            for (int z = 0; z < builder.sizeZ; z++) {
                BlockPos pos = origin.offset(x, -1, z);
                //remove roof
                ((LevelChunkAccessor) this.level.getChunkAt(new BlockPos(pos.getX(), roof, pos.getZ()))).lib99j$setBlockReallyUnsafeDoNotUse(new BlockPos(pos.getX(), roof, pos.getZ()), Blocks.AIR.defaultBlockState());
                boolean checker = (x % 2 == 0) != (z % 2 == 1);
                ((LevelChunkAccessor) this.level.getChunkAt(pos)).lib99j$setBlockReallyUnsafeDoNotUse(pos, checker ? builder.state1 : builder.state2);
            }
        }

        if (checkTime) {
            Lib99j.LOGGER.info("Stage {} took {}", "Floor", GLFW.glfwGetTime() - time);
            time = GLFW.glfwGetTime();
        }

        player.connection.send(new ClientboundContainerClosePacket(-1));

        this.titleTopBar = new ServerBossEvent(this.builder.title, BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
        this.subtitleTopBar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

        ((BossBarVisibility) this.titleTopBar).lib99j$setVisible(false);
        ((BossBarVisibility) this.subtitleTopBar).lib99j$setVisible(false);

        this.elementHolder = new ElementHolder();
        this.elementHolder.setAttachment(ChunkAttachment.ofTicking(this.elementHolder, this.getLevel(), this.origin));

        if (checkTime) {
            Lib99j.LOGGER.info("Stage {} took {}", "Elements + effects", GLFW.glfwGetTime() - time);
            time = GLFW.glfwGetTime();
        }

        PonderManager.activeScenes.put(this.player, this);

        //BEGIN RE-CREATION
        ServerPlayer player1 = this.player;

        Connection connection = new Connection(PacketFlow.SERVERBOUND);

        //note that the connection contains a reference to this due to player1!
        this.packetRedirector.connection = new ServerGamePacketListenerImpl(Lib99j.getServerOrThrow(),
                connection,
                this.packetRedirector,
                CommonListenerCookie.createInitial(this.packetRedirector.getGameProfile(), false)
        ) {
            //fix polyer VEs not being removed on disconnect due to disabling accepting messages
            @Override
            public boolean isAcceptingMessages() {
                return true;
            }

            @Override
            public void send(@NonNull Packet<?> packet) {
                PlayPacketUtils.PacketInfo info = PlayPacketUtils.getInfo(packet);
                if (info == null || !player1.connection.isAcceptingMessages()) {
                    return;
                }
                if (info.hasTag(PlayPacketUtils.PacketTag.PLAYER_CLIENT)) return;
                player1.connection.send(new Lib99j.BypassPacket(packet));

                //simulate the client accepting the server's chunks
                if (packet instanceof ClientboundChunkBatchFinishedPacket) {
                    //the float is the desired chunks/tick (64 is max)
                    this.handleChunkBatchReceived(new ServerboundChunkBatchReceivedPacket(64));
                }
            }
        };

        level.addNewPlayer(this.packetRedirector);

        this.packetRedirector.connection.send(new ClientboundSetChunkCacheCenterPacket(SectionPos.blockToSectionCoord(this.origin.getX()), SectionPos.blockToSectionCoord(this.origin.getZ())));

        this.packetRedirector.setPosRaw(this.origin.getX(), this.origin.getY(), this.origin.getZ());
        this.level.getChunkSource().chunkMap.move(this.packetRedirector); //instant update the pos
        this.titleTopBar.addPlayer(this.packetRedirector);
        this.subtitleTopBar.addPlayer(this.packetRedirector);

        if (checkTime) {
            Lib99j.LOGGER.info("Stage {} took {}", "Redirector init", GLFW.glfwGetTime() - time);
            time = GLFW.glfwGetTime();
        }

        if (checkTime) {
            Lib99j.LOGGER.info("Stage {} took {}", "Region", GLFW.glfwGetTime() - time);
            time = GLFW.glfwGetTime();
        }

        if (goTo <= 0) this.hideLoadingScreen(); //if were going to the first step then it wont ff

        this.elementHolder.startWatching(player);

        ItemDisplayElement background = new ItemDisplayElement(GuiUtils.colourItemData(DefaultGuiTextures.SOLID_COLOUR_BOX.asStack(), 0x111111));
        background.setBrightness(new Brightness(0, 0));
        background.setOverridePos(origin.getBottomCenter());
        //background.setTranslation(new Vector3f(builder.sizeX/2-10, builder.sizeY/2-10, builder.sizeZ/2-10));
        background.setBillboardMode(Display.BillboardConstraints.CENTER);
        //background.setScale(new Vector3f(-builder.sizeX-20, -builder.sizeY-20, -builder.sizeZ-20));
        background.setScale(new Vector3f(-1000, -1000, -40));

        this.elementHolder.addElement(background);

        if (checkTime) {
            Lib99j.LOGGER.info("Stage {} took {}", "Finish", GLFW.glfwGetTime() - time);
            time = GLFW.glfwGetTime();
        }

        if (goTo != -1) this.fastForwardUntil(goTo);
    }

    public void tick() {
        if (this.stepFFTo != -1) tick(100, true);
        else tick(1, false);
    }

    public void tick(int ticksToSimulate, boolean forced) {
        if (ticksToSimulate <= 0) return;
        if (this.isToBeStopped) this.stopPondering(true);
        if (this.isToBeRemoved()) return;
        if (this.player.hasDisconnected() || this.player.isRemoved()) {
            this.stopPondering(true);
            return;
        }
        ;
        //dont check paused here so inputs and disconnections still are checked

        if (this.player.getInventory().getSelectedSlot() != 4  && !this.ponderDevEdits.inBlocKEditMode) {
            if (this.player.getInventory().getSelectedSlot() > 4) this.addToSelectedStep(1);
            if (this.player.getInventory().getSelectedSlot() < 4) this.addToSelectedStep(-1);
            this.player.getInventory().setSelectedSlot(4);
            this.player.connection.send(new ClientboundSetHeldSlotPacket(4));
        }

        if ((this.player.getLastClientInput().forward() || this.player.getLastClientInput().backward() || this.player.getLastClientInput().jump()) && !this.ponderDevEdits.inBlocKEditMode) {
            if (this.inputCooldown <= 0) {
                this.inputCooldown = 10; //put it here so new ponder scenes already have it
                if (this.player.getLastClientInput().backward()) {
                    this.selectedStep = 0;
                    goTo(0);
                } else if (this.player.getLastClientInput().forward()) {
                    goTo(this.selectedStep);
                } else if (this.player.getLastClientInput().jump()) {
                    this.paused = !this.paused;
                    this.inputCooldown = 1;
                }
            }
        } else if (this.inputCooldown > 0) this.inputCooldown--;

        int ticksToRun = ticksToSimulate;
        while (ticksToRun-- > 0 && !this.isToBeRemoved() && (!paused || forced)) {
            if (stepFFTo == this.currentStep) {
                stepFFTo = -1;
                VFXUtils.clearParticles(this.packetRedirector);
                VFXUtils.clearParticles(this.player);
                this.hideLoadingScreen();
                break; //dont go past the step!
            }

            test++;

            this.level.runTick();

            this.activeInstructions.removeIf((instruction) -> instruction.isComplete(this));

            AtomicBoolean blockedContinue = new AtomicBoolean(false);
            this.activeInstructions.forEach((instruction) -> {
                instruction.tick(this);
                if (instruction.preventContinue(this)) blockedContinue.set(true);
            });

            if (!blockedContinue.get()) {
                var step = getCurrentStep();

                if (step == null) {
                    //player.sendSystemMessage(Component.literal("It done"));
                    stopPondering(true);
                    return;
                }

                if (currentInstructionInStep < step.instructions().size()) {
                    // next instruction
                    addInstruction(getCurrentInstruction());
                    currentInstructionInStep++;
                    //player.sendSystemMessage(Component.nullToEmpty("Normal run " + getCurrentInstruction()));
                } else {
                    // NEXT STEP!
                    currentStep++;
                    currentInstructionInStep = 0;

                    step = getCurrentStep();
                    if (step == null) {
                        //player.sendSystemMessage(Component.nullToEmpty("It done"));
                        stopPondering(true);
                        return;
                    } else {
                        //player.sendSystemMessage(Component.nullToEmpty("Next step " + step.name()));
                        addInstruction(getCurrentInstruction());
                        //player.sendSystemMessage(Component.nullToEmpty("Next step run " + getCurrentInstruction()));
                    }
                }
            }
        }

        if (this.isToBeRemoved()) return;
        //no more epilepsy
        VFXUtils.setCameraInterpolation(player, 10);
        VFXUtils.setCameraPos(player, this.cameraPos);
        VFXUtils.setCameraPitch(player, this.cameraRotation.x);
        VFXUtils.setCameraYaw(player, this.cameraRotation.y);
        this.packetRedirector.connection.send(new ClientboundSetTimePacket(this.level.getGameTime(), this.level.getDayTime(), false));

        this.player.connection.send(new ClientboundSetActionBarTextPacket(buildActionBar()));
        this.subtitleTopBar.setName(buildTopSubtitle());

        updateTickStatus();
    }

    //Use this if you are for example, currently in a tick loop with entities to avoid ConcurrentModificationException
    public void stopPonderingSafely() {
        this.isToBeStopped = true;
    }

    public void stopPondering(boolean stopVfx) {
        this.packetRedirector.disconnect();
        this.packetRedirector.discard();
        ((PlayerListAdder) this.packetRedirector).lib99j$setAddToPlayerList(false);

        EntityUtils.removeAllReferences(this.packetRedirector);
        //dont close anymore as it shouldn't actually change anything as it SHOULD be GC'd
        //this.level.close()

        this.isToBeRemoved = true;
        if (stopVfx) {
            VFXUtils.removeGenericScreenEffect(this.player, Identifier.fromNamespaceAndPath("ponder", "ponder_lock"));
            this.elementHolder.destroy();
            this.player.connection.send(new ClientboundSetChunkCacheCenterPacket(SectionPos.posToSectionCoord(this.player.getX()), SectionPos.posToSectionCoord(this.player.getZ())));
            PolymerUtils.reloadWorld(this.player);
            this.player.connection.send(new ClientboundSetActionBarTextPacket(Component.empty()));
            this.player.connection.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
            this.player.connection.send(ClientboundTickingStatePacket.from(this.player.level.tickRateManager()));
            this.player.connection.send(new ClientboundSetTimePacket(this.player.level.getGameTime(), this.player.level.getDayTime(), ((ServerLevel) this.player.level).getGameRules().get(GameRules.ADVANCE_TIME)));
        }

        this.titleTopBar.removeAllPlayers();
        this.subtitleTopBar.removeAllPlayers();

        Lib99j.getServerOrThrow().levels.remove(levelKey);

        try {
            Files.walkFileTree(Lib99j.getServerOrThrow().storageSource.getDimensionPath(levelKey), new SimpleFileVisitor<>() {
                @Override
                public @NotNull FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public @NotNull FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            Lib99j.LOGGER.error("Failed to remove save data for temporary level " + this.levelKey.identifier(), e);
        }

        VFXUtils.clearParticles(this.player);

        //just in case it might have stored it
        this.packetRedirector.connection = null;
        this.packetRedirector.gameMode.setLevel(null);
        this.packetRedirector.level = null;
    }

    public ElementHolder getElementHolder() {
        return this.elementHolder;
    }

    public PonderLevel getLevel() {
        return level;
    }

    public BlockPos getOrigin() {
        return this.origin;
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

    public MutableComponent buildTopSubtitle() {
        MutableComponent out = Component.empty();
        boolean notFirst = false;

        if (this.paused) {
            if (notFirst) {
                out.append(" ");
            }
            notFirst = true;
            out.append(Component.translatable("ponder.scene.paused").withColor(0xcccccc));
        }

        if (this.ponderDevEdits.inBlocKEditMode) {
            if (notFirst) {
                out.append(" ");
            }
            notFirst = true;
            out.append(Component.translatable("ponder.scene.dev_block_edit_mode").withColor(0xcccccc));
        }


        return out;
    }

    public MutableComponent buildActionBar() {
        int scale = 2;

        MutableComponent out = PonderGuiTextures.STEP_BAR_BACKGROUND.text();

        int max = 150;
        int current = Math.min(test, max);

        int pixels = (int) (PonderGuiTextures.FILLED_BAR_WIDTH*((float) current/max));

        String fillerPart = PonderGuiTextures.STEP_BAR_1.text().getString();

        GuiUtils.appendSpace(5, out);

        out.append(Component.literal(fillerPart.repeat(Math.max(0, pixels))).withStyle(Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("lib99j", "ui")))));

        GuiUtils.appendSpace(-5, out);

        //recenter
        GuiUtils.appendSpace((PonderGuiTextures.FILLED_BAR_WIDTH+3-pixels)*scale, out);

        if(this.paused) out.append(PonderGuiTextures.RESUME_BUTTON.text());
        else out.append(PonderGuiTextures.PAUSE_BUTTON.text());

        out.append(PonderGuiTextures.RIGHT_BUTTON.text());
        out.append(PonderGuiTextures.LEFT_BUTTON.text());
        out.append(PonderGuiTextures.EXIT_BUTTON.text());
        out.append(PonderGuiTextures.MENU_BUTTON.text());

        out = GuiUtils.styleText(out, Style.EMPTY.withoutShadow(), false);

        return out;
    }

    private int getTotalScore() {
        AtomicInteger out = new AtomicInteger();
        for (PonderInstruction instruction : this.getCurrentStep().instructions()) {
            if (instruction instanceof WaitInstruction wait) out.addAndGet(wait.waitTime);
            else if (!instruction.isComplete(this) && instruction.preventContinue(this)) out.addAndGet(1);
        }
        return out.get();
    }

    private int getCurrentScore() {
        PonderStep step = getCurrentStep();
        if (step == null) return 0;

        int score = 0;

        for (int i = 0; i < step.instructions().size(); i++) {
            PonderInstruction instruction = step.instructions().get(i);

            if (i < currentInstructionInStep) {
                // complete
                if (instruction instanceof WaitInstruction wait)
                    score += wait.waitTime;
                else if (!instruction.isComplete(this) && instruction.preventContinue(this))
                    score += 1;
            } else if (i == currentInstructionInStep) {
                // currently doing
                if (instruction.preventContinue(this))
                    score += instruction.time;
                // if its doing a instruction that doesnt wait just do 0
                break;
            }
        }

        return score;
    }

    public void fastForwardUntil(int step) {
        if (this.currentStep < step && stepFFTo == -1) {
            stepFFTo = step;
        }
    }

    public void goTo(int step) {
        if (step <= this.currentStep) {
            this.showLoadingScreen();
            this.stopPondering(false);
            this.builder.startPonderingFromGoTo(this.player, this, step);
        } else if (step > this.currentStep) {
            this.showLoadingScreen();
            fastForwardUntil(step);
        }
    }

    private void showLoadingScreen() {
        if (!this.showingLoadingScreen) {
            this.showingLoadingScreen = true;
            this.player.connection.send(new ClientboundSetSubtitleTextPacket(Component.nullToEmpty("Loading...")));
            this.player.connection.send(new ClientboundSetTitleTextPacket(GuiUtils.styleText(DefaultGuiTextures.BIG_WHITE_SQUARE.text(), Style.EMPTY.withColor(0x58638e), false)));
            this.player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 10000, 0));
        }
    }

    private void hideLoadingScreen() {
        if (this.showingLoadingScreen) {
            this.showingLoadingScreen = false;
            this.player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 1, 5));
        }
    }

    private void addToSelectedStep(int selectedStep) {
        if (this.selectedStep + selectedStep < 0) this.selectedStep = this.steps.size() + selectedStep;
        else this.selectedStep = (this.selectedStep + selectedStep) % (this.steps.size());
    }

    private void updateTickStatus() {
        int current = this.stepFFTo * (this.paused ? 5 : 1);
        if (this.tickSyncVerify == current) return;
        this.tickSyncVerify = current;

        if (this.paused) this.player.connection.send(new ClientboundTickingStatePacket(0, true));
        else if (this.stepFFTo != -1) this.player.connection.send(new ClientboundTickingStatePacket(10000, false));
        else this.player.connection.send(new ClientboundTickingStatePacket(20, false));
    }

    public void setCanBreakFloor(boolean canBreakFloor) {
        this.canBreakFloor = canBreakFloor;
    }

    public void pause() {
        this.paused = true;
    }

    public void unpause() {
        this.paused = false;
    }

    public boolean isPaused() {
        return this.paused;
    }
}
