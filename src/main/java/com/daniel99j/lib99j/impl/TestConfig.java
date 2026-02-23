package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.api.config.ConfigEntry;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.phys.Vec3;

public class TestConfig {
    @ConfigEntry(show = false)
    @SerializedName("dev_mode")
    public boolean supaHackaMode = FabricLoader.getInstance().isDevelopmentEnvironment();

    @ConfigEntry
    @SerializedName("disable_sync")
    public boolean disableServerSync = false;

    @ConfigEntry
    @Environment(EnvType.CLIENT)
    @SerializedName("tnt_timer")
    public boolean tntTimer = false;

    @ConfigEntry
    @SerializedName("test1")
    public int test1 = 1;

    @ConfigEntry
    @SerializedName("test2")
    public float test2 = 1;

    @ConfigEntry
    @SerializedName("test3")
    public double test3 = 1;

    @ConfigEntry
    @SerializedName("test4")
    public short test4 = 1;

    @ConfigEntry
    @SerializedName("test5")
    public long test5 = 1;

    @ConfigEntry
    @SerializedName("test6")
    public String test6 = "hello world";

    @ConfigEntry
    @SerializedName("test7")
    public Vec3 test7 = new Vec3(0, 10, 0);
}