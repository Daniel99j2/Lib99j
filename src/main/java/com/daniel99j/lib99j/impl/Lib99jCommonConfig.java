package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.api.config.ConfigEntry;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("ALL")
public class Lib99jCommonConfig {
    @ConfigEntry(show = false)
    public boolean allowSyncing = true;

    //If it were 50, it would be OVER 7.5 GIGABYTES JUST FOR PONDER ALONE, so it should be far more than needed
    @SerializedName("max_concurrent_ponders")
    @ConfigEntry(min = 1, max = 50)
    public int maxConcurrentPonders = 10;

    @SerializedName("enable_lib99j_commands")
    @ConfigEntry
    public boolean enableLib99jCommands = true;

    @SerializedName("booklet_ponder_additions")
    @ConfigEntry
    public boolean bookletPonderAdditions = true;

    @SerializedName("polydex_ponder_additions")
    @ConfigEntry
    public boolean polydexPonderAdditions = true;
}