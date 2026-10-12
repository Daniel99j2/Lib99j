package com.daniel99j.lib99j.impl;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;

public class LoadingBlastDoor {
    public static boolean isDevelopmentEnvironment = FabricLoader.getInstance().isDevelopmentEnvironment() || Files.exists(FabricLoader.getInstance().getConfigDir().resolve("lib99j_force_dev.txt"));
    public static boolean isDevelopingLib99j = System.getenv("DEVELOPING_LIB99J") != null && isDevelopmentEnvironment && FabricLoader.getInstance().getConfigDir().toAbsolutePath().getParent().toString().contains("Lib99j");;
    public static boolean personalFeatures = System.getenv("DANIEL99J_MODE") != null;
}
