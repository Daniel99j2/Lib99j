package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.api.config.ConfigDisplayType;
import com.daniel99j.lib99j.api.config.ConfigEntry;
import com.google.gson.annotations.SerializedName;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("ALL")
public class ExampleConfig {
    @ConfigEntry
    public boolean testBool = true;

    @ConfigEntry(sync = true)
    @SerializedName("wavy_ui")
    public boolean wavyUi = false;

    @ConfigEntry(min = 0, max = 10000)
    public Integer explosionDamage = 10;

    @ConfigEntry(displayType = ConfigDisplayType.SLIDER, max = 50, min = 0)
    public int explosionSize = 10;

    @ConfigEntry(displayType = ConfigDisplayType.COLOUR_PICKER)
    public int worldTint = 0xff00ff;

    @ConfigEntry(displayType = ConfigDisplayType.COLOUR_PICKER_ALPHA)
    public int overlayColour = 0xff0000ff;

    @ConfigEntry
    public Identifier packId = Identifier.withDefaultNamespace("test");

    @ConfigEntry
    public Vec2 spawnRotation = Vec2.ZERO;

    @ConfigEntry
    public Vec3 spawnPos = Vec3.ZERO;

    //ChatFormatting is an Enum
    @ConfigEntry
    public ChatFormatting formatType = ChatFormatting.RED;
}