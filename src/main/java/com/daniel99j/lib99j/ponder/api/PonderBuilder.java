package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.GameProperties;
import com.daniel99j.lib99j.ponder.api.instruction.ExecuteCodeInstruction;
import com.daniel99j.lib99j.ponder.api.instruction.PonderInstruction;
import com.daniel99j.lib99j.ponder.api.instruction.WaitInstruction;
import com.daniel99j.lib99j.ponder.impl.PonderLevel;
import com.daniel99j.lib99j.ponder.impl.PonderStep;
import com.daniel99j.lib99j.testmod.TestingElements;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;

/**
 * The ponder builder
 * <p>See {@link TestingElements} for a simple example of how to make one</p>
 * <p>Whilst developing a ponder scene, it is recommended to use something like {@link PonderBuilder#hotswapExample()} to allow for hotswapping. Note that non-registered builders throw an exception when opening the menu</p>
 * <p>Extra info:</p>
 * <p>1. The scene is NOT at 0,0,0. Use {@link PonderScene#getOrigin()} to find the origin point. Some common methods like {@link PonderLevel#getBlockState(BlockPos)} or {@link PonderLevel#addFreshEntity(Entity)} auto-convert, so usage varies (see {@link PonderLevel} for method overrides)</p>
 * <p>2. Scenes create a new world every time that it starts</p>
 * <p>3. Replaying/going back steps creates a new scene, then fast-forwards to the point</p>
 * <p>4. In a scene there is a fake player called packetRedirector. This entity is added to the player list through mixins and receives then redirects to the real player all outgoing packets</p>
 * <p>5. DO NOT EVER store the packet redirector, scene world, or active scene outside of a method where it will be automatically de-referenced. This will cause memory leaks of 150mb+ PER SCENE!</p>
 * <p>6. As the ponder world is only temporary, feel free to edit things like gamerules, time, weather etc</p>
 * <p>7. When using translatable text in text display instructions, the result is translated on the server. Using nucleoid server translation api and {@link GameProperties#enableAddingAssetTranslationsToServer()} you can add translations like normal (to /assets/)</p>
 * <p>8. Most instruction times are in SECONDS not ticks</p>
 */
public class PonderBuilder {
    protected Identifier id = null;
    @ApiStatus.Internal
    public ItemStack icon = null;
    @ApiStatus.Internal
    public MutableComponent description;
    protected boolean registered = false;
    protected ArrayList<PonderStep> steps = new ArrayList<>();
    @ApiStatus.Internal
    public Component title;
    protected int sizeX = 7;
    protected int sizeY = 7;
    protected int sizeZ = 7;
    protected int y = 60;
    protected ResourceKey<Biome> defaultBiome = Biomes.PLAINS;
    protected BlockState state1 = Blocks.SNOW_BLOCK.defaultBlockState();
    protected BlockState state2 = Blocks.COAL_BLOCK.defaultBlockState();
    protected boolean roof = true;
    protected boolean hideFromCommands = false;
    protected Item item;

    protected int totalValue = 0;
    
    private boolean done = false;
    private final ArrayList<PonderInstruction> currentStepInstructions = new ArrayList<>();

    protected ArrayList<Identifier> groups = new ArrayList<>();

    private PonderBuilder(Identifier id, ItemStack icon, MutableComponent title, MutableComponent description) {
        this.id = id;
        this.icon = icon;
        this.title = title.copy();
        this.description = description.copy();
    }

    public static PonderBuilder create(Identifier id, ItemStack icon, MutableComponent title, MutableComponent description) {
        GameProperties.throwIfPonderNotEnabled("Ponder has not been enabled! Use GameProperties.enablePonder()");
        return new PonderBuilder(id, icon, title, description);
    }

    public PonderBuilder instruction(PonderInstruction instruction) {
        this.throwIfBuilt();
        this.currentStepInstructions.add(instruction);
        if(instruction instanceof WaitInstruction) {
            Lib99j.LOGGER.warn("builder.wait() should be used instead of adding a WaitInstruction");
        }
        return this;
    }

    public PonderBuilder size(int sizeX, int sizeY, int sizeZ) {
        this.throwIfBuilt();
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        return this;
    }

    public PonderBuilder defaultBiome(ResourceKey<Biome> defaultBiome) {
        this.throwIfBuilt();
        this.defaultBiome = defaultBiome;
        return this;
    }

