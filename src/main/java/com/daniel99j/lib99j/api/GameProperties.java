package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.impl.BossBarVisibility;
import com.daniel99j.lib99j.impl.datagen.AssetProvider;
import com.daniel99j.lib99j.ponder.impl.PonderGuiTextures;

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

    /**
     * When turning on this value, all yellow bossbars are edited to be white when sent to the client
     * <p>When a bossbar is marked as invisible, it will be shown as yellow, which is retextured to be invisible</p>
     * <p>Use {@link BossBarVisibility#lib99j$setVisible(boolean)} to hide a bossbar</p>
     */
    private static boolean hideableBossBar = false;

    /**
     * When turning on this value, the unluck (Yes, that's a vanilla status effect) is re-textured and re-named to be "Custom Effect (/polymer effects)"
     */
    private static boolean customEffectBadLuck = false;

    private static boolean ponderEnabled = false;

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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPonderEnabled() {
        return ponderEnabled;
    }

    public static void throwIfPonderNotEnabled(String s) {
        if(!GameProperties.isPonderEnabled()) {
            throw new IllegalStateException(s);
        }
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
        if(!customEffectBadLuck) {
            AssetProvider.addOverrideTranslationAllSupportedLanguages("effect.minecraft.unluck", "Custom Effect (/polymer effects)");
            AssetProvider.addOverrideTranslationAllSupportedLanguages("effect.minecraft.unluck_fix", "Unluck");
        };
        GameProperties.customEffectBadLuck = true;
    }

    public static void enablePonder() {
        GameProperties.ponderEnabled = true;
        GameProperties.hideableBossBar = true;
        PonderGuiTextures.load();
    }
}
