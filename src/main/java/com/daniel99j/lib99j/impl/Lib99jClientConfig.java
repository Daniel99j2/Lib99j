package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.api.config.ConfigEntry;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("ALL")
public class Lib99jClientConfig {
    @ConfigEntry
    @SerializedName("ponder_tooltip")
    public boolean ponderTooltip = true;

    @ConfigEntry(requiresRestart = true)
    @SerializedName("extra_item_groups")
    public boolean extraItemGroups = false;

    @ConfigEntry
    @SerializedName("store_items_from_packets")
    public boolean storeItems = false;
}