    public PonderBuilder floorBlocks(BlockState state1, BlockState state2) {
        this.throwIfBuilt();
        this.state1 = state1;
        this.state2 = state2;
        return this;
    }

    public PonderBuilder yLevel(int level) {
        this.throwIfBuilt();
        this.y = level;
        return this;
    }

    public PonderBuilder roof(boolean roof) {
        this.throwIfBuilt();
        this.roof = roof;
        return this;
    }


    public PonderBuilder noRoof() {
        return roof(false);
    }

    public PonderBuilder hideFromCommands() {
        this.hideFromCommands = true;
        return this;
    }

    public PonderBuilder waitFor(float time) {
        this.throwIfBuilt();
        this.currentStepInstructions.add(new WaitInstruction(time));
        return this;
    }

    public PonderBuilder finishStep() {
        this.throwIfBuilt();
        int stepValue = 0;
        for (PonderInstruction currentStepInstruction : this.currentStepInstructions) {
            stepValue += currentStepInstruction.getMaxValue();
            currentStepInstruction.validate();
        }

        int totalValue = stepValue;
        for (PonderStep step : this.steps) {
            totalValue += step.stepValue();
        }
        this.steps.add(new PonderStep(new ArrayList<>(this.currentStepInstructions), steps.size(), stepValue, totalValue));
        this.currentStepInstructions.clear();
        return this;
    }
    
    public PonderBuilder build() {
        this.throwIfBuilt();
        if(!this.currentStepInstructions.isEmpty()) {
            throw new IllegalStateException("Run builder.finishStep() before builder.build()");
        }
        this.done = true;
        this.totalValue = this.steps.getLast().totalValue();;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public PonderScene startPondering(ServerPlayer player) {
        if(!this.registered) throw new IllegalStateException("You have not registered your ponder scene yet");
        return startPonderingIgnoreRegistration(player);
    };

    public PonderScene startPonderingIgnoreRegistration(ServerPlayer player) {
        return startPonderingFromGoTo(player, null, -1);
    };

    protected PonderScene startPonderingFromGoTo(ServerPlayer player, PonderScene from, int goTo) {
        if(!this.done) throw new IllegalStateException("PonderBuilder has not been built");
        PonderScene scene = new PonderScene(player, this, from, goTo);
        //ensure scene is inited
        if(from != null && from.isPaused() && goTo <= 0) {
            //scene.tick(true);
        };
        return scene;
    };
    
    private void throwIfBuilt() {
        if(this.done) throw new IllegalStateException("PonderBuilder has already been built");
    }

    public ArrayList<Identifier> getGroups() {
        if(!this.done) throw new IllegalStateException("PonderBuilder has not been built");
        if(!PonderManager.frozen) throw new IllegalStateException("Ponder groups have not been finalized");
        return groups;
    }

    public Identifier getId() {
        if(!this.done) throw new IllegalStateException("PonderBuilder has not been built");
        return id;
    }

    public BlockPos getSize() {
        return new BlockPos(sizeX, sizeY, sizeZ);
    }

    public boolean shouldHideFromCommands() {
        return hideFromCommands;
    }

    @ApiStatus.Internal
    public static void hotswapExample() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("mymod").then(Commands.literal("test-ponder")
                    .executes((context -> {
                        PonderScene hotswapScene = PonderBuilder.create(Identifier.fromNamespaceAndPath("mymod", "my_ponder"), Items.TNT.getDefaultInstance(), Component.translatable("ponder.scene.mymod.my_ponder"), Component.translatable("ponder.scene.mymod.my_ponder.description"))
                                .waitFor(2)
                                .instruction(new ExecuteCodeInstruction((scene -> {
                                    scene.getLevel().setBlockAndUpdate(new BlockPos(0, 0, 3), Blocks.COAL_ORE.defaultBlockState());
                                })))
                                .waitFor(2)
                                .instruction(new ExecuteCodeInstruction((scene -> {
                                    scene.getLevel().setBlockAndUpdate(new BlockPos(3, 0, 0), Blocks.GOLD_BLOCK.defaultBlockState());
                                })))
                                .finishStep()
                                .waitFor(6)
                                .finishStep()
                                .build().startPonderingIgnoreRegistration(context.getSource().getPlayerOrException());

                        hotswapScene.setCustomProperties(new CustomPonderProperties(null, false, true, false, true, true, false, () -> {
                            Lib99j.LOGGER.info("test");
                        }));
                        return 0;
                    }))));
        });
    }
}
