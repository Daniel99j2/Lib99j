package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.ponder.api.instruction.PonderInstruction;

import java.util.ArrayList;

public record PonderStep(ArrayList<PonderInstruction> instructions, int i) implements Cloneable {
    public PonderStep clone() {
        try {
            PonderStep clone = (PonderStep) super.clone();
            ArrayList<PonderInstruction> newInstructions = new ArrayList<>();
            this.instructions.forEach(i -> {
                newInstructions.add(i.clone());
            });
            return new PonderStep(newInstructions, i);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
