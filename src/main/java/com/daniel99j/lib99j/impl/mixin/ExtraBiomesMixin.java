package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.ExtraBiomeUtil;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.repository.KnownPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

@Mixin(RegistrySynchronization.class)
public class ExtraBiomesMixin {
    @ModifyVariable(method = "packRegistry", at = @At(value = "HEAD"), argsOnly = true)
    private static <T> Set<KnownPack> makeItUnknown(Set<KnownPack> clientKnownPacks, @Local final RegistryDataLoader.RegistryData<T> registryData) {
        if(registryData.key().equals(Registries.BIOME)) {
            return Set.of();
        }
        return clientKnownPacks;
    }

    @ModifyVariable(method = "packRegistry", at = @At(value = "HEAD"), argsOnly = true)
    private static <T> BiConsumer<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> makeItAddThem(BiConsumer<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> output, @Local final RegistryDataLoader.RegistryData<T> registryData) {
        if(registryData.key().equals(Registries.BIOME)) {
            return ((registry, entries) -> {
                int i = entries.size() + 1;
                for (RegistrySynchronization.PackedRegistryEntry packed : ExtraBiomeUtil.EXTRAS) {
                    entries.add(packed);
                    int old = ExtraBiomeUtil.PALETTE_IDS.getOrDefault(packed.id(), -1);
                    if (old == -1) {
                        ExtraBiomeUtil.PALETTE_IDS.put(packed.id(), i);
                    } else if (old != i) {
                        Lib99j.LOGGER.error("BIG ERROR! ID MISMATCH! " + old + " vs " + i);
                    }
                    i++;
                }
                output.accept(registry, entries);
            });
        }
        return output;
    }
}