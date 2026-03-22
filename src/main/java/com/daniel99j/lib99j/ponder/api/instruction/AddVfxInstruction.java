package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.api.GenericScreenEffect;
import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.ponder.api.InstructionRemovalReason;
import com.daniel99j.lib99j.ponder.api.PonderScene;
import net.minecraft.resources.Identifier;

public class AddVfxInstruction extends PonderInstruction {
    private final GenericScreenEffect effect;
    //Custom effect timer so that it is ticked faster when FF'ing
    private final int effectTime;

    public AddVfxInstruction(GenericScreenEffect effect, float time) {
        this.effect = effect;
        this.effectTime = (int) (time * 20);
        if(effect == GenericScreenEffect.LOCK_CAMERA_AND_POS) throw new IllegalStateException("Invalid effect type");
    }


    @Override
    public boolean isComplete(PonderScene scene) {
        return this.time > effectTime;
    }

    @Override
    public boolean preventContinue(PonderScene scene) {
        return false;
    }

    @Override
    public void start(PonderScene scene) {
        VFXUtils.addGenericScreenEffect(scene.player, -1, effect, Identifier.fromNamespaceAndPath("ponder", "add_vfx_instruction_"+effect.toString().toLowerCase()));
    }

    @Override
    public void onRemove(PonderScene scene, InstructionRemovalReason reason) {
        VFXUtils.removeGenericScreenEffect(scene.player, Identifier.fromNamespaceAndPath("ponder", "add_vfx_instruction_"+effect.toString().toLowerCase()));
    }

    @Override
    public boolean canPersist() {
        return true;
    }

    @Override
    public AddVfxInstruction clone() {
        return (AddVfxInstruction) super.clone();
    }

    @Override
    public String toString() {
        return "AddVfxInstruction{effect="+effect+"}";
    }
}
