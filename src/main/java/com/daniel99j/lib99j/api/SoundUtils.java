package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.Lib99j;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class SoundUtils {
    private static ArrayList<CustomSubtitle> customSubtitles = new ArrayList<>();

    public static void playSoundAtPosition(ServerLevel world, Vec3 position, SoundEvent sound, SoundSource category, float volume, float pitch) {
        world.playSound(null, position.x, position.y, position.z, sound, category, volume, pitch);
    }

    public static SoundEvent registerCustomSubtitle(Identifier id, SoundEvent soundEvent, boolean show) {
        SoundUtils.customSubtitles.add(new CustomSubtitle(id, soundEvent, show));
        return SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, id.toString().replace(":", ".").replace("/", ".")));
    }

    public static ArrayList<CustomSubtitle> getCustomSubtitles() {
        return customSubtitles;
    }

    public record CustomSubtitle(Identifier id, SoundEvent sound, boolean show) {

    }
}