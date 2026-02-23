package com.daniel99j.lib99j.api;

/**
 * Many game properties such as data gen etc.
 * <p>Most are just well-educated guesses, or a flag set by a mod
 */
@SuppressWarnings({"unused"})
public class GameProperties {
    private static boolean runningDataGen = false;

    /**
     * This value represents if a content mod is loaded.
     * <p>You should turn this on if your mod is not a library
     * <p>It enables things like /vfx, etc
     * <p>When disabled, you wouldn't even know the mod was installed
     */
    private static boolean contentModsLoaded = false;

    private static boolean hideableBossBar = false;

    private static boolean customEffectBadLuck = false;

    public static boolean isHideableBossBar() {
        return hideableBossBar;
    }

    public static boolean isBadLuckCustomEffect() {
        return customEffectBadLuck;
    }

    public static boolean isRunningDataGen() {
        return runningDataGen;
    }

    public static boolean areContentModsLoaded() {
        return contentModsLoaded;
    }

    public static void enableHideableBossBar() {
        GameProperties.hideableBossBar = true;
    }

    public static void markContentModsLoaded() {
        GameProperties.contentModsLoaded = true;
    }

    public static void markRunningDataGen() {
        GameProperties.runningDataGen = true;
    }

    public static void enableCustomEffectBadLuck() {
        GameProperties.customEffectBadLuck = true;
    }

    public static void enableHideableScreenBackgrounds() {

    }
}
