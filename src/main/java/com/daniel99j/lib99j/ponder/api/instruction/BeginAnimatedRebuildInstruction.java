package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.PonderScene;

import java.util.ArrayList;

public class BeginAnimatedRebuildInstruction extends InstantPonderInstruction {
    public BeginAnimatedRebuildInstruction() {

    }

    @Override
    public void start(PonderScene scene) {
        scene.animatedRebuildChanges = new ArrayList<>();
    }

    @Override
    public BeginAnimatedRebuildInstruction clone() {
        return (BeginAnimatedRebuildInstruction) super.clone();
    }

    @Override
    public String toString() {
        return "BeginAnimatedRebuildInstruction";
    }
}
