package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.api.config.ConfigEntry;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.phys.Vec3;

public class TestWorldConfig {
    @ConfigEntry(show = false)
    @SerializedName("dev_modew")
    public boolean supaHackaModew = FabricLoader.getInstance().isDevelopmentEnvironment();

    @ConfigEntry
    @SerializedName("disable_syncw")
    public boolean disableServerSyncw = false;

    @ConfigEntry
    @Environment(EnvType.CLIENT)
    @SerializedName("tnt_timerw")
    public boolean tntTimerw = false;

    @ConfigEntry
    @SerializedName("test1w")
    public int test1w = 1;

    @ConfigEntry
    @SerializedName("test2w")
    public float test2w = 1;

    @ConfigEntry
    @SerializedName("test3w")
    public double test3w = 1;

    @ConfigEntry
    @SerializedName("test4w")
    public short test4w = 1;

    @ConfigEntry
    @SerializedName("test5w")
    public long test5w = 1;

    @ConfigEntry
    @SerializedName("test6w")
    public String test6w = "hello world";

    @ConfigEntry
    @SerializedName("test7w")
    public Vec3 test7w = new Vec3(0, 10, 0);
}