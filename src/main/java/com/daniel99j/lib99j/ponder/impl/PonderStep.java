package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.ponder.impl.instruction.PonderInstruction;
import net.minecraft.text.Text;

import java.util.ArrayList;

public record PonderStep(Text title, Text desc, ArrayList<PonderInstruction> instructions) implements Cloneable {
    public PonderStep clone() {
        try {
            PonderStep clone = (PonderStep) super.clone();
            return new PonderStep(clone.title, clone.desc, new ArrayList<>(this.instructions));
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
