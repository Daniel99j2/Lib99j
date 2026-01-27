package com.daniel99j.lib99j.ponder.impl.instruction;

import com.daniel99j.lib99j.api.gui.DefaultGuiTextures;
import com.daniel99j.lib99j.ponder.api.PonderScene;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ShowLineInstruction extends PonderInstruction {
    private final int colour;
    private final Vec2 start;
    private final Vec2 end;
    private final int displayTime;
    private final float thickness;

    private ItemDisplayElement itemDisplayElement;

    public ShowLineInstruction(int displayTime, int colour, Vec2 start, Vec2 end, float thickness) {
        this.colour = colour;
        this.start = start;
        this.end = end;
        this.displayTime = displayTime;
        this.thickness = thickness;
    }

    public boolean isComplete(PonderScene scene) {
        return this.time > this.displayTime;
    };

    public boolean preventContinue(PonderScene scene) {
        return false;
    };

    public void start(PonderScene scene) {
        this.itemDisplayElement = new ItemDisplayElement(DefaultGuiTextures.SOLID_COLOUR.asStack());
        this.itemDisplayElement.setInitialPosition(Vec3.atLowerCornerOf(scene.getOrigin()));
        float distance = (float) this.end.add(this.start.negated()).length();
        this.itemDisplayElement.setScale(new Vector3f(distance, this.thickness, 0));
        this.itemDisplayElement.setTranslation(new Vector3f(0.5f, 0.5f, 0));
        scene.getElementHolder().addElement(this.itemDisplayElement);
    }

    public void cleanup(PonderScene scene) {

    }

    public void tick(PonderScene scene) {
        super.tick(scene);
    };

    @Override
    public ShowLineInstruction clone() {
        return (ShowLineInstruction) super.clone();
    }

    @Override
    public String toString() {
        return "ShowLineInstruction{" + "colour=" + colour + ", start=" + start.toString() + ", end=" + end.toString() + ", displayTime=" + displayTime + ", thickness=" + thickness + ", itemDisplayElement=" + itemDisplayElement.getEntityId() + ", time=" + time + '}';
    }
}
