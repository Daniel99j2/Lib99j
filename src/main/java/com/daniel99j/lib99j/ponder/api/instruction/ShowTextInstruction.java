package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.PonderScene;
import com.daniel99j.lib99j.ponder.api.PonderTextDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

public class ShowTextInstruction extends InstantPonderInstruction {
    private PonderTextDisplay display;
    private float displayTime;
    private Component component;

    public ShowTextInstruction(float displayTime, Component component) {
        this.component = component;
        this.displayTime = displayTime;
        this.display = new PonderTextDisplay((int) (displayTime * 20), new Vec2(0.5f, 0.5f), Vec2.ONE, this.component);
    }

    @Override
    public void start(PonderScene scene) {
        this.display.scene = scene;
        scene.getElementHolder().addElement(this.display);
    }

    @Override
    public ShowTextInstruction clone() {
        ShowTextInstruction showTextInstruction = new ShowTextInstruction(this.displayTime, this.component.copy());
        return showTextInstruction;
    }

    @Override
    public String toString() {
        return "ShowTextInstruction{component='"+this.component.getString() + "',displayTime="+this.displayTime+"}";
    }
}
