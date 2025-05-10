package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.SimpleEntityElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Consumer;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin
        extends PlayerEntity implements PolymerEntity, Lib99jPlayerUtilController {
    @Shadow private @Nullable Entity cameraEntity;

    @Shadow public abstract OptionalInt openHandledScreen(@Nullable NamedScreenHandlerFactory factory);

    @Unique
    private ElementHolder holder;
    @Unique
    private SimpleEntityElement horse;
    @Unique
    private DisplayElement cameraPoint;

    @Unique
    private ArrayList<String> modTranslationCheckerTranslations = null;
    @Unique
    private ArrayList<Map.Entry<String, String>> requiredModTranslationCheckerTranslations = null;
    @Unique
    private Consumer<ArrayList<String>> modTranslationCheckerOutput = null;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void deathEvent(DamageSource damageSource, CallbackInfo ci) {
        VFXUtils.stopAllShaking(getPlayer());
        VFXUtils.clearGenericScreenEffects(getPlayer());
    }

    @Unique
    private ServerPlayerEntity getPlayer() {
        PlayerEntity player = this;
        return (ServerPlayerEntity) player;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if(this.holder == null) {
            this.holder = new ElementHolder();
            this.holder.setAttachment(new EntityAttachment(this.holder, getPlayer(), true));
            this.holder.startWatching(getPlayer());
        }
        if(this.horse != null && this.cameraPoint != null) {
            this.horse.setPitch(this.getPitch());
            this.horse.setYaw(this.getYaw());
            this.cameraPoint.setPitch(this.getPitch());
            this.cameraPoint.setYaw(this.getYaw());
        }
    }

    @Override
    public void setYaw(float yaw) {
        super.setYaw(yaw);
        if(this.horse != null && this.cameraPoint != null) {
            this.horse.setPitch(this.getPitch());
            this.horse.setYaw(this.getYaw());
            this.cameraPoint.setPitch(this.getPitch());
            this.cameraPoint.setYaw(this.getYaw());
        }
    }

    @Override
    public void setPitch(float pitch) {
        super.setPitch(pitch);
        if(this.horse != null && this.cameraPoint != null) {
            this.horse.setPitch(this.getPitch());
            this.horse.setYaw(this.getYaw());
            this.cameraPoint.setPitch(this.getPitch());
            this.cameraPoint.setYaw(this.getYaw());
        }
    }

    @Inject(method = "rotate", at = @At("HEAD"))
    public void rotate(float yaw, float pitch, CallbackInfo ci) {
        if(this.horse != null && this.cameraPoint != null) {
            this.horse.setPitch(pitch);
            this.horse.setYaw(yaw);
            this.cameraPoint.setPitch(pitch);
            this.cameraPoint.setYaw(yaw);
        }
    }

    @Override
    public void lib99j$lockCamera() {
        float yaw = getPlayer().getYaw();
        float pitch = getPlayer().getPitch();
        getPlayer().setCameraEntity(getPlayer());
        this.cameraPoint = new BlockDisplayElement();
        this.cameraPoint.setYaw(yaw);
        this.cameraPoint.setPitch(pitch);
        this.cameraPoint.setOffset(new Vec3d(0, getPlayer().getStandingEyeHeight(), 0));
        this.cameraPoint.setInvisible(true);
        this.holder.addElement(this.cameraPoint);
        this.horse = new SimpleEntityElement(EntityType.HORSE) {

        };
        HorseEntity testEntity = new HorseEntity(EntityType.HORSE, getPlayer().getWorld());
        this.horse.setInvisible(true);
        this.horse.setOffset(new Vec3d(0, getPlayer().getStandingEyeHeight(), 0).subtract(testEntity.getPassengerRidingPos(getPlayer()).getY()).subtract(testEntity.getPos()));
        this.horse.setYaw(yaw);
        this.horse.setPitch(pitch);
        this.holder.addElement(horse);

        getPlayer().networkHandler.sendPacket(VirtualEntityUtils.createSetCameraEntityPacket(this.cameraPoint.getEntityId()));
        getPlayer().networkHandler.sendPacket(VirtualEntityUtils.createRidePacket(horse.getEntityId(), IntList.of(getPlayer().getId())));
        getPlayer().networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, GameMode.SPECTATOR.getIndex()));
        getPlayer().networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(getPlayer().getId(), List.of(DataTracker.SerializedEntry.of(EntityTrackedData.POSE, EntityPose.STANDING))));
    }

    @Override
    public void lib99j$unlockCamera() {
        getPlayer().networkHandler.sendPacket(new SetCameraEntityS2CPacket(this.cameraEntity == null ? getPlayer() : this.cameraEntity));
        getPlayer().networkHandler.sendPacket(VirtualEntityUtils.createRidePacket(horse.getEntityId(), IntList.of()));
        getPlayer().networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, getPlayer().interactionManager.getGameMode().getIndex()));
        getPlayer().networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(getPlayer().getId(), List.of(DataTracker.SerializedEntry.of(EntityTrackedData.POSE, getPlayer().getPose()))));
        this.holder.removeElement(this.cameraPoint);
        this.holder.removeElement(this.horse);
        this.cameraPoint = null;
        this.horse = null;
    }

    @Override
    public void lib99j$addModTranslationCheckerTranslation(String entry) {
        if(this.modTranslationCheckerTranslations == null) this.modTranslationCheckerTranslations = new ArrayList<>();
        this.modTranslationCheckerTranslations.add(entry);
    }

    @Override
    public void lib99j$setNeededModCheckerTranslations(ArrayList<Map.Entry<String, String>> translations) {
        this.requiredModTranslationCheckerTranslations = translations;
    }

    @Override
    public void lib99j$setModCheckerOutput(Consumer<ArrayList<String>> output) {
        this.modTranslationCheckerOutput = output;
    }

    @Override
    public void lib99j$runModCheckerOutput() {
        ArrayList<String> mods = new ArrayList<>();
        for (Map.Entry<String, String> entry : requiredModTranslationCheckerTranslations) {
            String mod = entry.getKey();
            String missingText = entry.getValue();

            boolean isMissing = false;
            for (String text : modTranslationCheckerTranslations) {
                if (text.contains(missingText)) {
                    isMissing = true;
                    break;
                }
            }

            if (!isMissing) {
                mods.add(mod);
            }
        }
        this.modTranslationCheckerOutput.accept(mods);

        this.modTranslationCheckerOutput = null;
        this.modTranslationCheckerTranslations = null;
        this.requiredModTranslationCheckerTranslations = null;
    }
}