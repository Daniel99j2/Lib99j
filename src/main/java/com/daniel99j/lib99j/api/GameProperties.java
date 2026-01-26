package com.daniel99j.lib99j.api;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Many game properties such as data gen etc.
 * <p>Most are just well-educated guesses
 */
@SuppressWarnings({"unused"})
public class GameProperties {
    public static boolean runningDataGen = false;

    /**
     * This value represents if a content mod is loaded.
     * <p>You should turn this on if your mod is not a library
     * <p>It enables things like warden camerashake roars, /vfx, etc
     * <p>When disabled, you wouldn't even know the mod was installed
     */
    public static boolean contentModsLoaded = FabricLoader.getInstance().isDevelopmentEnvironment();
}
