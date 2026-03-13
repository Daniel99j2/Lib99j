package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.PonderScene;

import java.util.function.Consumer;

public class ExecuteCodeInstruction extends InstantPonderInstruction {
    private final Consumer<PonderScene> onExecute;

    public ExecuteCodeInstruction(Consumer<PonderScene> onExecute) {
        this.onExecute = onExecute;
    }

    @Override
    public void start(PonderScene scene) {
        this.onExecute.accept(scene);
    }

    @Override
    public ExecuteCodeInstruction clone() {
        return (ExecuteCodeInstruction) super.clone();
    }

    @Override
    public String toString() {
        return "ExecuteCodeInstruction{onExecute=*custom*, time=" + time + '}';
    }
}
