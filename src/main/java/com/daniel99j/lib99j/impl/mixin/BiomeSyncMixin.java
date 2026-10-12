package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.api.ExtraBiomeUtil;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Stream;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public class BiomeSyncMixin {
    @Inject(method = "filterRegistries", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/RegistryAccess$ImmutableRegistryAccess;<init>(Ljava/util/List;)V", shift = At.Shift.BY, by = -1))
    private static void dontRemoveCustomEntries(RegistryAccess.Frozen original, Stream<ResourceKey<? extends Registry<?>>> keysToInclude, CallbackInfoReturnable<RegistryAccess.Frozen> cir, @Local List<? extends Registry<?>> filteredRegistries) {
        for (Registry<?> registry : filteredRegistries) {
            if(registry.key().equals(Registries.BIOME)) {
                if(registry instanceof MappedRegistry<?> m) {
                    @SuppressWarnings("unchecked")
                    MappedRegistry<Biome> mapped = ((MappedRegistry<Biome>) m);
                    for (RegistrySynchronization.PackedRegistryEntry extra : ExtraBiomeUtil.EXTRAS) {
                        mapped.register(ResourceKey.create(Registries.BIOME, extra.id()), Biome.NETWORK_CODEC.decode(NbtOps.INSTANCE, extra.data().get()).getOrThrow().getFirst(), RegistrationInfo.BUILT_IN);
                    }
                }
            }
        }
    }
}