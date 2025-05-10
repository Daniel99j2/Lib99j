package com.daniel99j.lib99j.api;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

public class SoundUtils {
    public static void playSoundAtPosition(ServerWorld world, Vec3d position, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        world.playSound(null, position.x, position.y, position.z, sound, category, volume, pitch);
    }
}
