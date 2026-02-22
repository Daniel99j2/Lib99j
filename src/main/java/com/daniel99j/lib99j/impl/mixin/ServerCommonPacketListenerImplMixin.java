package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.GameProperties;
import com.daniel99j.lib99j.api.PlayPacketUtils;
import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.impl.BossBarVisibility;
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
import net.minecraft.world.BossEvent;
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

    @Shadow public abstract void send(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener);

    @Inject(method = "send", at = @At("HEAD"), cancellable = true, order = 1001)
    private void cancelUnwantedPackets(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof Lib99j.BypassPacket(Packet<?> packet1)) {
            ci.cancel();
            send(packet1, null);
        } else if (this instanceof ServerPlayerConnection networkHandler) {
            ServerPlayer player = networkHandler.getPlayer();

            if(PonderManager.isPondering(player)) {
                PlayPacketUtils.PacketInfo info = PlayPacketUtils.getInfo(packet);
                //dont send world packets, but update display entities so that vfx still works!
                if (info != null && info.hasTag(PlayPacketUtils.PacketTag.WORLD) && !info.hasTag(PlayPacketUtils.PacketTag.PLAYER_CLIENT) && !info.hasTag(PlayPacketUtils.PacketTag.ENTITY)) {
                    ci.cancel();
                    return;
                }
            }

            if(GameProperties.isHideableBossBar() && packet instanceof ClientboundBossEventPacket clientboundBossEventPacket && clientboundBossEventPacket instanceof BossBarVisibility visibility) {
                if(((BossbarAccessor) clientboundBossEventPacket).getOperation().getType() == ClientboundBossEventPacket.OperationType.ADD) {
                    BossEvent.BossBarColor colour = ((BossbarAccessor2) ((BossbarAccessor) clientboundBossEventPacket).getOperation()).getColor();
                    if(!visibility.lib99j$isVisible()) {
                        ((BossbarAccessor2) ((BossbarAccessor) clientboundBossEventPacket).getOperation()).setColor(BossEvent.BossBarColor.YELLOW);
                    } else if(visibility.lib99j$isVisible() && colour == BossEvent.BossBarColor.YELLOW) {
                        ((BossbarAccessor2) ((BossbarAccessor) clientboundBossEventPacket).getOperation()).setColor(BossEvent.BossBarColor.WHITE);
                    }
                }

                if(((BossbarAccessor) clientboundBossEventPacket).getOperation().getType() == ClientboundBossEventPacket.OperationType.UPDATE_STYLE) {
                    BossEvent.BossBarColor colour = ((BossbarAccessor3) ((BossbarAccessor) clientboundBossEventPacket).getOperation()).getColor();
                    if(!visibility.lib99j$isVisible()) {
                        ((BossbarAccessor3) ((BossbarAccessor) clientboundBossEventPacket).getOperation()).setColor(BossEvent.BossBarColor.YELLOW);
                    } else if(visibility.lib99j$isVisible() && colour == BossEvent.BossBarColor.YELLOW) {
                        ((BossbarAccessor3) ((BossbarAccessor) clientboundBossEventPacket).getOperation()).setColor(BossEvent.BossBarColor.WHITE);
                    }
                }
            }

            if (packet instanceof ClientboundSetBorderWarningDistancePacket && VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.RED_TINT))
                ci.cancel();
            else if (packet instanceof ClientboundUpdateMobEffectPacket || packet instanceof ClientboundRemoveMobEffectPacket && VFXUtils.hasEffectEffect(player)) {
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
                else if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.DARKNESS) && effectId == MobEffects.DARKNESS)
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
        }
    }

    @Unique
    private void sendFromThis(Packet<?> packet) {
        ((ServerCommonPacketListenerImpl) (Object) this).send(new Lib99j.BypassPacket(packet));
    }
}