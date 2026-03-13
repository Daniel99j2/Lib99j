package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.PonderScene;

public abstract class InstantPonderInstruction extends PonderInstruction {
    @Override
    public boolean isComplete(PonderScene scene) {
        return true;
    }

    @Override
    public boolean preventContinue(PonderScene scene) {
        return false;
    }
}
