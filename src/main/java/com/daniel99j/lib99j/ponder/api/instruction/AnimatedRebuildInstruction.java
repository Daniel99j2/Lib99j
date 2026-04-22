package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.ponder.api.PonderScene;
import com.daniel99j.lib99j.ponder.impl.AnimatedRebuildChange;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class AnimatedRebuildInstruction extends PonderInstruction {
    private final int ticksPerBlock;
    private int currentBlockTicks;
    private int currentBlock;
    private ArrayList<AnimatedRebuildChange> changes;
    private final int estimatedBlocks;

    public AnimatedRebuildInstruction(int ticksPerBlock, int estimatedBlocks) {
        this.ticksPerBlock = ticksPerBlock;
        this.estimatedBlocks = estimatedBlocks;
    }

    @Override
    public AnimatedRebuildInstruction clone() {
        return new AnimatedRebuildInstruction(this.ticksPerBlock, this.estimatedBlocks);
    }

    @Override
    public boolean isComplete(PonderScene scene) {
        return changes.isEmpty();
    }

    @Override
    public int getMaxValue() {
        return this.estimatedBlocks*this.ticksPerBlock;
    }

    @Override
    public int getValue(PonderScene scene) {
        return this.currentBlock*this.ticksPerBlock;
    }

    @Override
    public void start(PonderScene scene) {
        changes = new ArrayList<>();
        changes.addAll(scene.animatedRebuildChanges);
        if(changes.size() != this.estimatedBlocks && Lib99j.isDevelopmentEnvironment) scene.player.sendSystemMessage(Component.literal("Actual block amount: "+changes.size()));
        scene.animatedRebuildChanges = null;
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        currentBlockTicks++;
        if(currentBlockTicks >= ticksPerBlock) {
            currentBlock++;
            currentBlockTicks = 0;
            if(changes.isEmpty()) {
                return;
            }
            AnimatedRebuildChange change = changes.getFirst();
            scene.getLevel().setBlock(change.blockPos(), change.blockState(), change.i(), change.j());
            changes.removeFirst();
        }
    }

    @Override
    public String toString() {
        return "AnimatedRebuildInstruction";
    }
}
