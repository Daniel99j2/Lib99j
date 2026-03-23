package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.*;
import com.daniel99j.lib99j.api.gui.DefaultGuiTextures;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.impl.*;
import com.daniel99j.lib99j.ponder.api.instruction.PonderInstruction;
import com.daniel99j.lib99j.ponder.impl.*;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.sidebars.api.Sidebar;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.*;
import net.minecraft.advancements.criterion.PlayerTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.*;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    public final ServerPlayer packetRedirector;
    private final BlockPos origin;
    private boolean isToBeRemoved = false;
    private boolean isToBeStopped = false;
    private boolean stopWithoutVfx = false;
    private final ServerBossEvent titleTopBar;
    private final ServerBossEvent subtitleTopBar;
    public Vec3 cameraPos = Vec3.ZERO;
    public Vec2 cameraRotation = PonderStandards.DEFAULT_CAMERA_ROT;
    public int cameraInterpolateTime = 10;
    private int stepFFTo = -1;
    private boolean showingLoadingScreen;
    @ApiStatus.Internal
    public boolean canEscapeBounds = false;
    public PonderDevEdits ponderDevEdits = new PonderDevEdits();
    @ApiStatus.Internal
    public final PonderHotbarGui hotbarGui;
    private final Sidebar sidebarGui;
    private PonderSceneMode mode = PonderSceneMode.PLAYING;
    protected int inputCooldown = 0;
    protected int selectedStep = 0;
    private int tickSyncVerify = 0;
    @ApiStatus.Internal
    public Vec3 identifyPos = Vec3.ZERO;
    @ApiStatus.Internal
    public float identifyPitch = 0;
    @ApiStatus.Internal
    public float identifyYaw = 0;
    //this is not needed for fall damage, but it makes things like elytra rocketing before a ponder less punishing
    @ApiStatus.Internal
    public Vec3 initialVelocity = Vec3.ZERO;
    private int time;
    @ApiStatus.Internal
    public Runnable runOnceDone = null;

    /**
     * Options for developers who want finer control over their ponder scenes (eg, an intro to a minigame might be unskippable)
     */
    private CustomPonderProperties customProperties = new CustomPonderProperties(null, true, true, true, true, true, true, null);

    protected PonderScene(ServerPlayer player, PonderBuilder builder, PonderScene oldAfterStep, int goTo) {
        GameProperties.throwIfPonderNotEnabled("This code should never be reached");

        this.player = player;

        if ((goTo == -1) != (oldAfterStep == null)) throw new IllegalStateException("oldAfterStep is for fast-forward");
        if (oldAfterStep != null) {
            this.showingLoadingScreen = oldAfterStep.showingLoadingScreen;
            //Dont keep dev editing etc
            if(oldAfterStep.mode == PonderSceneMode.PAUSED) this.mode = PonderSceneMode.PAUSED;
            this.inputCooldown = oldAfterStep.inputCooldown;
            this.selectedStep = oldAfterStep.selectedStep;
            this.initialVelocity = oldAfterStep.initialVelocity;
        } else {
            this.initialVelocity = this.player.getDeltaMovement();
            //this.player.level.getEntitie
        }

        boolean checkTime = Lib99j.isDevelopmentEnvironment && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;

        double time = GLFW.glfwGetTime();
        if (PonderManager.isPondering(player)) PonderManager.activeScenes.get(player).stopPondering(true);

        if (Lib99j.isDevelopmentEnvironment) {
            if (builder.y <= 2)
                player.sendSystemMessage(Component.literal("Warning: Ponder height is very low!").withColor(0xf5d442));
            if (builder.y <= 2)
                player.sendSystemMessage(Component.literal("Warning: This will cause issues in some dimensions!").withColor(0xf5d442));

            if (builder.y >= 250 - builder.y)
                player.sendSystemMessage(Component.literal("Warning: Ponder height is very high!").withColor(0xf5d442));
            if (builder.y >= 250 - builder.y)
                player.sendSystemMessage(Component.literal("Warning: This will cause issues in some dimensions!").withColor(0xf5d442));
        }

        this.showLoadingScreen();

        this.hotbarGui = new PonderHotbarGui(this.player, this);
        this.hotbarGui.open();
        this.sidebarGui = new Sidebar(Sidebar.Priority.OVERRIDE);
        this.sidebarGui.show();
        this.sidebarGui.addPlayer(player);

        //forces advancement screen to have tabs, meaning the advancement button detection works
        AdvancementProgress progress = new AdvancementProgress();
        progress.update(AdvancementRequirements.allOf(Set.of("yes")));
        progress.grantProgress("yes");
        player.connection.send(new ClientboundUpdateAdvancementsPacket(false, Set.of(new AdvancementHolder(Identifier.fromNamespaceAndPath("lib99j", "ponder_advancement_forcer"), new Advancement(Optional.empty(), Optional.of(new DisplayInfo(DefaultGuiTextures.INVISIBLE.getItemStack(), Component.empty(), Component.empty(), Optional.of(new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("block/white_concrete"))), AdvancementType.TASK, false, false, false)), AdvancementRewards.EMPTY, Map.of("yes", PlayerTrigger.TriggerInstance.tick()), AdvancementRequirements.allOf(Set.of("yes")), false, Optional.of(Component.empty())))), Set.of(), Map.of(), true));
        player.connection.send(new ClientboundUpdateAdvancementsPacket(false, Set.of(), Set.of(), Map.of(Identifier.fromNamespaceAndPath("lib99j", "ponder_advancement_forcer"), progress), true));

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

        VFXUtils.addGenericScreenEffect(player, -1, GenericScreenEffect.LOCK_CAMERA_AND_POS, Identifier.fromNamespaceAndPath("ponder", "ponder_lock"));
        //VFXUtils.addGenericScreenEffect(player, -1, VFXUtils.GenericScreenEffect.NIGHT_VISION, Identifier.fromNamespaceAndPath("ponder", "ponder_bright"));

        //this.player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("lib99j.q_to_stop_pondering", Component.keybind("key.drop"))));

        BlockPos farPos = EntityUtils.getFarPos(player, player.level.getWorldBorder());
        this.origin = new BlockPos(farPos.getX(), builder.y, farPos.getZ());
        this.cameraPos = new Vec3(-builder.sizeX / 4.0f, builder.sizeY / 2.0f, -builder.sizeZ / 4.0f);
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

        int roof = player.level.getMaxY();

        for (int x = 0; x < builder.sizeX; x++) {
            for (int z = 0; z < builder.sizeZ; z++) {
                BlockPos pos = origin.offset(x, -1, z);
                LevelChunk chunk = this.level.getChunkAt(pos);
                //remove roof
                ((LevelChunkAccessor) chunk).lib99j$setBlockReallyUnsafeDoNotUse(new BlockPos(pos.getX(), roof, pos.getZ()), Blocks.AIR.defaultBlockState());
                boolean checker = (x % 2 == 0) != (z % 2 == 1);
                ((LevelChunkAccessor) chunk).lib99j$setBlockReallyUnsafeDoNotUse(pos, checker ? builder.state1 : builder.state2);

                this.level.setChunkForced(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), true);
            }
        }

        if (checkTime) {
            Lib99j.LOGGER.info("Stage {} took {}", "Floor", GLFW.glfwGetTime() - time);
            time = GLFW.glfwGetTime();
        }

        //player.connection.send(new ClientboundContainerClosePacket(-1));

        this.titleTopBar = new ServerBossEvent(Component.translatable("ponder.scene.currently_pondering_about").withColor(ChatFormatting.GRAY.getColor()), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
        this.subtitleTopBar = new ServerBossEvent(this.builder.title, BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

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
                if(info.hasTag(PlayPacketUtils.PacketTag.MANY_USES)) {
                    if(packet instanceof ClientboundGameEventPacket gameEventPacket) {
                        if(!(gameEventPacket.getEvent() == ClientboundGameEventPacket.START_RAINING || gameEventPacket.getEvent() == ClientboundGameEventPacket.STOP_RAINING || gameEventPacket.getEvent() == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE || gameEventPacket.getEvent() == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE)) {
                            return;
                        }
                    } else this.disconnect(Component.literal("Custom handler not setup for packet {}. Check logs and report to Daniel99j (https://modrinth.com/mod/lib99j)".replace("{}", packet.toString())));
                } else if (info.hasTag(PlayPacketUtils.PacketTag.PLAYER_CLIENT)) return;
                player1.connection.send(new BypassPacket(packet));

                //simulate the client accepting the server's chunks
                if (packet instanceof ClientboundChunkBatchFinishedPacket) {
                    //the float is the desired chunks/tick (64 is max)
                    this.handleChunkBatchReceived(new ServerboundChunkBatchReceivedPacket(64));
                }

                if (packet instanceof ClientboundKeepAlivePacket clientboundKeepAlivePacket) {
                    this.handleKeepAlive(new ServerboundKeepAlivePacket(clientboundKeepAlivePacket.getId()));
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

        this.elementHolder.startWatching(packetRedirector);

        this.level.syncRain();

        ItemDisplayElement background = new ItemDisplayElement(GuiUtils.colourItemData(DefaultGuiTextures.SOLID_COLOUR_BOX.asStack(), 0x111111));
        background.setOverridePos(origin.getBottomCenter());
        background.setScale(new Vector3f(-1000, -1000, -1000));
        background.setViewRange(100f);

        //TODO: Noxesium compat
        this.elementHolder.addElement(background);

        TextDisplayElement f5Hint = new TextDisplayElement(Component.translatable("ponder.scene.please_press_key", Component.keybind("key.togglePerspective"))) {
            @Override
            public Vec3 getCurrentPos() {
                BlockDisplayElement element = ((Lib99jPlayerUtilController) player).lib99j$getCamera();
                if(!getMode().hasInGameGui() || element == null) return new Vec3(0, -10000, 0);
                return element.getCurrentPos();
            }
        };

        f5Hint.setTranslation(new Vector3f(0, 0, 1f));
        f5Hint.setScale(new Vector3f(3, 3, 0));
        f5Hint.setBillboardMode(Display.BillboardConstraints.CENTER);
        f5Hint.setBackground(0xff000000);
        this.elementHolder.addElement(f5Hint);

        if (checkTime) {
            Lib99j.LOGGER.info("Stage {} took {}", "Finish", GLFW.glfwGetTime() - time);
            time = GLFW.glfwGetTime();
        }

        if (goTo != -1) this.fastForwardUntil(goTo);

        VFXUtils.clearParticles(this.player, Vec3.atCenterOf(this.getOrigin().below()));
    }

    public void tick() {
        if (this.stepFFTo != -1) tick(true);
        else tick(false);
    }

    public void tick(boolean forced) {
        if(this.isToBeRemoved) return;
        if (this.isToBeStopped) {
            this.stopPondering(!this.stopWithoutVfx);
            return;
        };
        if (this.isToBeRemoved()) return;
        if (this.player.hasDisconnected() || this.player.isRemoved()) {
            this.stopPondering(true);
            return;
        }
        //dont check paused here so inputs and disconnections still are checked
        if ((this.player.getLastClientInput().forward() || this.player.getLastClientInput().backward() || this.player.getLastClientInput().jump() || this.player.getLastClientInput().left() || this.player.getLastClientInput().right()) && this.mode.hasMovementControls()) {
            if (this.inputCooldown <= 0) {
                this.inputCooldown = 10; //put it here so new ponder scenes already have it
                if (this.player.getLastClientInput().backward() && this.customProperties.canSkipAndRewind) {
                    goTo(0);
                } else if (this.player.getLastClientInput().forward() && this.customProperties.canSkipAndRewind) {
                    goTo(this.selectedStep);
                } else if (this.player.getLastClientInput().jump() && this.customProperties.canPause) {
                    if(this.mode == PonderSceneMode.PAUSED) this.setMode(PonderSceneMode.PLAYING);
                    else if(this.mode == PonderSceneMode.PLAYING) this.setMode(PonderSceneMode.PAUSED);
                    this.inputCooldown = 1;
                } else if(builder.item != null && this.customProperties.canSkipAndRewind) {
                    int current = PonderManager.itemToBuilders.get(builder.item).indexOf(builder.id);
                    if (this.player.getLastClientInput().right() && current < PonderManager.itemToBuilders.get(builder.item).size()-1) {
                        this.stopPonderingSafely();
                        this.runOnceDone = () -> {
                            PonderManager.idToBuilder.get(PonderManager.itemToBuilders.get(builder.item).get(current+1)).startPondering(player);
                        };
                    } else if (this.player.getLastClientInput().left() && current > 0) {
                        this.stopPonderingSafely();
                        this.runOnceDone = () -> {
                            PonderManager.idToBuilder.get(PonderManager.itemToBuilders.get(builder.item).get(current-1)).startPondering(player);
                        };
                    }
                }
            }
        } else if (this.inputCooldown > 0) this.inputCooldown--;

        if (this.isToBeRemoved()) return;

        //update before too so the client always tick the entities properly
        updateTickStatus();

        this.packetRedirector.connection.tick();
        this.packetRedirector.tick();

        int ticksToRun = this.stepFFTo == -1 ? 1 : 500;
        while (ticksToRun-- > 0 && !this.isToBeStopped && !this.isToBeRemoved() && (!this.mode.isPaused() || forced)) {
            if (stepFFTo != -1 && this.currentStep >= stepFFTo) {
                stepFFTo = -1;
                VFXUtils.clearParticles(this.packetRedirector);
                VFXUtils.clearParticles(this.player);
                this.hideLoadingScreen();
                break; //dont go past the step!
            }

            time++;

            this.packetRedirector.connection.tick();
            this.packetRedirector.tick();
            this.level.runTick();

            AtomicBoolean blockedContinue = new AtomicBoolean(false);
            this.activeInstructions.forEach((instruction) -> {
                instruction.tick(this);
                if (instruction.preventContinue(this)) blockedContinue.set(true);
            });

            List<PonderInstruction> toRemove = new ArrayList<>();
            this.activeInstructions.forEach((instruction) -> {
                if(instruction.isComplete(this)) {
                    toRemove.add(instruction);
                    instruction.onRemove(this, InstructionRemovalReason.COMPLETED_INSTRUCTION);
                };
            });

            this.activeInstructions.removeAll(toRemove);

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

                    List<PonderInstruction> persistentInstructions = new ArrayList<>();

                    if(!this.activeInstructions.isEmpty() && Lib99j.isDevelopmentEnvironment) {
                        List<String> problems = new ArrayList<>();
                        for (PonderInstruction activeInstruction : this.activeInstructions) {
                            if(!activeInstruction.canPersist()) {
                                problems.add("Offending instruction: {}".replace("{}", activeInstruction.toString()));
                            } else {
                                persistentInstructions.add(activeInstruction);
                            }
                        }
                        if(!problems.isEmpty()) {
                            this.player.sendSystemMessage(Component.literal("Warning: Active instructions were present whilst a step finished! See logs for more info.").withStyle(ChatFormatting.YELLOW));
                            Lib99j.LOGGER.error("Active instructions were present whilst a step finished! Instructions left over are auto-cleared, try adding a wait before going to the next step.");
                            for (String problem : problems) {
                                Lib99j.LOGGER.error(problem);
                            }
                        }
                    };
                    this.activeInstructions.clear();

                    this.activeInstructions.addAll(persistentInstructions);

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
        VFXUtils.setCameraInterpolation(player, this.cameraInterpolateTime);
        VFXUtils.setCameraPos(player,  Vec3.atCenterOf(this.origin).add(cameraPos));
        VFXUtils.setCameraPitch(player, this.cameraRotation.x);
        VFXUtils.setCameraYaw(player, this.cameraRotation.y);
        this.packetRedirector.connection.send(new ClientboundSetTimePacket(this.level.getGameTime(), this.level.getDayTime(), false));

        this.player.connection.send(new ClientboundSetActionBarTextPacket(buildActionBar()));
        MutableComponent identify = PonderGuiTextures.IDENTIFY_BUTTON.text();
        if(this.mode == PonderSceneMode.IDENTIFYING) identify.append(PonderGuiTextures.IDENTIFY_BUTTON_SELECTED.text());
        this.sidebarGui.setTitle(identify.append(PonderGuiTextures.STEP_SELECT_BUTTON.text()).append(PonderGuiTextures.STEP_GOTO_BUTTON.text()).append(PonderGuiTextures.RESTART_BUTTON.text()));

        if(this.mode.isPaused()) {
            this.getElementHolder().tick();
        }

        updateTickStatus();
        updateTitleBar();
    }

    private void updateTitleBar() {
        if(!this.customProperties.showHud) {
            this.titleTopBar.setName(Component.empty());
            this.subtitleTopBar.setName(Component.empty());
        } else if(this.mode == PonderSceneMode.IDENTIFYING) {
            this.titleTopBar.setName(Component.translatable("ponder.scene.currently_looking_at").withColor(ChatFormatting.GRAY.getColor()));

            float max = 6;
            Vec3 vec3 = this.identifyPos;
            Vec3 vec32 =  this.packetRedirector.calculateViewVector(this.identifyYaw, this.identifyPitch);
            Vec3 vec33 = vec3.add(vec32.x * max, vec32.y * max, vec32.z * max);
            HitResult result = this.level.clip(new ClipContext(vec3, vec33, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, this.packetRedirector));

            Mannequin lookAngle = new Mannequin(EntityType.MANNEQUIN, this.level);
            lookAngle.setPos(this.identifyPos.add(0, this.player.getEyeHeight(), 0));
            lookAngle.setYRot(this.identifyYaw);
            lookAngle.setXRot(this.identifyPitch);
            lookAngle.setPose(this.player.getPose());

            AABB aABB = lookAngle.getBoundingBox().expandTowards(vec32.scale(max)).inflate(1.0, 1.0, 1.0);
            EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(lookAngle, vec3, vec33, aABB, EntitySelector.CAN_BE_PICKED, Mth.square(max));
            if (entityHitResult != null) {
                this.subtitleTopBar.setName((entityHitResult.getEntity()).getName());
            } else {
                if (result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult blockHitResult) {
                    BlockState state = this.level.getBlockState(blockHitResult.getBlockPos());
                    if(blockHitResult.getBlockPos().getY() == this.getOrigin().getY()-1 && (state == this.builder.state1 || state == this.builder.state2)) this.subtitleTopBar.setName(Component.empty());
                    else this.subtitleTopBar.setName(state.getBlock().getName());
                } else this.subtitleTopBar.setName(Component.empty());
            }

            float moveDistance = Math.max(this.builder.sizeX, Math.max(this.builder.sizeY, this.builder.sizeZ))*2+15;
            double distance = this.identifyPos.subtract(this.getBottomCenterPos()).length();
            if(distance > moveDistance*1.5) {
                this.player.connection.send(new BypassPacket(new ClientboundPlayerPositionPacket(-100, new PositionMoveRotation(this.getOrigin().above(1).getBottomCenter(), Vec3.ZERO, 0, 0), Set.of())));
            } else if(distance > moveDistance) {
                Vec3 target = this.getBottomCenterPos();
                Vec3 currentPos = this.identifyPos;
                Vec3 targetPos = new Vec3(target.x, target.y, target.z);
                Vec3 direction = targetPos.subtract(currentPos).normalize();
                Vec3 accelerationVector = direction.scale(distance-moveDistance);
                EntityUtils.sendVelocityDelta(this.player, accelerationVector);
            }
        } else {
            this.subtitleTopBar.setName(this.builder.title);
            this.titleTopBar.setName(Component.translatable("ponder.scene.currently_pondering_about").withColor(ChatFormatting.GRAY.getColor()));
        }
    }

    public Vec3 getBottomCenterPos() {
        return this.origin.getBottomCenter().add(this.builder.sizeX/2.0f, 0, this.builder.sizeZ/2.0f);
    }

    //Use this if you are for example, currently in a tick loop with entities to avoid ConcurrentModificationException
    public void stopPonderingSafely() {
        this.isToBeStopped = true;
    }

    public void stopPonderingSafelyWithoutVfx() {
        this.isToBeStopped = true;
        this.stopWithoutVfx = true;
    }

    public void stopPondering(boolean willNotContinuePondering) {
        if(isToBeRemoved) return;
        this.activeInstructions.forEach((instruction) -> {
            if(instruction.isComplete(this)) {
                instruction.onRemove(this, InstructionRemovalReason.SCENE_CLOSED);
            };
        });
        this.activeInstructions.clear();

        this.packetRedirector.disconnect();
        this.packetRedirector.discard();
        ((PlayerListAdder) this.packetRedirector).lib99j$setAddToPlayerList(false);

        EntityUtils.removeAllReferences(this.packetRedirector);
        //dont close anymore as it shouldn't actually change anything as it SHOULD be GC'd
        //this.level.close()

        this.isToBeRemoved = true;
        if (willNotContinuePondering) {
            //make the player properly reset
            if(!this.mode.locksCamera()) {
                ((Lib99jPlayerUtilController) this.player).lib99j$lockCamera();
            }

            VFXUtils.removeGenericScreenEffect(this.player, Identifier.fromNamespaceAndPath("ponder", "ponder_lock"));
            this.player.connection.send(new ClientboundSetChunkCacheCenterPacket(SectionPos.posToSectionCoord(this.player.getX()), SectionPos.posToSectionCoord(this.player.getZ())));
            PolymerUtils.reloadWorld(this.player);
            this.player.connection.send(new ClientboundSetActionBarTextPacket(Component.empty()));
            this.player.connection.teleport(new PositionMoveRotation(this.player.position(), initialVelocity, this.player.getYRot(), this.player.getXRot()), Set.of());
            this.player.connection.send(ClientboundTickingStatePacket.from(this.player.level.tickRateManager()));
            this.player.connection.send(new ClientboundSetTimePacket(this.player.level.getGameTime(), this.player.level.getDayTime(), ((ServerLevel) this.player.level).getGameRules().get(GameRules.ADVANCE_TIME)));

            player.connection.send(new ClientboundUpdateAdvancementsPacket(false, Set.of(), Set.of(Identifier.fromNamespaceAndPath("lib99j", "ponder_advancement_forcer")), Map.of(), false));

            if(this.mode == PonderSceneMode.IN_MENU) player.connection.send(new ClientboundContainerClosePacket(-1));

            this.showShortLoadingScreen();

            if (player.level.isRaining()) {
                player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F));
            } else {
                player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
            }

            player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, player.level.getRainLevel(1)));
            player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, player.level.getRainLevel(1)));

        }

        this.elementHolder.destroy();
        this.titleTopBar.removeAllPlayers();
        this.subtitleTopBar.removeAllPlayers();
        this.hotbarGui.close();
        this.sidebarGui.hide();

        //noinspection resource
        Lib99j.getServerOrThrow().levels.remove(levelKey);

        //dont just delete this world, also delete all other ponder worlds
        try {
            Files.walkFileTree(Lib99j.getServerOrThrow().storageSource.getDimensionPath(levelKey), new SimpleFileVisitor<>() {
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
            Lib99j.LOGGER.error("Failed to remove save data for temporary level {}", this.levelKey.identifier(), e);
        }

        VFXUtils.clearParticles(this.player);

        //just in case it might have stored it
        //noinspection DataFlowIssue
        this.packetRedirector.connection = null;
        //noinspection DataFlowIssue
        this.packetRedirector.gameMode.setLevel(null);
        //noinspection DataFlowIssue
        this.packetRedirector.level = null;

        if(this.runOnceDone != null) this.runOnceDone.run();
        if(willNotContinuePondering && this.runOnceDone == null && this.customProperties.onClose != null) this.customProperties.onClose.run();
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
        return Objects.requireNonNull(getCurrentStep()).instructions().get(this.currentInstructionInStep);
    }

    public MutableComponent buildActionBar() {
        if(!this.customProperties.showHud) return Component.empty();
        int scale = 2;

        int max = this.builder.totalValue;

        int completedInstructionsValue = 0;
        for (int i = 0; i < this.currentStep; i++) {
            completedInstructionsValue += this.steps.get(i).stepValue();
        }

        int currentInstructionValue = 0;

        int i = 0;
        for (PonderInstruction instruction : Objects.requireNonNull(this.getCurrentStep()).instructions()) {
            if(i > this.currentInstructionInStep) break;
            currentInstructionValue+=instruction.getValue(this);
            i++;
        }

        int current = completedInstructionsValue + currentInstructionValue;

        int pixels = (int) (PonderGuiTextures.FILLED_BAR_WIDTH*((float) current/max));

        MutableComponent out = PonderGuiTextures.STEP_BAR_BACKGROUND.text();

        GuiUtils.appendSpace(5, out);

        StringBuilder barFiller = new StringBuilder();

//        String space = GuiUtils.getSpace(scale).getString();
//        barFiller.append(space.repeat(fill2));
//
        Map<Integer, Integer> positions = new HashMap<>();
        for (PonderStep step : this.builder.steps) {
            int pos = (int)((PonderGuiTextures.FILLED_BAR_WIDTH) * ((float)step.totalValue()/max));
            positions.put(pos, step.i());
        }

        for (int j = 0; j < PonderGuiTextures.FILLED_BAR_WIDTH; j++) {
            if(j == 0 && this.selectedStep == 0 && this.builder.steps.size() > 1 && this.customProperties.canSkipAndRewind) {
                barFiller.append(PonderGuiTextures.STEP_BAR_STEP_SELECTED.string());
            } else {
                boolean selectedStep = positions.containsKey(j) && positions.get(j) == this.selectedStep - 1;
                barFiller.append(positions.containsKey(j) ? (selectedStep ? PonderGuiTextures.STEP_BAR_STEP_SELECTED.string() : PonderGuiTextures.STEP_BAR_STEP.string()) : (j <= pixels ? PonderGuiTextures.STEP_BAR_FILLER.string() : GuiUtils.getSpace(scale).getString()));
            }
        }

        out.append(Component.literal(barFiller.toString()).withStyle(GuiUtils.GUI_FONT_STYLE));

        GuiUtils.appendSpace(-5, out);

        //recenter
        GuiUtils.appendSpace((3)*scale, out);

        if(this.mode.isPaused()) out.append(PonderGuiTextures.RESUME_BUTTON.text());
        else out.append(PonderGuiTextures.PAUSE_BUTTON.text());

        if(builder.item != null) {
            if(PonderManager.itemToBuilders.get(builder.item).indexOf(builder.id) < PonderManager.itemToBuilders.get(builder.item).size()-1) out.append(PonderGuiTextures.RIGHT_BUTTON.text());
            if(PonderManager.itemToBuilders.get(builder.item).indexOf(builder.id) > 0) out.append(PonderGuiTextures.LEFT_BUTTON.text());
        }
        out.append(PonderGuiTextures.EXIT_BUTTON.text());
        out.append(PonderGuiTextures.MENU_BUTTON.text());

        out = GuiUtils.styleText(out, Style.EMPTY.withoutShadow(), false);

        return out;
    }

    public void fastForwardUntil(int step) {
        if (step <= this.currentStep) {
            if (this.stepFFTo != -1) {
                this.stepFFTo = -1;
                this.hideLoadingScreen();
            }
            return;
        }
        this.stepFFTo = step;
    }

    public void goTo(int step) {
        if (step <= this.currentStep) {
            this.showLoadingScreen();
            this.stopPondering(false);
            this.builder.startPonderingFromGoTo(this.player, this, step);
        } else {
            this.showLoadingScreen();
            fastForwardUntil(step);
        }
    }

    private void showLoadingScreen() {
        if (!this.showingLoadingScreen) {
            this.showingLoadingScreen = true;
            ArrayList<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
            packets.add(new ClientboundSetTitlesAnimationPacket(5, 10000, 0));
            packets.add(new ClientboundSetTitleTextPacket(GuiUtils.styleText(DefaultGuiTextures.BIG_WHITE_SQUARE.text(), Style.EMPTY.withColor(0x58638e), false)));
            packets.add(new ClientboundSetSubtitleTextPacket(Component.nullToEmpty("Loading...")));
            this.player.connection.send(new ClientboundBundlePacket(packets));
        }
    }

    private void showShortLoadingScreen() {
        if (!this.showingLoadingScreen) {
            ArrayList<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
            packets.add(new ClientboundSetTitlesAnimationPacket(4, 2, 4));
            packets.add(new ClientboundSetTitleTextPacket(GuiUtils.styleText(DefaultGuiTextures.BIG_WHITE_SQUARE.text(), Style.EMPTY.withColor(0x58638e), false)));
            packets.add(new ClientboundSetSubtitleTextPacket(Component.nullToEmpty("Loading...")));
            this.player.connection.send(new ClientboundBundlePacket(packets));
        }
    }

    private void hideLoadingScreen() {
        if (this.showingLoadingScreen) {
            this.showingLoadingScreen = false;
            this.player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 3, 5));
        }
    }

    public void addToSelectedStep(int selectedStep) {
        if(!this.getMode().hasMovementControls() || !this.customProperties.canSkipAndRewind) return;
        if (this.selectedStep + selectedStep < 0) this.selectedStep = this.steps.size() + selectedStep;
        else this.selectedStep = (this.selectedStep + selectedStep) % (this.steps.size());
    }

    protected void updateTickStatus() {
        int current = Objects.hash(this.mode, this.stepFFTo);
        if (this.tickSyncVerify == current) return;
        this.tickSyncVerify = current;

        if(this.packetRedirector.connection == null) return;

        if (this.mode.isPaused()) this.packetRedirector.connection.send(new ClientboundTickingStatePacket(20, true));
        else if (this.stepFFTo != -1) this.packetRedirector.connection.send(new ClientboundTickingStatePacket(1000, true));
        else this.packetRedirector.connection.send(new ClientboundTickingStatePacket(20, false));
    }

    public void setCanEscapeBounds(boolean canEscapeBounds) {
        this.canEscapeBounds = canEscapeBounds;
    }

    public boolean isPaused() {
        return this.mode.isPaused();
    }

    public void setMode(PonderSceneMode newMode) {
        PonderSceneMode oldMode = this.mode;
        this.mode = newMode;

        if(!oldMode.locksCamera() && newMode.locksCamera()) {
            this.showShortLoadingScreen();
            ((Lib99jPlayerUtilController) this.player).lib99j$lockCamera();
        }
        if(oldMode.locksCamera() && !newMode.locksCamera()) {
            this.showShortLoadingScreen();
            ((Lib99jPlayerUtilController) this.player).lib99j$unlockCamera();
            this.player.connection.send(new BypassPacket(new ClientboundPlayerPositionPacket(-100, new PositionMoveRotation(this.getOrigin().above(1).getBottomCenter(), Vec3.ZERO, 0, 0), Set.of())));
        }
        //if camera is unlocked then the gamemode resets!
        if(oldMode.getGameType() != newMode.getGameType() || oldMode.locksCamera() != newMode.locksCamera()) {
            this.player.connection.send(new BypassPacket(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, newMode.getGameType().getId())));
        }
        if(oldMode.locksHotbar() && !newMode.locksHotbar()) {
            this.hotbarGui.close();
        }
        if(!oldMode.locksHotbar() && newMode.locksHotbar()) {
            this.hotbarGui.open();
        }
    }

    public PonderSceneMode getMode() {
        return mode;
    }

    public void openMenu() {
        if(!this.customProperties.canUseMenu) return;
        if(!this.builder.registered) {
            if(Lib99j.isDevelopmentEnvironment) this.player.sendSystemMessage(Component.literal("The menu cannot be opened if the builder is not registered. Please disable opening the menu if this will be used in release builds through customProperties."));
            return;
        }
        this.setMode(PonderSceneMode.IN_MENU);
        PonderMenu.buildBaseMenu(this.player, this.builder.id);
    }

    public int getStep() {
        return currentStep;
    }

    public void closeMenu() {
        this.setMode(PonderSceneMode.PLAYING);
        this.player.connection.send(new ClientboundContainerClosePacket(-1));
    }

    public int getTime() {
        return time;
    }

    public void setCustomProperties(CustomPonderProperties customProperties) {
        this.customProperties = customProperties;
    }

    public CustomPonderProperties getCustomProperties() {
        return customProperties;
    }
}
