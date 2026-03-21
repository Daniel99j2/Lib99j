package com.daniel99j.lib99j.ponder.api.instruction;

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

    public PonderInstruction clone() {
        try {
            return (PonderInstruction) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    };
}
