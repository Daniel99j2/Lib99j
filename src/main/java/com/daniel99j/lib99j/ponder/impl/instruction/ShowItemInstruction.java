package com.daniel99j.lib99j.ponder.impl.instruction;

import com.daniel99j.lib99j.ponder.api.PonderScene;
import net.minecraft.world.item.ItemStack;

public class ShowItemInstruction extends PonderInstruction {
    private final int displayTime;
    private ItemStack stack;

    public ShowItemInstruction(int displayTime, ItemStack stack) {
        this.displayTime = displayTime;
        this.stack = stack;
    }

    public boolean isComplete(PonderScene scene) {
        return this.time > this.displayTime;
    };

    public boolean preventContinue(PonderScene scene) {
        return false;
    };

    public void start(PonderScene scene) {
    }

    public void cleanup(PonderScene scene) {

    }

    public void tick(PonderScene scene) {
        super.tick(scene);
    };

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
