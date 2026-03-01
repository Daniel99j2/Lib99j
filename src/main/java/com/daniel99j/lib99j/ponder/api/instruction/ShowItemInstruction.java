package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.PonderScene;
import net.minecraft.world.item.ItemStack;

public class ShowItemInstruction extends PonderInstruction {
    private final int displayTime;
    private ItemStack stack;

    public ShowItemInstruction(float displayTime, ItemStack stack) {
        this.displayTime = (int) (displayTime * 20);
        this.stack = stack;
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
    public ShowItemInstruction clone() {
        ShowItemInstruction clone = (ShowItemInstruction) super.clone();
        clone.stack = stack.copy();
        return clone;
    }

    @Override
    public String toString() {
        return "ShowItemInstruction{displayTime=" + displayTime + ", stack=" + stack.toString() + ", time=" + time + '}';
    }
}
