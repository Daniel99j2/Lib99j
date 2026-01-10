package com.daniel99j.lib99j.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@SuppressWarnings({"unused"})
@Environment(EnvType.CLIENT)
public class ClientGuiUtils {
    public static boolean shouldShowCrossHairLike() {
        return
                shouldShowHungerLike()
                        && MinecraftClient.getInstance().options.getPerspective().isFirstPerson();
    }

    public static boolean shouldShowHungerLike() {
        return
                MinecraftClient.isHudEnabled()
                        && MinecraftClient.getInstance().player != null
                        && !MinecraftClient.getInstance().player.isSpectator();
    }
}
