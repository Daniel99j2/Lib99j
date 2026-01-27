package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.ponder.api.PonderScene;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class PonderManager {
    public static Map<ServerPlayer, PonderScene> activeScenes = new HashMap<>();

    public static boolean isPondering(ServerPlayer player) {
        return activeScenes.containsKey(player) && !activeScenes.get(player).isToBeRemoved();
    }

    public static void tick() {
        activeScenes.entrySet().removeIf((scene) -> {
            boolean toRemove = scene.getValue().isToBeRemoved();
            if(toRemove) Lib99j.getServerOrThrow().levels.remove(scene.getValue().worldKey);
            return toRemove;
        });

        for(PonderScene scene : activeScenes.values()) {
            scene.tick();
        }
    }
}
