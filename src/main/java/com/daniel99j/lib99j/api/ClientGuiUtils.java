package com.daniel99j.lib99j.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@SuppressWarnings({"unused"})
@Environment(EnvType.CLIENT)
public class ClientGuiUtils {
    public static boolean shouldShowCrossHairLike() {
        return
                shouldShowHungerLike()
                        && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    public static boolean shouldShowHungerLike() {
        return
                Minecraft.renderNames()
                        && Minecraft.getInstance().player != null
                        && !Minecraft.getInstance().player.isSpectator();
    }
}
