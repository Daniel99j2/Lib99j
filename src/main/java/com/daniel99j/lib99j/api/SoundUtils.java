package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.Lib99j;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SoundUtils {
    private static ArrayList<CustomSubtitle> customSubtitles = new ArrayList<>();

    public static void playSoundAtPosition(ServerWorld world, Vec3d position, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        world.playSound(null, position.x, position.y, position.z, sound, category, volume, pitch);
    }

    public static SoundEvent registerCustomSubtitle(Identifier id, SoundEvent soundEvent, boolean show) {
        SoundUtils.customSubtitles.add(new CustomSubtitle(id, soundEvent, show));
        return SoundEvent.of(Identifier.of(Lib99j.MOD_ID, id.toString().replace(":", ".").replace("/", ".")));
    }

    public static ArrayList<CustomSubtitle> getCustomSubtitles() {
        return customSubtitles;
    }

    public record CustomSubtitle(Identifier id, SoundEvent sound, boolean show) {

    }
}