package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.PlayPacketUtils;
import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.ponder.impl.PonderManager;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {

    @Shadow public abstract void resumeFlushing();

    @Shadow public abstract void send(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener);

    @Shadow public abstract void send(Packet<?> packet);

    @Unique
    private volatile boolean lib99j$fromThis; //volatile makes it so all the threads have the same value

    @Inject(method = "send", at = @At("HEAD"), cancellable = true, order = 1001)
    private void cancelUnwantedPackets(Packet<?> packet, CallbackInfo ci) {
        if (this instanceof ServerPlayerConnection networkHandler && !lib99j$fromThis) {
            ServerPlayer player = networkHandler.getPlayer();
            if (packet instanceof ClientboundSetBorderWarningDistancePacket && VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.RED_TINT))
                ci.cancel();
            else if (packet instanceof ClientboundUpdateMobEffectPacket || packet instanceof ClientboundRemoveMobEffectPacket entityStatusEffectS2CPacket2 && VFXUtils.hasEffectEffect(player)) {
                Holder<MobEffect> effectId;
                if (packet instanceof ClientboundUpdateMobEffectPacket) {
                    effectId = ((ClientboundUpdateMobEffectPacket) packet).getEffect();
                } else {
                    effectId = ((ClientboundRemoveMobEffectPacket) packet).effect();
                }
                if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.BLINDNESS) && effectId == MobEffects.BLINDNESS)
                    ci.cancel();
                else if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.NAUSEA) && effectId == MobEffects.NAUSEA)
                    ci.cancel();
                else if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.GREEN_HUNGER) && effectId == MobEffects.HUNGER)
                    ci.cancel();
                else if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.GREEN_HEARTS) && effectId == MobEffects.POISON)
                    ci.cancel();
                else if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.BLACK_HEARTS) && effectId == MobEffects.WITHER)
                    ci.cancel();
                else if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.NIGHT_VISION) && effectId == MobEffects.NIGHT_VISION)
                    ci.cancel();
            } else if (packet instanceof ClientboundSetEntityDataPacket trackerUpdateS2CPacket && trackerUpdateS2CPacket.id() == player.getId() && (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.SNOW) || VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.FIRE))) {
                ci.cancel();
                ArrayList<SynchedEntityData.DataValue<?>> out = new ArrayList<>();
                for (SynchedEntityData.DataValue<?> entry : trackerUpdateS2CPacket.packedItems()) {
                    if (entry.id() != Entity.DATA_TICKS_FROZEN.id() || !VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.SNOW)) {
                        out.add(entry);
                    } else if (entry.id() != EntityTrackedData.FLAGS.id() || !(entry.value() instanceof Byte value) || (value & (1 << EntityTrackedData.ON_FIRE_FLAG_INDEX)) == 0 || !VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.FIRE)) {
                        out.add(entry);
                    }
                }
                sendFromThis(new ClientboundSetEntityDataPacket(trackerUpdateS2CPacket.id(), out));
            } else if (packet instanceof ClientboundSetBorderWarningDistancePacket && VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.RED_TINT))
                ci.cancel();
            else if (packet instanceof ClientboundSetCameraPacket && VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS))
                ci.cancel();



            if (packet instanceof Lib99j.BypassPacket(Packet<?> packet1)) {
                ci.cancel();
                send(packet1, null);
            } else if(PonderManager.isPondering(player)) {
                PlayPacketUtils.PacketInfo info = PlayPacketUtils.getInfo(packet);
                //dont send world packets, but update display entities so that vfx still works!
                if (info != null && info.hasTag(PlayPacketUtils.PacketTag.WORLD) && !info.hasTag(PlayPacketUtils.PacketTag.PLAYER_CLIENT) && !info.hasTag(PlayPacketUtils.PacketTag.ENTITY)) {
                    ci.cancel();
                }
            }
        }
    }

    @Unique
    private void sendFromThis(Packet<?> packet) {
        lib99j$fromThis = true;
        ((ServerCommonPacketListenerImpl) (Object) this).send(packet);
        lib99j$fromThis = false;
    }
}