package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.Lib99j;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

public class MiscUtils {
    public static String getTextBetween(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        if (startIndex == -1) return ""; // start string not found

        startIndex += start.length(); // skip past start
        int endIndex = text.indexOf(end, startIndex);
        if (endIndex == -1) return ""; // end string not found

        return text.substring(startIndex, endIndex);
    }


    public static String replaceTextBetween(String text, String start, String end, String replacement) {
        int startIndex = text.indexOf(start);
        if (startIndex == -1) return text; // start string not found

        int endIndex = text.indexOf(end, startIndex);
        if (endIndex == -1) return text; // end string not found

        endIndex += end.length(); // include the end
        return text.substring(0, startIndex) + replacement + text.substring(endIndex);
    }

    public static void runOnMainThread(Runnable code) {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Minecraft.getInstance().execute(code);
        } else {
            Lib99j.getServerOrThrow().execute(code);
        }
    }

    public static <T> T fallback(@Nullable T check, T fallback) {
        return check != null ? check : fallback;
    }
}
