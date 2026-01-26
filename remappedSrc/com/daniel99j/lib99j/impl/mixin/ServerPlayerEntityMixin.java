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
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
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
    @Shadow
    private @Nullable Entity cameraEntity;
    @Unique
    private ElementHolder lib99j$holder;
    @Unique
    private SimpleEntityElement lib99j$horse;
    @Unique
    private DisplayElement lib99j$cameraPoint;
    @Unique
    private ArrayList<String> lib99j$modTranslationCheckerTranslations = null;
    @Unique
    private ArrayList<Map.Entry<String, String>> lib99j$requiredModTranslationCheckerTranslations = null;
    @Unique
    private Consumer<ArrayList<String>> lib99j$modTranslationCheckerOutput = null;

    public ServerPlayerEntityMixin(World world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Shadow
    public abstract OptionalInt openHandledScreen(@Nullable NamedScreenHandlerFactory factory);

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
        if (this.lib99j$holder == null) {
            this.lib99j$holder = new ElementHolder();
            this.lib99j$holder.setAttachment(new EntityAttachment(this.lib99j$holder, getPlayer(), true));
            this.lib99j$holder.startWatching(getPlayer());
        }
        if (this.lib99j$horse != null && this.lib99j$cameraPoint != null) {
            this.lib99j$horse.setPitch(this.getPitch());
            this.lib99j$horse.setYaw(this.getYaw());
            this.lib99j$cameraPoint.setPitch(this.getPitch());
            this.lib99j$cameraPoint.setYaw(this.getYaw());
        }
    }

    @Override
    public void setYaw(float yaw) {
        super.setYaw(yaw);
        if (this.lib99j$horse != null && this.lib99j$cameraPoint != null) {
            this.lib99j$horse.setPitch(this.getPitch());
            this.lib99j$horse.setYaw(this.getYaw());
            this.lib99j$cameraPoint.setPitch(this.getPitch());
            this.lib99j$cameraPoint.setYaw(this.getYaw());
        }
    }

    @Override
    public void setPitch(float pitch) {
        super.setPitch(pitch);
        if (this.lib99j$horse != null && this.lib99j$cameraPoint != null) {
            this.lib99j$horse.setPitch(this.getPitch());
            this.lib99j$horse.setYaw(this.getYaw());
            this.lib99j$cameraPoint.setPitch(this.getPitch());
            this.lib99j$cameraPoint.setYaw(this.getYaw());
        }
    }

    @Inject(method = "rotate", at = @At("HEAD"))
    public void rotate(float yaw, float pitch, CallbackInfo ci) {
        if (this.lib99j$horse != null && this.lib99j$cameraPoint != null) {
            this.lib99j$horse.setPitch(pitch);
            this.lib99j$horse.setYaw(yaw);
            this.lib99j$cameraPoint.setPitch(pitch);
            this.lib99j$cameraPoint.setYaw(yaw);
        }
    }

    @Override
    public void lib99j$lockCamera() {
        float yaw = getPlayer().getYaw();
        float pitch = getPlayer().getPitch();
        getPlayer().setCameraEntity(getPlayer());
        this.lib99j$cameraPoint = new BlockDisplayElement();
        this.lib99j$cameraPoint.setYaw(yaw);
        this.lib99j$cameraPoint.setPitch(pitch);
        this.lib99j$cameraPoint.setOffset(new Vec3d(0, getPlayer().getStandingEyeHeight(), 0));
        this.lib99j$cameraPoint.setInvisible(true);
        this.lib99j$holder.addElement(this.lib99j$cameraPoint);
        this.lib99j$horse = new SimpleEntityElement(EntityType.HORSE) {

        };
        HorseEntity testEntity = new HorseEntity(EntityType.HORSE, getPlayer().getWorld());
        this.lib99j$horse.setInvisible(true);
        this.lib99j$horse.setOffset(new Vec3d(0, getPlayer().getStandingEyeHeight(), 0).subtract(testEntity.getPassengerRidingPos(getPlayer()).getY()).subtract(testEntity.getPos()));
        this.lib99j$horse.setYaw(yaw);
        this.lib99j$horse.setPitch(pitch);
        this.lib99j$holder.addElement(lib99j$horse);

        getPlayer().networkHandler.sendPacket(VirtualEntityUtils.createSetCameraEntityPacket(this.lib99j$cameraPoint.getEntityId()));
        getPlayer().networkHandler.sendPacket(VirtualEntityUtils.createRidePacket(lib99j$horse.getEntityId(), IntList.of(getPlayer().getId())));
        getPlayer().networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, GameMode.SPECTATOR.getIndex()));
        getPlayer().networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(getPlayer().getId(), List.of(DataTracker.SerializedEntry.of(EntityTrackedData.POSE, EntityPose.STANDING))));
    }

    @Override
    public void lib99j$unlockCamera() {
        getPlayer().networkHandler.sendPacket(new SetCameraEntityS2CPacket(this.cameraEntity == null ? getPlayer() : this.cameraEntity));
        getPlayer().networkHandler.sendPacket(VirtualEntityUtils.createRidePacket(lib99j$horse.getEntityId(), IntList.of()));
        getPlayer().networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, getPlayer().interactionManager.getGameMode().getIndex()));
        getPlayer().networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(getPlayer().getId(), List.of(DataTracker.SerializedEntry.of(EntityTrackedData.POSE, getPlayer().getPose()))));
        this.lib99j$holder.removeElement(this.lib99j$cameraPoint);
        this.lib99j$holder.removeElement(this.lib99j$horse);
        this.lib99j$cameraPoint = null;
        this.lib99j$horse = null;
    }

    @Override
    public void lib99j$addModTranslationCheckerTranslation(String entry) {
        if (this.lib99j$modTranslationCheckerTranslations == null) this.lib99j$modTranslationCheckerTranslations = new ArrayList<>();
        this.lib99j$modTranslationCheckerTranslations.add(entry);
    }

    @Override
    public void lib99j$setNeededModCheckerTranslations(ArrayList<Map.Entry<String, String>> translations) {
        this.lib99j$requiredModTranslationCheckerTranslations = translations;
    }

    @Override
    public void lib99j$setModCheckerOutput(Consumer<ArrayList<String>> output) {
        this.lib99j$modTranslationCheckerOutput = output;
    }

    @Override
    public void lib99j$runModCheckerOutput() {
        ArrayList<String> mods = new ArrayList<>();
        for (Map.Entry<String, String> entry : lib99j$requiredModTranslationCheckerTranslations) {
            String mod = entry.getKey();
            String missingText = entry.getValue();

            boolean isMissing = false;
            for (String text : lib99j$modTranslationCheckerTranslations) {
                if (text.contains(missingText)) {
                    isMissing = true;
                    break;
                }
            }

            if (!isMissing) {
                mods.add(mod);
            }
        }
        this.lib99j$modTranslationCheckerOutput.accept(mods);

        this.lib99j$modTranslationCheckerOutput = null;
        this.lib99j$modTranslationCheckerTranslations = null;
        this.lib99j$requiredModTranslationCheckerTranslations = null;
    }
}