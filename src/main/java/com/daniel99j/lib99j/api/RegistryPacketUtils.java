package com.daniel99j.lib99j.api;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.schedule.Activity;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class RegistryPacketUtils {
    @ApiStatus.Internal
    public static Map<Identifier, List<BiFunction<Identifier, Tag, Boolean>>> modifications = new HashMap<>();

    public static void addRegistryModification(Identifier key, BiFunction<Identifier, Tag, Boolean> consumer) {
        if(!modifications.containsKey(key)) {
            modifications.put(key, new ArrayList<>());
        }
        modifications.get(key).add(consumer);
    }
}
