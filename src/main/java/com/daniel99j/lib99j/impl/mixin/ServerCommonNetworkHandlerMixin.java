package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.api.VFXUtils;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(ServerCommonNetworkHandler.class)
public class ServerCommonNetworkHandlerMixin {

    @Unique
    private boolean lib99j$fromThis;

    @Inject(method = "send", at = @At("HEAD"), cancellable = true, order = 1001)
    private void cancelVFXPackets(Packet<?> packet, ChannelFutureListener channelFutureListener, CallbackInfo ci) {
        if (this instanceof PlayerAssociatedNetworkHandler networkHandler && !lib99j$fromThis) {
            ServerPlayerEntity player = networkHandler.getPlayer();
            if (packet instanceof WorldBorderWarningBlocksChangedS2CPacket && VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.RED_TINT))
                ci.cancel();
            else if (packet instanceof EntityStatusEffectS2CPacket || packet instanceof RemoveEntityStatusEffectS2CPacket entityStatusEffectS2CPacket2 && VFXUtils.hasEffectEffect(player)) {
                RegistryEntry<StatusEffect> effectId;
                if (packet instanceof EntityStatusEffectS2CPacket) {
                    effectId = ((EntityStatusEffectS2CPacket) packet).getEffectId();
                } else {
                    effectId = ((RemoveEntityStatusEffectS2CPacket) packet).effect();
                }
                if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.BLINDNESS) && effectId == StatusEffects.BLINDNESS)
                    ci.cancel();
                else if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.NAUSEA) && effectId == StatusEffects.NAUSEA)
                    ci.cancel();
                else if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.GREEN_HUNGER) && effectId == StatusEffects.HUNGER)
                    ci.cancel();
                else if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.GREEN_HEARTS) && effectId == StatusEffects.POISON)
                    ci.cancel();
                else if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.BLACK_HEARTS) && effectId == StatusEffects.WITHER)
                    ci.cancel();
                else if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.NIGHT_VISION) && effectId == StatusEffects.NIGHT_VISION)
                    ci.cancel();
            } else if (packet instanceof EntityTrackerUpdateS2CPacket trackerUpdateS2CPacket && trackerUpdateS2CPacket.id() == player.getId() && (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.SNOW) || VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.FIRE))) {
                ci.cancel();
                ArrayList<DataTracker.SerializedEntry<?>> out = new ArrayList<>();
                for (DataTracker.SerializedEntry<?> entry : trackerUpdateS2CPacket.trackedValues()) {
                    if (entry.id() != Entity.FROZEN_TICKS.id() || !VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.SNOW)) {
                        out.add(entry);
                    } else if (entry.id() != EntityTrackedData.FLAGS.id() || !(entry.value() instanceof Byte value) || (value & (1 << EntityTrackedData.ON_FIRE_FLAG_INDEX)) == 0 || !VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.FIRE)) {
                        out.add(entry);
                    }
                }
                sendFromThis(new EntityTrackerUpdateS2CPacket(trackerUpdateS2CPacket.id(), out));
            } else if (packet instanceof WorldBorderWarningBlocksChangedS2CPacket && VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.RED_TINT))
                ci.cancel();
        }
    }

    @Unique
    private void sendFromThis(Packet<?> packet) {
        lib99j$fromThis = true;
        ((ServerCommonNetworkHandler) (Object) this).sendPacket(packet);
        lib99j$fromThis = false;
    }
}