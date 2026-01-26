package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.ponder.impl.PonderManager;
import eu.pb4.factorytools.mixin.LivingEntityAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "damage", at = @At("TAIL"))
    private void lib99j$stopPondering(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(((Object) this) instanceof ServerPlayerEntity player) {
            if(PonderManager.isPondering(player)) {
                PonderManager.activeScenes.get(player).stopPondering();
            }
        }
    }
}