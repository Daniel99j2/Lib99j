package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.ponder.api.instruction.PonderInstruction;
import com.daniel99j.lib99j.ponder.api.instruction.WaitInstruction;
import com.daniel99j.lib99j.ponder.impl.PonderStep;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * The ponder builder
 * <p>See TestingElements for a simple example of how to make one</p>
 * <p>Things to note:</p>
 * <p>1. The scene is NOT at 0,0,0. Use scene.getOrigin to find the origin point. Some common methods like setBlock() or addFreshEntity() auto-convert, so usage varies (see PonderLevel for method overrides)</p>
 * <p>2. Scenes create a new world every time that it starts</p>
 * <p>3. Replaying/going back steps creates a new scene, then fast-forwards to the point</p>
 * <p>4. In a scene there is a fake player called packetRedirector. This entity is added to the player list through mixins and receives, then redirects to the real player, all outgoing packets</p>
 * <p>5. DO NOT EVER store the packet redirector, scene world, or active scene outside of a method where it will be automatically de-referenced</p>
 * <p>6. As the ponder world is only temporary, feel free to edit things like gamerules, time, weather etc</p>
 */
public class PonderBuilder {
    protected @Nullable String sourceNamespace = null;
    protected boolean registered = false;
    protected ArrayList<PonderStep> steps = new ArrayList<>();
    protected Component title = Component.literal("Title not specified");
    protected int sizeX = 10;
    protected int sizeY = 10;
    protected int sizeZ = 10;
    protected int y = 60;
    protected ResourceKey<Biome> defaultBiome = Biomes.PLAINS;
    protected BlockState state1 = Blocks.SNOW_BLOCK.defaultBlockState();
    protected BlockState state2 = Blocks.COAL_BLOCK.defaultBlockState();
    protected boolean roof = true;
    protected boolean hideFromCommands = false;
    
    private boolean done = false;
    private ArrayList<PonderInstruction> currentStepInstructions = new ArrayList<>();

    private PonderBuilder() {}

    public static PonderBuilder create() {
        return new PonderBuilder();
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

    public PonderBuilder title(String title) {
        this.throwIfBuilt();
        this.title = Component.literal(title);
        return this;
    }

    public PonderBuilder title(Component title) {
        this.throwIfBuilt();
        this.title = title;
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

    public PonderBuilder finishStep(String name) {
        this.throwIfBuilt();
        this.steps.add(new PonderStep(name, new ArrayList<>(this.currentStepInstructions), steps.size()));
        this.currentStepInstructions.clear();
        return this;
    }
    
    public PonderBuilder build() {
        this.throwIfBuilt();
        if(!this.currentStepInstructions.isEmpty()) {
            throw new IllegalStateException("Run builder.finishStep() before builder.build()");
        }
        this.done = true;
        return this;
    }

    public PonderScene startPondering(ServerPlayer player) {
        if(!this.registered) throw new IllegalStateException("You have not registered your ponder scene yet");
        return startPonderingIgnoreRegistration(player);
    };

    public PonderScene startPonderingIgnoreRegistration(ServerPlayer player) {
        return startPonderingFromGoTo(player, null, -1);
    };

    protected PonderScene startPonderingFromGoTo(ServerPlayer player, PonderScene from, int goTo) {
        if(!this.done) throw new IllegalStateException("PonderBuilder has not been built");
        return new PonderScene(player, this, from, goTo);
    };
    
    private void throwIfBuilt() {
        if(this.done) throw new IllegalStateException("PonderBuilder has already been built");
    }
}
