package com.daniel99j.lib99j.api;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings({"unused"})
public class ParticleHelper {
    public static void spawnParticlesAtPosition(Level world, Vec3 position, ParticleOptions particle, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        if (world instanceof ServerLevel serverWorld) {
            serverWorld.sendParticles(particle, position.x, position.y, position.z, count, offsetX, offsetY, offsetZ, speed);
        }
    }
}
