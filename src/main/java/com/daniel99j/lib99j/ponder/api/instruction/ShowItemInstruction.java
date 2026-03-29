package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.PonderItemDisplay;
import com.daniel99j.lib99j.ponder.api.PonderLine;
import com.daniel99j.lib99j.ponder.api.PonderScene;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class ShowItemInstruction extends InstantPonderInstruction {
    private final List<ItemStackTemplate> items;
    private final float displayTime;
    private final PonderLine line;
    private final Vector2i pos;

    public ShowItemInstruction(float displayTime, List<ItemStackTemplate> items, Vector2i pos, PonderLine line) {
        this.items = items;
        this.displayTime = displayTime;
        this.line = line;
        this.pos = pos;
    }

    @Override
    public void start(PonderScene scene) {
        ArrayList<ItemStack> newItems = new ArrayList<>();
        for (ItemStackTemplate item : this.items) {
            newItems.add(item.create());
        }
        scene.getElementHolder().addElement(new PonderItemDisplay((int) (displayTime * 20), pos, Vec2.ONE, newItems, scene, line));
    }

    @Override
    public ShowItemInstruction clone() {
        ArrayList<ItemStackTemplate> newItems = new ArrayList<>();
        newItems.addAll(this.items);
        return new ShowItemInstruction(this.displayTime, newItems, this.pos, this.line);
    }

    @Override
    public String toString() {
        return "ShowItemInstruction{items=["+this.items.toString()+"],time="+this.displayTime+"}";
    }
}
