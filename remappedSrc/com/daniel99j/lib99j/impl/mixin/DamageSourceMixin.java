package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(LivingEntity.class)
public abstract class DamageSourceMixin {
    @Unique
    private final Map<RegistryEntry<DamageType>, MutableInt> lib99j$damageSourceTimers = new HashMap<>();

    @Inject(method = "tick", at = @At("TAIL"))
    public void lib99j$tickMultiTickDamage(CallbackInfo ci) {
        lib99j$damageSourceTimers.forEach((type, timer) -> {
            timer.add(-1);
        });
        lib99j$damageSourceTimers.entrySet().removeIf(entry -> entry.getValue().getValue() <= 0);
    }

    @Redirect(
            method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/damage/DamageSource;isIn(Lnet/minecraft/registry/tag/TagKey;)Z",
                    ordinal = 3
            )
    )
    private boolean lib99j$multiTickDamage(DamageSource source, TagKey<DamageType> tag) {
        if (tag == DamageTypeTags.BYPASSES_COOLDOWN) {
            return source.isIn(tag) || lib99j$checkMultiTick(source);
        }
        throw new IllegalStateException("Lib99j MultiTickDamage mixin targeting the wrong method!");
    }

    @Unique
    private boolean lib99j$checkMultiTick(DamageSource source) {
        if (!source.isIn(TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Lib99j.MOD_ID, "multi_tick_damage")))) {
            return false;
        }

        var entry = source.getTypeRegistryEntry();
        MutableInt timer = lib99j$damageSourceTimers.get(entry);

        if (timer == null) {
            lib99j$damageSourceTimers.put(entry, new MutableInt(20));
            return true;
        }

        if (timer.getValue() == 20) {
            return true;
        }

        return false;
    }

}