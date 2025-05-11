package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.impl.SupplyingEvent;
import com.daniel99j.lib99j.impl.TriggerableEvent;
import eu.pb4.polymer.common.api.events.SimpleEvent;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CustomEvents {
    public static TriggerableEvent<Runnable> GAME_LOADED = new TriggerableEvent<>();
    /**
     * This is for my mods, so that datagen does not clear each other's mods.
     * This library should be the only one hooked into fabric api
     */
    public static SupplyingEvent<FabricDataGenerator> DATAGEN_RUN = new SupplyingEvent<>();
}
