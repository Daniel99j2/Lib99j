package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.PonderScene;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class SetCameraInstruction extends PonderInstruction {
    private final Vec3 pos;
    private final Vec2 rot;
    private final int interpolateTime;

    public SetCameraInstruction(Vec3 pos, Vec2 rot, float interpolateTime) {
        this.pos = pos;
        this.rot = rot;
        this.interpolateTime = (int) (interpolateTime * 20);
    }

    @Override
    public boolean isComplete(PonderScene scene) {
        return this.time > interpolateTime;
    }

    @Override
    public int getValue(PonderScene scene) {
        return this.time;
    }

    @Override
    public int getMaxValue() {
        return this.interpolateTime;
    }

    @Override
    public void start(PonderScene scene) {
        scene.cameraPos = this.pos;
        scene.cameraRotation = this.rot;
        scene.cameraInterpolateTime = this.interpolateTime;
    }

    @Override
    public SetCameraInstruction clone() {
        return (SetCameraInstruction) super.clone();
    }

    @Override
    public String toString() {
        return "SetCameraInstruction{}";
    }
}
