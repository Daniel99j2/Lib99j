package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.PonderScene;
import com.daniel99j.lib99j.ponder.api.PonderTextDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

public class ShowTextInstruction extends PonderInstruction {
    private PonderTextDisplay display;
    private float displayTime;
    private List<Component> components;

    public ShowTextInstruction(float displayTime, List<Component> components) {
        this.components = new ArrayList<>();
        for (Component component : components) {
            this.components.add(component.copy());
        }
        this.displayTime = displayTime;
        this.display = new PonderTextDisplay((int) (displayTime * 20), new Vec2(0.5f, 0.5f), Vec2.ONE, this.components);
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
    public ShowTextInstruction clone() {
        ShowTextInstruction showTextInstruction = new ShowTextInstruction(this.displayTime, this.components);
        return showTextInstruction;
    }

    @Override
    public String toString() {
        return "ShowTextInstruction";
    }
}
