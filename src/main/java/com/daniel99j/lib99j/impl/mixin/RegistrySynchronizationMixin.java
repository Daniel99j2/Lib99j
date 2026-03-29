package com.daniel99j.lib99j.impl.mixin;

import net.minecraft.core.RegistrySynchronization;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegistrySynchronization.class)
public class RegistrySynchronizationMixin {
//    @Inject(
//            method = "Lnet/minecraft/core/RegistrySynchronization;packRegistry(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/resources/RegistryDataLoader$RegistryData;Lnet/minecraft/core/RegistryAccess;Ljava/util/Set;Ljava/util/function/BiConsumer;)V", // synthetic lambda for packRegistry
//            at = @At("TAIL")
//    )
//    private static <T> void editSyncedValues(Registry registry, Set set, RegistryDataLoader.RegistryData registryData, DynamicOps dynamicOps, List list, Holder.Reference reference, CallbackInfo ci, @Local Optional<Tag> optional, @Local boolean bl) throws Throwable {
//        if (!RegistryModificationUtils.modifications.containsKey(registry.key().registry())) return;
//        Tag tag1;
//
//        if(optional.isPresent()) {
//            tag1 = optional.get();
//        } else {
//            tag1 = (Tag) registryData.elementCodec()
//                    .encodeStart(dynamicOps, (T)reference.value())
//                    .getOrThrow(string -> new IllegalArgumentException("Failed to serialize " + reference.key() + ": " + string));
//        }
//
//        AtomicBoolean anyModified = new AtomicBoolean(false);
//
//        RegistryModificationUtils.modifications.forEach(((registryResourceKey, functions) -> {
//            if(registryResourceKey.equals(registry.key().registry())) {
//                functions.forEach((tagBooleanFunction -> {
//                    boolean modified = tagBooleanFunction.apply(reference.key().identifier(), tag1);
//                    anyModified.set(anyModified.get() || modified);
//                }));
//            }
//        }));
//
//        if(!anyModified.get()) return;
//
//        list.removeIf((t) -> {
//            return t instanceof RegistrySynchronization.PackedRegistryEntry packed && packed.id().equals(reference.key().identifier());
//        });
//
//        list.add(new RegistrySynchronization.PackedRegistryEntry(reference.key().identifier(), optional));
//    }
}