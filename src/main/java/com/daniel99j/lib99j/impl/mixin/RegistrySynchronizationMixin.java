package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.api.RegistryPacketUtils;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(RegistrySynchronization.class)
public class RegistrySynchronizationMixin {
    @Inject(
            method = "method_56595", // synthetic lambda for packRegistry
            at = @At("TAIL")
    )
    private static <T> void editSyncedValues(Registry registry, Set set, RegistryDataLoader.RegistryData registryData, DynamicOps dynamicOps, List list, Holder.Reference reference, CallbackInfo ci, @Local Optional<Tag> optional, @Local boolean bl) throws Throwable {
        if (!RegistryPacketUtils.modifications.containsKey(registry.key().registry())) return;
        Tag tag1;

        if(optional.isPresent()) {
            tag1 = optional.get();
        } else {
            tag1 = (Tag) registryData.elementCodec()
                    .encodeStart(dynamicOps, (T)reference.value())
                    .getOrThrow(string -> new IllegalArgumentException("Failed to serialize " + reference.key() + ": " + string));
        }

        AtomicBoolean anyModified = new AtomicBoolean(false);

        RegistryPacketUtils.modifications.forEach(((registryResourceKey, functions) -> {
            if(registryResourceKey.equals(registry.key().registry())) {
                functions.forEach((tagBooleanFunction -> {
                    boolean modified = tagBooleanFunction.apply(reference.key().identifier(), tag1);
                    anyModified.set(anyModified.get() || modified);
                }));
            }
        }));

        if(!anyModified.get()) return;

        list.removeIf((t) -> {
            return t instanceof RegistrySynchronization.PackedRegistryEntry packed && packed.id().equals(reference.key().identifier());
        });

        list.add(new RegistrySynchronization.PackedRegistryEntry(reference.key().identifier(), optional));
    }
}