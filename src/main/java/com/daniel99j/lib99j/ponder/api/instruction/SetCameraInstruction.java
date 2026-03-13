package com.daniel99j.lib99j.ponder.api.instruction;

import com.daniel99j.lib99j.ponder.api.PonderScene;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class SetCameraInstruction extends InstantPonderInstruction {
    private final Vec3 pos;
    private final Vec2 rot;
    private final int interpolateTime;

    public SetCameraInstruction(Vec3 pos, Vec2 rot, int interpolateTime) {
        this.pos = pos;
        this.rot = rot;
        this.interpolateTime = interpolateTime;
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
