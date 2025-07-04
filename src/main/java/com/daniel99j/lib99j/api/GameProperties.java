package com.daniel99j.lib99j.api;

/**
 * Many game properties such as data gen etc.
 * <p>Most are just well-educated guesses
 */
@SuppressWarnings({"unused"})
public class GameProperties {
    public static boolean runningDataGen;

    /**
     * This value represents if a content mod is loaded.
     * <p>You should turn this on if your mod is not a library
     * <p>It enables things like warden camerashake roars etc
     */
    public static boolean contentModsLoaded;
}
