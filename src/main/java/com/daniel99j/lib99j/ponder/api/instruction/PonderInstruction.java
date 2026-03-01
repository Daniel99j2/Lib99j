package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.PonderScene;

public abstract class PonderInstruction implements Cloneable {
    public int time = 0;

    //if the scene is allowed to continue going
    public abstract boolean isComplete(PonderScene scene);

    public boolean preventContinue(PonderScene scene) {
        return true;
    };

    //when it is triggered
    public abstract void start(PonderScene scene);

    public void tick(PonderScene scene) {
        this.time++;
    };

    public PonderInstruction clone() {
        try {
            return (PonderInstruction) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    };
}
