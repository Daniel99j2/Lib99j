package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.*;
import com.daniel99j.lib99j.impl.BossBarVisibility;
import com.daniel99j.lib99j.impl.BypassPacket;
import com.daniel99j.lib99j.impl.PlayerReconfigurator;
import com.daniel99j.lib99j.ponder.api.PonderManager;
import com.daniel99j.lib99j.ponder.impl.PonderMenu;
import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.virtualentity.api.data.EntityData;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Guardian;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.UUID;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin implements PlayerReconfigurator {
    @Shadow public abstract void send(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener);

    @Shadow
    public abstract void disconnect(Component component);

    @Shadow
    @Final
    public Connection connection;

    @Shadow
    protected abstract GameProfile playerProfile();

    @Unique
    private ArrayList<Packet<?>> heldPackets = new ArrayList<>();

    @Inject(method = "handleCustomClickAction", at = @At("TAIL"))
    private void handleCustomPayload(ServerboundCustomClickActionPacket packet, CallbackInfo ci) {
        if (this instanceof ServerPlayerConnection networkHandler) {
            if (packet.id().getNamespace().equals(Lib99j.MOD_ID)) {
                ServerPlayer player = networkHandler.getPlayer();
                if (PonderManager.isPondering(player) && packet.id().getPath().equals("show_ponder_group") && packet.payload().orElseThrow() instanceof CompoundTag compoundTag) {
                    Identifier id = Identifier.parse(compoundTag.getStringOr("id", "error"));
                    PonderMenu.buildGroupMenu(player, id);
                }
                if (PonderManager.isPondering(player) && packet.id().getPath().equals("ponder_about_menu") && packet.payload().orElseThrow() instanceof CompoundTag compoundTag) {
                    Identifier id = Identifier.parse(compoundTag.getStringOr("id", "error"));
                    PonderMenu.buildBaseMenu(player, id);
                }
                if (PonderManager.isPondering(player) && packet.id().getPath().equals("close_ponder_menu")) {
                    PonderManager.activeScenes.get(player).closeMenu();
                }
            }

            if (packet.id().getNamespace().equals("lib99j_run_code_click_event")) {
                ServerPlayer player = networkHandler.getPlayer();
                UUID uuid = UUID.fromString(packet.id().getPath());
                if (RunCodeClickEvent.eventMap.containsKey(uuid)) {
                    RunCodeClickEvent event = RunCodeClickEvent.eventMap.get(uuid);
                    if (event != null && !event.isDisabled() && event.allowedPlayerUUID.equals(player.getUUID())) {
                        event.run(packet.payload());
                    } else {
                        Lib99j.debug("The code was garbage collected or it was for a different player");
                        this.disconnect(Component.translatable("lib99j.invalid_run_code_uuid"));
                    }
                } else {
                    Lib99j.debug("The code was not found");
                    this.disconnect(Component.translatable("lib99j.invalid_run_code_uuid"));
                }
            }
        }
    }
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"), cancellable = true, order = 1001)
    private void holdReconfigPackets(Packet<?> packet, @Nullable ChannelFutureListener listener, CallbackInfo ci) {
        if(this instanceof ServerPlayerConnection networkHandler && ((PlayerReconfigurator) networkHandler).lib99j$isReconfigurating()) {
            heldPackets.add(packet);
            ci.cancel();
        }
    }


    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true, order = 1001)
    private void cancelUnwantedPackets(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof BypassPacket(Packet<?> packet1)) {
            ci.cancel();
            send(packet1, null);
        } else if (this instanceof ServerPlayerConnection networkHandler) {
            ServerPlayer player = networkHandler.getPlayer();

            if(PonderManager.isPondering(player)) {
                GameCommonPacketList.PacketInfo info = GameCommonPacketList.getInfo(packet);
                //dont send world packets, but update display entities so that vfx still works!
                if(info != null && info.hasTag(PacketTag.MANY_USES)) {
                    if(packet instanceof ClientboundGameEventPacket gameEventPacket) {
                        if(gameEventPacket.getEvent() == ClientboundGameEventPacket.START_RAINING || gameEventPacket.getEvent() == ClientboundGameEventPacket.STOP_RAINING || gameEventPacket.getEvent() == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE || gameEventPacket.getEvent() == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE || gameEventPacket.getEvent() == ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT || gameEventPacket.getEvent() == ClientboundGameEventPacket.PLAY_ARROW_HIT_SOUND) {
                             ci.cancel();
                            return;
                        }
                    } else if(packet instanceof ClientboundCustomPayloadPacket) {

                    } else this.disconnect(Component.literal("Custom handler not setup for packet {}. Check logs and report to Daniel99j (https://modrinth.com/mod/lib99j)".replace("{}", packet.toString())));
                } else if (info != null && info.hasTag(PacketTag.WORLD) && !info.hasTag(PacketTag.PLAYER_CLIENT) && !info.hasTag(PacketTag.ENTITY)) {
                    ci.cancel();
                    return;
                }

                //If a guardian is being updated whilst in a ponder, then dont update it
                //It works because ponder guardians are in a different world
                //This prevents extreme lag when guardian rays are being created
                //The world reload fixes it automatically
                if(packet instanceof ClientboundSetEntityDataPacket trackerUpdateS2CPacket && ((ServerPlayerConnection) this).getPlayer().level.getEntity(trackerUpdateS2CPacket.id()) instanceof Guardian) {
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

            //hide particle swirls
//            if (GameProperties.isBadLuckCustomEffect() && packet instanceof ClientboundUpdateMobEffectPacket mobEffectPacket && mobEffectPacket.getEffect() == MobEffects.UNLUCK && mobEffectPacket.isEffectVisible()) {
//                ci.cancel();
//                this.send(new ClientboundUpdateMobEffectPacket(mobEffectPacket.getEntityId(), new MobEffectInstance(mobEffectPacket.getEffect(), mobEffectPacket.getEffectDurationTicks(), mobEffectPacket.getEffectAmplifier(), mobEffectPacket.isEffectAmbient(), false, mobEffectPacket.effectShowsIcon(), null), false));
//            }

            if(VFXUtils.hasAnyGenericScreenEffect(player)) {
                if (packet instanceof ClientboundSetBorderWarningDistancePacket && VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.RED_TINT))
                    ci.cancel();
                else if (packet instanceof ClientboundUpdateMobEffectPacket || packet instanceof ClientboundRemoveMobEffectPacket && VFXUtils.hasEffectEffect(player)) {
                    Holder<MobEffect> effectId;
                    if (packet instanceof ClientboundUpdateMobEffectPacket) {
                        effectId = ((ClientboundUpdateMobEffectPacket) packet).getEffect();
                    } else {
                        effectId = ((ClientboundRemoveMobEffectPacket) packet).effect();
                    }
                    if (VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.BLINDNESS) && effectId == MobEffects.BLINDNESS)
                        ci.cancel();
                    else if (VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.NAUSEA) && effectId == MobEffects.NAUSEA)
                        ci.cancel();
                    else if (VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.GREEN_HUNGER) && effectId == MobEffects.HUNGER)
                        ci.cancel();
                    else if (VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.GREEN_HEARTS) && effectId == MobEffects.POISON)
                        ci.cancel();
                    else if (VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.BLACK_HEARTS) && effectId == MobEffects.WITHER)
                        ci.cancel();
                    else if (VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.NIGHT_VISION) && effectId == MobEffects.NIGHT_VISION)
                        ci.cancel();
                    else if (VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.CONDUIT_POWER) && effectId == MobEffects.CONDUIT_POWER)
                        ci.cancel();
                    else if (VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.DARKNESS) && effectId == MobEffects.DARKNESS)
                        ci.cancel();
                } else if (packet instanceof ClientboundSetEntityDataPacket trackerUpdateS2CPacket && trackerUpdateS2CPacket.id() == player.getId() && (VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.SNOW) || VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.FIRE))) {
                    ci.cancel();
                    ArrayList<SynchedEntityData.DataValue<?>> out = new ArrayList<>();
                    for (SynchedEntityData.DataValue<?> entry : trackerUpdateS2CPacket.packedItems()) {
                        if (entry.id() != Entity.DATA_TICKS_FROZEN.id() || !VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.SNOW)) {
                            out.add(entry);
                        } else if (entry.id() != EntityData.FLAGS.id() || !(entry.value() instanceof Byte value) || (value & (1 << EntityData.ON_FIRE_FLAG_INDEX)) == 0 || !VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.FIRE)) {
                            out.add(entry);
                        }
                    }
                    sendFromThis(new ClientboundSetEntityDataPacket(trackerUpdateS2CPacket.id(), out));
                } else if (packet instanceof ClientboundSetBorderWarningDistancePacket && VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.RED_TINT))
                    ci.cancel();
                else if (packet instanceof ClientboundSetCameraPacket && VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.LOCK_CAMERA_AND_POS))
                    ci.cancel();
                else if (packet instanceof ClientboundGameEventPacket gameEventPacket && VFXUtils.hasGenericScreenEffect(player, GenericScreenEffect.LOCK_CAMERA_AND_POS))
                    if (gameEventPacket.getEvent() == ClientboundGameEventPacket.CHANGE_GAME_MODE) ci.cancel();
            }
        }
    }

    @Unique
    private void sendFromThis(Packet<?> packet) {
        ((ServerCommonPacketListenerImpl) (Object) this).send(new BypassPacket(packet));
    }

    @Override
    public void lib99j$sendHeldPackets() {
        for (Packet<?> heldPacket : heldPackets) {
            this.send(heldPacket, null);
        }
    }
}