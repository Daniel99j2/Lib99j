package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2i;

public class ShowTextInstruction extends InstantPonderInstruction {
    private final float displayTime;
    private final Component component;
    private final PonderLine ponderLine;
    private final Vector2i pos;

    public ShowTextInstruction(float displayTime, Component component, Vector2i pos, PonderLine ponderLine) {
        this.component = component;
        this.displayTime = displayTime;
        this.ponderLine = ponderLine;
        this.pos = pos;
    }

    @Override
    public void start(PonderScene scene) {
        PonderTextDisplay text = new PonderTextDisplay((int) (displayTime * 20), new Vector2i(0, pos.y), Vec2.ONE, this.component, scene);

        int textX = 0;

        int lineX = 0;
        int lineWidth = 0;
        if(ponderLine.shouldShow() && ponderLine.lineSide() == PonderLine.LineSide.LEFT) {
            textX = ponderLine.textStartPos();
            lineX = (int) (textX+text.textUncentering.x);
            lineWidth = pos.x-lineX;
        } else if(ponderLine.shouldShow() && ponderLine.lineSide() == PonderLine.LineSide.RIGHT) {
            textX = (int) (PonderCoordUtil.SCREEN_SIZE.x-ponderLine.textStartPos()-text.textUncentering.x);

            lineWidth = textX-pos.x;
            lineX = pos.x;
        }

        text.setPos(new Vector2i(textX, pos.y));

        scene.getElementHolder().addElement(text);

        if(ponderLine.shouldShow()) scene.getElementHolder().addElement(new PonderLineDisplay((int) (displayTime * 20), new Vector2i(0, pos.y), Vec2.ONE, scene, lineX, lineWidth));
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public ShowTextInstruction clone() {
        return new ShowTextInstruction(this.displayTime, this.component.copy(), this.pos, this.ponderLine);
    }

    @Override
    public String toString() {
        return "ShowTextInstruction{component='"+this.component.getString() + "',displayTime="+this.displayTime+"}";
    }
}
