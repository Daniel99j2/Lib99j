package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.PonderScene;
import net.minecraft.network.chat.MutableComponent;

public class ShowTextInstruction extends PonderInstruction {
    private final int displayTime;
    private MutableComponent component;

    public ShowTextInstruction(float displayTime, MutableComponent component) {
        this.displayTime = (int) (displayTime*20);
        this.component = component;
    }

    @Override
    public boolean isComplete(PonderScene scene) {
        return this.time > this.displayTime;
    }

    @Override
    public boolean preventContinue(PonderScene scene) {
        return false;
    }

    @Override
    public void start(PonderScene scene) {
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
    }

    @Override
    public ShowTextInstruction clone() {
        ShowTextInstruction clone = (ShowTextInstruction) super.clone();
        clone.component = component.copy();
        return clone;
    }

    @Override
    public String toString() {
        return "ShowTextInstruction{displayTime=" + displayTime + ", text=" + component.getString() + ", time=" + time + '}';
    }
}
