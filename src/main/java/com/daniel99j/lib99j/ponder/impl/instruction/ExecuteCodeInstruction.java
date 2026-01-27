package com.daniel99j.lib99j.ponder.impl.instruction;

import com.daniel99j.lib99j.ponder.api.PonderScene;

import java.util.function.Consumer;

public class ExecuteCodeInstruction extends PonderInstruction {
    private final Consumer<PonderScene> onExecute;

    public ExecuteCodeInstruction(Consumer<PonderScene> onExecute) {
        this.onExecute = onExecute;
    }

    public boolean isComplete(PonderScene scene) {
        return true;
    };

    public void start(PonderScene scene) {
        this.onExecute.accept(scene);
    }

    public void cleanup(PonderScene scene) {

    }

    public void tick(PonderScene scene) {
        super.tick(scene);
    };

    @Override
    public WaitInstruction clone() {
        return (WaitInstruction) super.clone();
    }

    @Override
    public String toString() {
        return "ExecuteCodeInstruction{onExecute=*custom*, time=" + time + '}';
    }
}
