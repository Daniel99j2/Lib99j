package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.PonderItemDisplay;
import com.daniel99j.lib99j.ponder.api.PonderScene;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

public class ShowItemInstruction extends PonderInstruction {
    private PonderItemDisplay display;
    private List<ItemStack> items;

    public ShowItemInstruction(float displayTime, List<ItemStack> items) {
        this.items = items;
        this.display = new PonderItemDisplay((int) (displayTime * 20), new Vec2(0.5f, 0.6f), Vec2.ONE, this.items);
    }

    @Override
    public boolean isComplete(PonderScene scene) {
        return true;
    }

    @Override
    public boolean preventContinue(PonderScene scene) {
        return false;
    }

    @Override
    public void start(PonderScene scene) {
        this.display.scene = scene;
        scene.getElementHolder().addElement(this.display);
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
    }

    @Override
    public ShowItemInstruction clone() {
        ArrayList<ItemStack> newItems = new ArrayList<>();
        this.items.forEach((item) -> newItems.add(item.copy()));
        return new ShowItemInstruction(this.display.life, newItems);
    }

    @Override
    public String toString() {
        return "ShowItemInstruction";
    }
}
