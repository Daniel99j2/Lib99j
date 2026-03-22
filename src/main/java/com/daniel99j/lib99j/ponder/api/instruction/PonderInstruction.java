package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.InstructionRemovalReason;
import com.daniel99j.lib99j.ponder.api.PonderScene;

public abstract class PonderInstruction implements Cloneable {
    public int time = 0;

    /**
     * If the instruction is complete and can be removed
     */
    public abstract boolean isComplete(PonderScene scene);

    /**
     * If the instruction should prevent other instructions from running concurrently
     */
    public boolean preventContinue(PonderScene scene) {
        return true;
    };

    /**
     * Called when the scene starts
     */
    public abstract void start(PonderScene scene);

    /**
     * Called when the instruction is removed
     */
    public void onRemove(PonderScene scene, InstructionRemovalReason reason) {

    };

    /**
     * Called when the instruction ticks
     */
    public void tick(PonderScene scene) {
        this.time++;
    };

    /**
     * The value of this instruction on the progress bar in an active scene
     */
    public int getValue(PonderScene scene) {
        return 0;
    }

    /**
     * The value of this instruction on the progress bar when complete
     */
    public int getMaxValue() {
        return 0;
    }

    /**
     * If the instruction can persist over multiple steps
     * <p>Mutually exclusive with: getMaxValue > 0, preventContinue != false</p>
     */
    public boolean canPersist() {
        return false;
    };

    public void validate() {
        if(this.canPersist() && (this.getMaxValue() > 0)) throw new IllegalStateException("canPersist == true is mutually exclusive with: getMaxValue > 0, preventContinue != false");
    }

    @Override
    public String toString() {
        return "PonderInstruction{type="+getClass().getName()+"}";
    }

    public PonderInstruction clone() {
        try {
            return (PonderInstruction) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    };
}
