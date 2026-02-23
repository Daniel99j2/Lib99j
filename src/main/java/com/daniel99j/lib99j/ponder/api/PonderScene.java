    package com.daniel99j.lib99j.ponder.api;

    import com.daniel99j.lib99j.Lib99j;
    import com.daniel99j.lib99j.api.EmptyChunkGenerator;
    import com.daniel99j.lib99j.api.EntityUtils;
    import com.daniel99j.lib99j.api.PlayPacketUtils;
    import com.daniel99j.lib99j.api.VFXUtils;
    import com.daniel99j.lib99j.api.gui.DefaultGuiTextures;
    import com.daniel99j.lib99j.api.gui.GuiUtils;
    import com.daniel99j.lib99j.impl.BossBarVisibility;
    import com.daniel99j.lib99j.impl.PlayerListAdder;
    import com.daniel99j.lib99j.impl.mixin.PlayerListAccessor;
    import com.daniel99j.lib99j.ponder.impl.PonderManager;
    import com.daniel99j.lib99j.ponder.impl.PonderStep;
    import com.daniel99j.lib99j.ponder.impl.instruction.PonderInstruction;
    import com.daniel99j.lib99j.ponder.impl.instruction.WaitInstruction;
    import com.mojang.serialization.Lifecycle;
    import eu.pb4.polymer.core.api.utils.PolymerUtils;
    import eu.pb4.polymer.virtualentity.api.ElementHolder;
    import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
    import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
    import net.fabricmc.loader.api.FabricLoader;
    import net.minecraft.core.BlockPos;
    import net.minecraft.core.Holder;
    import net.minecraft.core.SectionPos;
    import net.minecraft.core.registries.Registries;
    import net.minecraft.network.Connection;
    import net.minecraft.network.chat.ClickEvent;
    import net.minecraft.network.chat.Component;
    import net.minecraft.network.chat.MutableComponent;
    import net.minecraft.network.chat.Style;
    import net.minecraft.network.protocol.Packet;
    import net.minecraft.network.protocol.PacketFlow;
    import net.minecraft.network.protocol.game.*;
    import net.minecraft.resources.Identifier;
    import net.minecraft.resources.ResourceKey;
    import net.minecraft.server.dialog.*;
    import net.minecraft.server.dialog.action.StaticAction;
    import net.minecraft.server.dialog.input.NumberRangeInput;
    import net.minecraft.server.level.*;
    import net.minecraft.server.network.CommonListenerCookie;
    import net.minecraft.server.network.ServerGamePacketListenerImpl;
    import net.minecraft.util.Brightness;
    import net.minecraft.world.BossEvent;
    import net.minecraft.world.RandomSequences;
    import net.minecraft.world.entity.Display;
    import net.minecraft.world.entity.HumanoidArm;
    import net.minecraft.world.entity.player.ChatVisiblity;
    import net.minecraft.world.level.GameType;
    import net.minecraft.world.level.Level;
    import net.minecraft.world.level.LevelSettings;
    import net.minecraft.world.level.WorldDataConfiguration;
    import net.minecraft.world.level.block.Block;
    import net.minecraft.world.level.block.Blocks;
    import net.minecraft.world.level.chunk.ChunkAccess;
    import net.minecraft.world.level.dimension.LevelStem;
    import net.minecraft.world.level.gamerules.GameRules;
    import net.minecraft.world.level.levelgen.WorldOptions;
    import net.minecraft.world.level.storage.PrimaryLevelData;
    import net.minecraft.world.phys.Vec2;
    import net.minecraft.world.phys.Vec3;
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
    import java.util.List;
    import java.util.Optional;
    import java.util.UUID;
    import java.util.concurrent.atomic.AtomicBoolean;
    import java.util.concurrent.atomic.AtomicInteger;
    import java.util.function.BooleanSupplier;

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
        private boolean isToBeStopped = false;
        private final ServerBossEvent titleTopBar;
        private final ServerBossEvent subtitleTopBar;
        private Vec3 cameraPos = Vec3.ZERO;
        private Vec2 cameraRotation = new Vec2(45, -45);
        private int stepFFTo = -1;

        protected int inputCooldown = 0;

        protected int selectedStep = 0;

        private boolean paused = false;

        private int tickSyncVerify = 0;

        protected PonderScene(ServerPlayer player, PonderBuilder builder) {
            double time = GLFW.glfwGetTime();
            if (PonderManager.isPondering(player)) PonderManager.activeScenes.get(player).stopPondering(true);

            if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
                if(builder.y <= 2) player.sendSystemMessage(Component.literal("Warning: Ponder height is very low!").withColor(0xf5d442));
                if(builder.y <= 2) player.sendSystemMessage(Component.literal("Warning: This will cause issues in some dimensions!").withColor(0xf5d442));

                if(builder.y >= 250-builder.y) player.sendSystemMessage(Component.literal("Warning: Ponder height is very high!").withColor(0xf5d442));
                if(builder.y >= 250-builder.y) player.sendSystemMessage(Component.literal("Warning: This will cause issues in some dimensions!").withColor(0xf5d442));
            }

            this.player = player;
            this.player.getInventory().setSelectedSlot(4);
            this.player.connection.send(new ClientboundSetHeldSlotPacket(4));
            this.player.connection.send(new ClientboundSetTitleTextPacket(Component.nullToEmpty("FADE EFFECT")));
            this.player.connection.send(new ClientboundSetSubtitleTextPacket(DefaultGuiTextures.BIG_WHITE_SQUARE.text().withColor(0x000000)));
            this.player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 10000, 0));

            this.builder = builder;
            ArrayList<PonderStep> copiedSteps = new ArrayList<>();
            for (PonderStep step : builder.steps) {
                copiedSteps.add(step.clone());
            }
            this.steps = copiedSteps;

            this.uuid = UUID.randomUUID();

            Lib99j.LOGGER.info("Stage {} took {}", "Steps", GLFW.glfwGetTime()-time); time = GLFW.glfwGetTime();

            //START THE JANKFEST!

            VFXUtils.addGenericScreenEffect(player, -1, VFXUtils.GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS, Identifier.fromNamespaceAndPath("ponder", "ponder_lock"));
            VFXUtils.addGenericScreenEffect(player, -1, VFXUtils.GENERIC_SCREEN_EFFECT.NIGHT_VISION, Identifier.fromNamespaceAndPath("ponder", "ponder_bright"));

            //this.player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("lib99j.q_to_stop_pondering", Component.keybind("key.drop"))));

            BlockPos farPos = EntityUtils.getFarPos(player);
            this.origin = new BlockPos(farPos.getX(), builder.y, farPos.getZ());
            this.cameraPos = Vec3.atCenterOf(this.origin).add(-builder.sizeX/4.0f, builder.sizeY/2.0f, -builder.sizeZ/4.0f);
            VFXUtils.setCameraPos(player, this.cameraPos);
            VFXUtils.setCameraPitch(player, this.cameraRotation.x);
            VFXUtils.setCameraYaw(player, this.cameraRotation.y);

            this.worldKey = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("ponder", "scene_" + this.uuid));
            this.world = new ServerLevel(Lib99j.getServerOrThrow(), Lib99j.getServerOrThrow().executor, Lib99j.getServerOrThrow().storageSource, new PrimaryLevelData(new LevelSettings("Ponder (Temporary world)", GameType.CREATIVE, player.level().getLevelData().isHardcore(), player.level().getDifficulty(), true, player.level().getGameRules(), WorldDataConfiguration.DEFAULT), new WorldOptions(0, false, false), PrimaryLevelData.SpecialWorldProperty.FLAT, Lifecycle.experimental()), worldKey, new LevelStem(player.level().dimensionTypeRegistration(), new EmptyChunkGenerator(builder.defaultBiome, 0)), false, 0, List.of(), true, new RandomSequences()) {
                @Override
                public boolean noSave() {
                    return true;
                }

                @Override // only tick when I want it to
                public void tick(BooleanSupplier booleanSupplier) {
                }

                @Override // an unused method that nobody cares about
                public void sendPacketToServer(Packet<?> packet) {
                    super.tick((() -> true));
                }
            };

            this.world.getGameRules().set(GameRules.ADVANCE_TIME, false, null);
            this.world.getGameRules().set(GameRules.ADVANCE_WEATHER, false, null);
            this.world.getGameRules().set(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS, false, null);
            this.world.getGameRules().set(GameRules.SPAWNER_BLOCKS_WORK, true, null);
            this.world.getGameRules().set(GameRules.SPAWN_MOBS, false, null);
            this.world.getGameRules().set(GameRules.SPECTATORS_GENERATE_CHUNKS, true, null);

            Lib99j.LOGGER.info("Stage {} took {}", "World create", GLFW.glfwGetTime()-time); time = GLFW.glfwGetTime();

            Lib99j.getServerOrThrow().levels.put(worldKey, world);

            this.packetRedirector = EntityUtils.fakeServerPlayerAddToWorld(world, BlockPos.ZERO, GameType.CREATIVE, UUID.randomUUID(), "ponder_scene_", true);

            this.packetRedirector.setGameMode(GameType.SURVIVAL);

            ((PlayerListAdder) this.packetRedirector).lib99j$setAddToPlayerList(true);

            this.packetRedirector.updateOptions(new ClientInformation("ponder", 32, ChatVisiblity.HIDDEN, false, 0, HumanoidArm.RIGHT, true, false, ParticleStatus.ALL));

            Lib99j.LOGGER.info("Stage {} took {}", "Player add", GLFW.glfwGetTime()-time); time = GLFW.glfwGetTime();

            int roof = player.level.getMaxY()-1;

            for (int x = -20; x < builder.sizeX + 20; x++) {
                for (int z = -20; z < builder.sizeZ + 20; z++) {
                    BlockPos pos = origin.offset(x, -1, z);
                    ChunkAccess chunk = world.getChunkAt(pos);
                    boolean inArea = x >= 0 && x < builder.sizeX && z >= 0 && z < builder.sizeZ;
                    if(!inArea && builder.roof) chunk.setBlockState(new BlockPos(pos.getX(), roof, pos.getZ()), Blocks.BARRIER.defaultBlockState(), Block.UPDATE_NONE);
                    if(inArea) {
                        boolean checker = (x % 2 == 0) != (z % 2 == 1);
                        chunk.setBlockState(pos, checker ? builder.state1 : builder.state2, Block.UPDATE_NONE);
                    }
                }
            }

            Lib99j.LOGGER.info("Stage {} took {}", "Floor", GLFW.glfwGetTime()-time); time = GLFW.glfwGetTime();

            player.connection.send(new ClientboundContainerClosePacket(-1));

            this.titleTopBar = new ServerBossEvent(this.builder.title, BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
            this.subtitleTopBar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

            ((BossBarVisibility) this.titleTopBar).lib99j$setVisible(false);
            ((BossBarVisibility) this.subtitleTopBar).lib99j$setVisible(false);

            this.elementHolder = new ElementHolder();
            this.elementHolder.setAttachment(ChunkAttachment.ofTicking(this.elementHolder, this.getWorld(), Vec3.ZERO));

            Lib99j.LOGGER.info("Stage {} took {}", "Elements + effects", GLFW.glfwGetTime()-time); time = GLFW.glfwGetTime();

            PonderManager.activeScenes.put(this.player, this);

            //BEGIN RE-CREATION
            ServerPlayer player1 = this.player;

            Connection connection = new Connection(PacketFlow.SERVERBOUND);

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
                    if (info == null || !player1.connection.isAcceptingMessages()) {
                        return;
                    }
                    ;
                    if (info.hasTag(PlayPacketUtils.PacketTag.PLAYER_CLIENT)) return;
                    player1.connection.send(new Lib99j.BypassPacket(packet));
                }
            };

            this.packetRedirector.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());

            world.addDuringTeleport(this.packetRedirector);

            this.packetRedirector.connection.send(new ClientboundSetChunkCacheCenterPacket(SectionPos.blockToSectionCoord(this.origin.getX()), SectionPos.blockToSectionCoord(this.origin.getZ())));

            this.packetRedirector.setPosRaw(this.origin.getX(), this.origin.getY(), this.origin.getZ());
            this.world.getChunkSource().chunkMap.move(this.packetRedirector); //instant update the pos
            this.titleTopBar.addPlayer(this.packetRedirector);
            this.subtitleTopBar.addPlayer(this.packetRedirector);

            PlayerListAccessor playerManagerAccessor = ((PlayerListAccessor) Lib99j.getServerOrThrow().getPlayerList());
            playerManagerAccessor.getPlayersByUUID().remove(this.packetRedirector.getUUID());
            playerManagerAccessor.getPlayers().remove(this.packetRedirector);


            Lib99j.LOGGER.info("Stage {} took {}", "Redirector init", GLFW.glfwGetTime()-time); time = GLFW.glfwGetTime();

            PolymerUtils.reloadWorld(this.packetRedirector);

            Lib99j.LOGGER.info("Stage {} took {}", "Region", GLFW.glfwGetTime()-time); time = GLFW.glfwGetTime();

            this.player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 0, 5));

            this.elementHolder.startWatching(player);

            ItemDisplayElement background = new ItemDisplayElement(GuiUtils.colourItemData(DefaultGuiTextures.SOLID_COLOUR_BOX.asStack(), 0x111111));
            background.setBrightness(new Brightness(0, 0));
            background.setOverridePos(origin.getBottomCenter());
            //background.setTranslation(new Vector3f(builder.sizeX/2-10, builder.sizeY/2-10, builder.sizeZ/2-10));
            background.setBillboardMode(Display.BillboardConstraints.CENTER);
            //background.setScale(new Vector3f(-builder.sizeX-20, -builder.sizeY-20, -builder.sizeZ-20));
            background.setScale(new Vector3f(-1000, -1000, -40));

            this.elementHolder.addElement(background);

            Lib99j.LOGGER.info("Stage {} took {}", "Finish", GLFW.glfwGetTime()-time); time = GLFW.glfwGetTime();
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

        //Use this if you are for example, currently in a tick loop with entities to avoid ConcurrentModificationException
        public void stopPonderingSafely() {
            this.isToBeStopped = true;
        }

        public void stopPondering(boolean stopVfx) {
            this.packetRedirector.discard();
            ((PlayerListAdder) this.packetRedirector).lib99j$setAddToPlayerList(false);

            this.isToBeRemoved = true;
            if(stopVfx) VFXUtils.removeGenericScreenEffect(this.player, Identifier.fromNamespaceAndPath("ponder", "ponder_lock"));
            if(stopVfx) VFXUtils.removeGenericScreenEffect(this.player, Identifier.fromNamespaceAndPath("ponder", "ponder_bright"));
            this.elementHolder.destroy();
            if(stopVfx) this.player.connection.send(new ClientboundSetChunkCacheCenterPacket(SectionPos.posToSectionCoord(this.player.getX()), SectionPos.posToSectionCoord(this.player.getZ())));
            if(stopVfx) PolymerUtils.reloadWorld(this.player);
            this.titleTopBar.removeAllPlayers();
            this.subtitleTopBar.removeAllPlayers();
            if(stopVfx) player.connection.send(new ClientboundSetActionBarTextPacket(Component.empty()));
            if(stopVfx) this.player.connection.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
            if(stopVfx) this.player.connection.send(ClientboundTickingStatePacket.from(this.player.level.tickRateManager()));

            Lib99j.getServerOrThrow().levels.remove(worldKey);

            try {
                Files.walkFileTree(Lib99j.getServerOrThrow().storageSource.getDimensionPath(worldKey), new SimpleFileVisitor<>() {
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
            if(this.stepFFTo != -1) tick(100);
            else tick(1);
        }

        public void tick(int tickToSimulate) {
            if(tickToSimulate <= 0) return;
            if(this.isToBeStopped) this.stopPondering(true);
            if(this.isToBeRemoved()) return;
            //dont check paused here so inputs and disconnections still are checked

            if(this.player.getInventory().getSelectedSlot() != 4) {
                this.setSelectedStep(this.selectedStep+this.player.getInventory().getSelectedSlot()-4);
                this.player.getInventory().setSelectedSlot(4);
                this.player.connection.send(new ClientboundSetHeldSlotPacket(4));
            }

            if (this.player.getLastClientInput().forward() || this.player.getLastClientInput().backward() || this.player.getLastClientInput().jump()) {
                if (this.inputCooldown <= 0) {
                    this.inputCooldown = 10; //also put it here so new ponder scenes already have it
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

            if(this.player.hasDisconnected() || this.player.isRemoved()) this.stopPondering(true);

            int ticksToRun = tickToSimulate;
            while (ticksToRun-- > 0 && !this.isToBeRemoved() && (!paused || this.stepFFTo != -1)) {
                if(stepFFTo == this.currentStep) {
                    stepFFTo = -1;
                    VFXUtils.clearParticles(this.packetRedirector);
                    break; //dont go past the step!
                }

                this.world.sendPacketToServer(null);

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
                        stopPondering(true);
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
                            stopPondering(true);
                            return;
                        } else {
                            player.sendSystemMessage(Component.nullToEmpty("Next step " + step.name()));
                            addInstruction(getCurrentInstruction());
                            player.sendSystemMessage(Component.nullToEmpty("Next step run " + getCurrentInstruction()));
                        }
                    }
                }
            }

            //no more epilepsy
            VFXUtils.setCameraInterpolation(player, 10);
            VFXUtils.setCameraPos(player, this.cameraPos);
            VFXUtils.setCameraPitch(player, this.cameraRotation.x);
            VFXUtils.setCameraYaw(player, this.cameraRotation.y);
            player.connection.send(new ClientboundSetTimePacket(this.player.level().getGameTime(), this.player.level().getDayTime(), true));

            player.connection.send(new ClientboundSetActionBarTextPacket(buildActionBar()));
            this.subtitleTopBar.setName(buildTopSubtitle());

            updateTickStatus();
        }

        private MutableComponent buildTopSubtitle() {
            MutableComponent out = Component.empty();
            boolean first = true;

            if(this.paused) {
                if(first) {
                    out.append(" ");
                    first = false;
                }
                out.append(Component.translatable("ponder.scene.paused").withColor(0xcccccc));
            }

            return out;
        }

        private MutableComponent buildActionBar() {
            MutableComponent out = Component.empty();

            for (int i = 0; i < this.steps.size(); i++) {
                PonderStep step = this.steps.get(i);

                String text = step.getName().getString();
                int colour = 0xffffff;
                int filled = 0;

                if (this.currentStep > i) {
                    colour = 0xcccccc;
                    filled = text.length();
                }
                else if (this.currentStep == i) {
                    colour = 0x00ff00;
                    float progress = (float) getCurrentScore() / getTotalScore();
                    filled = Math.round(text.length() * progress);
                }

                filled = Math.max(0, Math.min(filled, text.length()));

                if (i > 0) out.append(" ");

                out.append("[");

                MutableComponent start = Component.literal(text.substring(0, filled))
                        .withStyle(Style.EMPTY.withUnderlined(true).withColor(colour));

                MutableComponent end = Component.literal(text.substring(filled))
                        .withStyle(Style.EMPTY.withColor(0xffffff));

                out.append(start.withStyle(Style.EMPTY.withBold(i == this.selectedStep)));
                out.append(end.withStyle(Style.EMPTY.withBold(i == this.selectedStep)));
                out.append("]");
            }

            return out;
        }

        private int getTotalScore() {
            AtomicInteger out = new AtomicInteger();
            for (PonderInstruction instruction : this.getCurrentStep().instructions()) {
                if (instruction instanceof WaitInstruction wait) out.addAndGet(wait.waitTime);
                else out.addAndGet(1);
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
                    else
                        score += 1;
                }
                else if (i == currentInstructionInStep) {
                    // currently doing
                    if (instruction instanceof WaitInstruction wait)
                        score += wait.time;
                    else
                        score += 0; // if its doing a instruction that doesnt wait just do 0
                    break;
                }
            }

            return score;
        }

        public void fastForwardUntil(int step) {
            if(this.currentStep < step && stepFFTo == -1) {
                stepFFTo = step;
            }
        }

        public void goTo(int step) {
            if(step <= this.currentStep) {
                this.stopPondering(false);
                PonderScene scene = this.builder.startPondering(this.player);
                scene.fastForwardUntil(step);
                scene.inputCooldown = this.inputCooldown;
                scene.selectedStep = this.selectedStep;
                scene.paused = this.paused;
            } else if(step > this.currentStep) {
                fastForwardUntil(step);
            }
        }

        private void openControls() {
            paused = true;
            Input test = new Input("hej", new NumberRangeInput(512, Component.literal("hi"), "heyy", new NumberRangeInput.RangeInfo(1, 10, Optional.empty(), Optional.empty())));
            CommonDialogData dialogData = new CommonDialogData(Component.literal("hi"), Optional.empty(), false, false, DialogAction.CLOSE, List.of(), List.of(test));
            Dialog dialog = new MultiActionDialog(dialogData, List.of(new ActionButton(new CommonButtonData(Component.literal("boomba"), 512), Optional.empty())), Optional.of(new ActionButton(new CommonButtonData(Component.literal("okie"), 512), Optional.of(new StaticAction(new ClickEvent.RunCommand("/summon cow"))))), 1);
            player.openDialog(Holder.direct(dialog));
            //player.connection
        }

        public void setSelectedStep(int selectedStep) {
            if(this.selectedStep+selectedStep < 0) this.selectedStep = this.steps.size()+selectedStep;
            else this.selectedStep = selectedStep % (this.steps.size());
        }

        public void updateTickStatus() {
            int current = this.stepFFTo * (this.paused ? 5 : 1);
            if(this.tickSyncVerify == current) return;
            this.tickSyncVerify = current;

            if(this.paused) this.player.connection.send(new ClientboundTickingStatePacket(0, true));
            else if(this.stepFFTo != -1) this.player.connection.send(new ClientboundTickingStatePacket(10000, false));
            else this.player.connection.send(new ClientboundTickingStatePacket(20, false));
        }
    }
