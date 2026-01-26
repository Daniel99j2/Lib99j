package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.ponder.impl.PonderStep;
import com.daniel99j.lib99j.ponder.impl.instruction.PonderInstruction;
import com.daniel99j.lib99j.ponder.impl.instruction.WaitInstruction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.hud.debug.BiomeDebugHudEntry;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.ArrayList;

public class PonderBuilder {
    protected ArrayList<PonderStep> steps = new ArrayList<>();
    protected Text title = Text.of("Title not specified");
    protected int sizeX = 10;
    protected int sizeY = 10;
    protected int sizeZ = 10;
    protected RegistryKey<Biome> defaultBiome = BiomeKeys.PLAINS;
    protected BlockState state1 = Blocks.SNOW_BLOCK.getDefaultState();
    protected BlockState state2 = Blocks.GRAY_CONCRETE.getDefaultState();
    
    private boolean done = false;
    private ArrayList<PonderInstruction> currentStepInstructions = new ArrayList<>();

    private PonderBuilder() {}

    public static PonderBuilder create() {
        return new PonderBuilder();
    }

    public PonderBuilder instruction(PonderInstruction instruction) {
        if(this.done) throw new IllegalStateException("PonderBuilder has already been built");
        this.currentStepInstructions.add(instruction);
        if(instruction instanceof WaitInstruction) {
            Lib99j.LOGGER.warn("builder.wait() should be used instead of adding a WaitInstruction");
        }
        return this;
    }

    public PonderBuilder size(int sizeX, int sizeY, int sizeZ) {
        if(this.done) throw new IllegalStateException("PonderBuilder has already been built");
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        return this;
    }

    public PonderBuilder defaultBiome(RegistryKey<Biome> defaultBiome) {
        if(this.done) throw new IllegalStateException("PonderBuilder has already been built");
        this.defaultBiome = defaultBiome;
        return this;
    }

    public PonderBuilder title(String title) {
        if(this.done) throw new IllegalStateException("PonderBuilder has already been built");
        this.title = Text.literal(title);
        return this;
    }

    public PonderBuilder floorBlocks(BlockState state1, BlockState state2) {
        if(this.done) throw new IllegalStateException("PonderBuilder has already been built");
        this.state1 = state1;
        this.state2 = state2;
        return this;
    }

    public PonderBuilder title(Text title) {
        if(this.done) throw new IllegalStateException("PonderBuilder has already been built");
        this.title = title;
        return this;
    }

    public PonderBuilder wait(int time) {
        if(this.done) throw new IllegalStateException("PonderBuilder has already been built");
        this.currentStepInstructions.add(new WaitInstruction(time));
        return this;
    }

    public PonderBuilder finishStep(Text title, Text description) {
        if(this.done) throw new IllegalStateException("PonderBuilder has already been built");
        this.steps.add(new PonderStep(title, description, new ArrayList<>(this.currentStepInstructions)));
        this.currentStepInstructions.clear();
        return this;
    }
    
    public PonderBuilder build() {
        if(this.done) throw new IllegalStateException("PonderBuilder has already been built");
        if(!this.currentStepInstructions.isEmpty()) {
            throw new IllegalStateException("Run builder.finishStep() before builder.build()");
        }
        this.done = true;
        return this;
    }

    public PonderScene startPondering(ServerPlayerEntity player) {
        if(!this.done) throw new IllegalStateException("PonderBuilder has not been built");
        return new PonderScene(player, this);
    };
}
