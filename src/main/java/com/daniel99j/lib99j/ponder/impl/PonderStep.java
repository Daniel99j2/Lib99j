package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.ponder.impl.instruction.PonderInstruction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;

public record PonderStep(String name, ArrayList<PonderInstruction> instructions, int i) implements Cloneable {
    public PonderStep clone() {
        try {
            PonderStep clone = (PonderStep) super.clone();
            ArrayList<PonderInstruction> newInstructions = new ArrayList<>();
            this.instructions.forEach(i -> {
                newInstructions.add(i.clone());
            });
            return new PonderStep(name, newInstructions, i);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public MutableComponent getName() {
        return Component.translatable("ponder.scene.step."+name);
    }
}
