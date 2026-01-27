package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class DamageSourceMixin {
    @Unique
    private final Map<Holder<DamageType>, MutableInt> lib99j$damageSourceTimers = new HashMap<>();

    @Inject(method = "tick", at = @At("TAIL"))
    public void lib99j$tickMultiTickDamage(CallbackInfo ci) {
        lib99j$damageSourceTimers.forEach((type, timer) -> {
            timer.add(-1);
        });
        lib99j$damageSourceTimers.entrySet().removeIf(entry -> entry.getValue().intValue() <= 0);
    }

    @Redirect(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z",
                    ordinal = 3
            )
    )
    private boolean lib99j$multiTickDamage(DamageSource source, TagKey<DamageType> tag) {
        if (tag == DamageTypeTags.BYPASSES_COOLDOWN) {
            return source.is(tag) || lib99j$checkMultiTick(source);
        }
        throw new IllegalStateException("Lib99j MultiTickDamage mixin targeting the wrong method!");
    }

    @Unique
    private boolean lib99j$checkMultiTick(DamageSource source) {
        if (!source.is(TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "multi_tick_damage")))) {
            return false;
        }

        var entry = source.typeHolder();
        MutableInt timer = lib99j$damageSourceTimers.get(entry);

        if (timer == null) {
            lib99j$damageSourceTimers.put(entry, new MutableInt(20));
            return true;
        } else if (timer.intValue() == 20) {
            return true;
        }

        return false;
    }

}