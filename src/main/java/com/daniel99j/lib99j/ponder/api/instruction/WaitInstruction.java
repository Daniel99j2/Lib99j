package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.PonderScene;

public class WaitInstruction extends PonderInstruction {
    public final int waitTime;

    public WaitInstruction(float waitTime) {
        this.waitTime = (int) (waitTime*20);
    }

    public boolean isComplete(PonderScene scene) {
        return this.time > this.waitTime;
    };

    public void start(PonderScene scene) {
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
        return "WaitInstruction{" +
                "waitTime=" + waitTime +
                ", time=" + time +
                '}';
    }
}
