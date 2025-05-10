package com.daniel99j.lib99j.api;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleHelper {
    public static void spawnParticlesAtPosition(World world, Vec3d position, ParticleEffect particle, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(particle, position.x, position.y, position.z, count, offsetX, offsetY, offsetZ, speed);
        }
    }
}
