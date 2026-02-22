package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.daniel99j.lib99j.impl.PlayerElementHolder;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DynamicOps;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.mixin.block.ClientboundBlockEntityDataPacketAccessor;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Consumer;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin
        extends Player implements PolymerEntity, Lib99jPlayerUtilController, PlayerElementHolder {
    @Shadow
    private @Nullable Entity camera;
    @Unique
    private ElementHolder lib99j$holder;
    @Unique
    private DisplayElement lib99j$cameraPoint;
    @Unique
    private final ArrayList<GuiUtils.PlayerTranslationCheckerData> lib99j$activeTranslationCheckers = new ArrayList<>();
    @Unique
    private int lib99j$modTranslationCheckerTimeout = 0;

    public ServerPlayerMixin(Level world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Shadow
    public abstract OptionalInt openMenu(@Nullable MenuProvider factory);

    @Inject(method = "die", at = @At("TAIL"))
    public void deathEvent(DamageSource damageSource, CallbackInfo ci) {
        VFXUtils.stopAllShaking(getPlayer());
        VFXUtils.clearGenericScreenEffects(getPlayer());
    }
    @Unique
    private ServerPlayer getPlayer() {
        Player player = this;
        return (ServerPlayer) player;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        //Might be janky but it works.
        //The constructor doesn't work because the player isn't initialised
        if (this.lib99j$holder == null) {
            this.lib99j$holder = new ElementHolder();
            this.lib99j$holder.setAttachment(new EntityAttachment(this.lib99j$holder, getPlayer(), true));
            this.lib99j$holder.startWatching(getPlayer());
        }
        if(lib99j$isModCheckerRunning()) {
            this.lib99j$modTranslationCheckerTimeout--;
            if (this.lib99j$modTranslationCheckerTimeout == 0 && this.lib99j$getActiveTranslationChecker().remainingTries().get().intValue() <= 0) {
                Lib99j.LOGGER.warn("Translation check for " + this.getPlayer().getName() + " timed out too many times");
                this.lib99j$getActiveTranslationChecker().output().accept(new GuiUtils.PlayerTranslationsResponse(false, true, new ArrayList<>(), new ArrayList<>()));
                this.lib99j$activeTranslationCheckers.removeFirst();
            } else {
                Lib99j.LOGGER.warn("Translation check for " + this.getPlayer().getName() + " timed out (tries remaining: " + this.lib99j$getActiveTranslationChecker().remainingTries().get() + ")");
                this.lib99j$getActiveTranslationChecker().remainingTries().setValue(this.lib99j$getActiveTranslationChecker().remainingTries().get().intValue() - 1);
                this.lib99j$modTranslationCheckerTimeout = 5;
            }
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true, order = 10000)
    private void disableAttacking(Entity target, CallbackInfo ci) {
        if (VFXUtils.hasGenericScreenEffect(getPlayer(), VFXUtils.GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS)) {
            ci.cancel();
        }
    }

    @Override
    public void lib99j$lockCamera() {
        if (this.lib99j$cameraPoint != null) return;
        float yaw = getPlayer().getYRot();
        float pitch = getPlayer().getXRot();
        this.lib99j$cameraPoint = new BlockDisplayElement() {
            @Override
            public Vec3 getOffset() {
                return super.getOffset().subtract(getPlayer().position());
            }
        };
        this.lib99j$cameraPoint.setYaw(yaw);
        this.lib99j$cameraPoint.setPitch(pitch);
        this.lib99j$cameraPoint.setOffset(new Vec3(0, getPlayer().getEyeHeight(), 0));
        this.lib99j$cameraPoint.setInvisible(true);
        this.lib99j$holder.addElement(this.lib99j$cameraPoint);
        getPlayer().connection.send(VirtualEntityUtils.createSetCameraEntityPacket(this.lib99j$cameraPoint.getEntityId()));
        getPlayer().connection.send(VirtualEntityUtils.createRidePacket(this.lib99j$cameraPoint.getEntityId(), IntList.of(getId())));
        getPlayer().connection.send(new ClientboundSetSubtitleTextPacket(Component.empty()));
        getPlayer().connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, GameType.SPECTATOR.getId()));
        lib99j$setCameraPos(getPlayer().getEyePosition());
    }

    @Override
    public void lib99j$setCameraInterpolationTime(int time) {
        if (this.lib99j$cameraPoint == null) return;
        this.lib99j$cameraPoint.setInterpolationDuration(time);
        this.lib99j$cameraPoint.setInterpolationDuration(time);
    }

    @Override
    public void lib99j$setCameraPos(Vec3 pos) {
        if (this.lib99j$cameraPoint == null) return;
        this.lib99j$cameraPoint.setOffset(pos);
    }

    @Override
    public void lib99j$setCameraPitch(float pitch) {
        if (this.lib99j$cameraPoint == null) return;
        this.lib99j$cameraPoint.setPitch(pitch);
    }

    @Override
    public void lib99j$setCameraYaw(float yaw) {
        if (this.lib99j$cameraPoint == null) return;
        this.lib99j$cameraPoint.setYaw(yaw);
    }

    @Override
    public void lib99j$unlockCamera() {
        if (this.lib99j$cameraPoint == null) return;
        getPlayer().connection.send(new Lib99j.BypassPacket(new ClientboundSetCameraPacket(this.camera == null ? getPlayer() : this.camera)));
        getPlayer().connection.send(new Lib99j.BypassPacket(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, getPlayer().gameMode.getGameModeForPlayer().getId())));
        getPlayer().connection.send(new Lib99j.BypassPacket(new ClientboundSetEntityDataPacket(getPlayer().getId(), List.of(SynchedEntityData.DataValue.create(EntityTrackedData.POSE, getPlayer().getPose())))));
        this.lib99j$holder.removeElement(this.lib99j$cameraPoint);
        this.lib99j$cameraPoint = null;
    }

    @Override
    public void lib99j$addTranslationChecker(Map<String, String> modTranslations, Consumer<GuiUtils.PlayerTranslationsResponse> output) {
        Map<String, String> modifiedTranslations = new HashMap<>(modTranslations);
        modifiedTranslations.put("lib99j", "lib99j.translations_available");
        this.lib99j$activeTranslationCheckers.add(new GuiUtils.PlayerTranslationCheckerData(modifiedTranslations.entrySet().stream().toList(), new ArrayList<>(), output, new MutableInt(5)));

        if(this.lib99j$activeTranslationCheckers.size() == 1) this.lib99j$checkMods(this.lib99j$getActiveTranslationChecker());
    }

    @Override
    public GuiUtils.PlayerTranslationCheckerData lib99j$getActiveTranslationChecker() {
        return this.lib99j$activeTranslationCheckers.isEmpty() ? null : this.lib99j$activeTranslationCheckers.getFirst();
    }

    @Override
    public void lib99j$finishCurrentModChecker() {
        if(lib99j$getActiveTranslationChecker() == null) return;

        ArrayList<String> matches = new ArrayList<>();
        ArrayList<String> misses = new ArrayList<>();
        boolean workedAtAll = false;

        //loop through translations
        for (Map.Entry<String, String> entry : lib99j$getActiveTranslationChecker().translations()) {
            String mod = entry.getKey();
            String missingText = entry.getValue();

            //if the results contains the raw string, its not valid for them
            boolean isMissing = false;
            for (String text : lib99j$getActiveTranslationChecker().results()) {
                if (text.contains(missingText)) {
                    isMissing = true;
                    break;
                }
            }

            if (isMissing) {
                misses.add(mod);
            } else {
                if (Objects.equals(mod, "lib99j"))
                    workedAtAll = true;
                else matches.add(mod);
            }
        }
        this.lib99j$getActiveTranslationChecker().output().accept(new GuiUtils.PlayerTranslationsResponse(!workedAtAll, false, misses, matches));

        this.lib99j$activeTranslationCheckers.removeFirst();
        if(!this.lib99j$activeTranslationCheckers.isEmpty()) this.lib99j$checkMods(this.lib99j$getActiveTranslationChecker());
    }

    @Unique
    private void lib99j$checkMods(GuiUtils.PlayerTranslationCheckerData data) {
        ServerPlayer player = getPlayer();
        if(data.remainingTries().intValue() == 5) this.lib99j$modTranslationCheckerTimeout = 5;

        List<Map.Entry<String, String>> entries = data.translations();
        int total = entries.size();
        int lines = 3;
        int count = (int) Math.ceil((double) total / lines);
        BlockPos pos = new BlockPos(player.blockPosition().getX(), 0, player.blockPosition().getZ());

        for (int i = 0; i < count; i++) {
            CompoundTag nbt = new CompoundTag();
            SignText text = new SignText();

            for (int line = 0; line < lines; line++) {
                int index = i * lines + line;
                if (index < total) {
                    Map.Entry<String, String> entry = entries.get(index);
                    text = text.setMessage(line, Component.translatable(entry.getValue()));
                }
            }

            if (i == count - 1) text = text.setMessage(3, Component.literal("lib99j$final"));
            else text = text.setMessage(3, Component.literal("lib99j$checker"));

            DynamicOps<Tag> dynamicOps = player.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            nbt.store("front_text", SignText.DIRECT_CODEC, dynamicOps, text);
            nbt.store("back_text", SignText.DIRECT_CODEC, dynamicOps, new SignText());
            nbt.putBoolean("is_waxed", false);

            player.connection.send(new ClientboundBlockUpdatePacket(pos, Blocks.OAK_SIGN.defaultBlockState()));
            player.connection.send(ClientboundBlockEntityDataPacketAccessor.createBlockEntityUpdateS2CPacket(pos, BlockEntityType.SIGN, nbt));
            player.connection.send(new ClientboundOpenSignEditorPacket(pos, true));
            player.connection.send(new ClientboundContainerClosePacket(0));
        }
        player.connection.send(new ClientboundBlockUpdatePacket(pos, player.level().getBlockState(pos)));
        if (player.level().getBlockEntity(pos) != null)
            player.connection.send(ClientboundBlockEntityDataPacket.create(player.level().getBlockEntity(pos)));
    }

    @Override
    public boolean lib99j$isModCheckerRunning() {
        return !this.lib99j$activeTranslationCheckers.isEmpty();
    }

    @Override
    public Vec3 lib99j$getCameraWorldPos() {
        return this.lib99j$cameraPoint.getOffset();
    }

    @Override
    public ElementHolder lib99j$getPlayerElementHolder() {
        return this.lib99j$holder;
    }
}