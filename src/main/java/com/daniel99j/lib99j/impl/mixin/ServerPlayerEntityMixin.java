package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.daniel99j.lib99j.ponder.impl.PonderManager;
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
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
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

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin
        extends PlayerEntity implements PolymerEntity, Lib99jPlayerUtilController {
    @Shadow
    private @Nullable Entity cameraEntity;
    @Unique
    private ElementHolder lib99j$holder;
    @Unique
    private DisplayElement lib99j$cameraPoint;
    @Unique
    private final ArrayList<GuiUtils.PlayerTranslationCheckerData> lib99j$activeTranslationCheckers = new ArrayList<>();
    @Unique
    private int lib99j$modTranslationCheckerTimeout = 0;

    public ServerPlayerEntityMixin(World world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Shadow
    public abstract OptionalInt openHandledScreen(@Nullable NamedScreenHandlerFactory factory);

    @Inject(method = "onDeath", at = @At("TAIL"))
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
        if(lib99j$isModCheckerRunning()) {
            this.lib99j$modTranslationCheckerTimeout--;
            if (this.lib99j$modTranslationCheckerTimeout == 0 && this.lib99j$getActiveTranslationChecker().remainingTries().get().intValue() == 0) {
                Lib99j.LOGGER.warn("Translation check for " + this.getPlayer().getName() + " timed out too many times");
                this.lib99j$getActiveTranslationChecker().output().accept(new GuiUtils.PlayerTranslationsResponse(false, true, new ArrayList<>(), new ArrayList<>()));
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
        float yaw = getPlayer().getYaw();
        float pitch = getPlayer().getPitch();
        this.lib99j$cameraPoint = new BlockDisplayElement() {
            @Override
            public Vec3d getOffset() {
                return super.getOffset().subtract(getPlayer().getEntityPos());
            }
        };
        this.lib99j$cameraPoint.setYaw(yaw);
        this.lib99j$cameraPoint.setPitch(pitch);
        this.lib99j$cameraPoint.setOffset(new Vec3d(0, getPlayer().getStandingEyeHeight(), 0));
        this.lib99j$cameraPoint.setInvisible(true);
        this.lib99j$holder.addElement(this.lib99j$cameraPoint);
        getPlayer().networkHandler.sendPacket(VirtualEntityUtils.createSetCameraEntityPacket(this.lib99j$cameraPoint.getEntityId()));
        getPlayer().networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, GameMode.SPECTATOR.getIndex()));
        lib99j$setCameraPos(getPlayer().getEyePos());
    }

    @Override
    public void lib99j$setCameraPos(Vec3d pos) {
        this.lib99j$cameraPoint.setOffset(pos);
    }

    @Override
    public void lib99j$setCameraPitch(float pitch) {
        this.lib99j$cameraPoint.setPitch(pitch);
    }

    @Override
    public void lib99j$setCameraYaw(float yaw) {
        this.lib99j$cameraPoint.setYaw(yaw);
    }

    @Override
    public void lib99j$unlockCamera() {
        getPlayer().networkHandler.sendPacket(new SetCameraEntityS2CPacket(this.cameraEntity == null ? getPlayer() : this.cameraEntity));
        getPlayer().networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, getPlayer().interactionManager.getGameMode().getIndex()));
        getPlayer().networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(getPlayer().getId(), List.of(DataTracker.SerializedEntry.of(EntityTrackedData.POSE, getPlayer().getPose()))));
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

        for (Map.Entry<String, String> entry : lib99j$getActiveTranslationChecker().translations()) {
            String mod = entry.getKey();
            String missingText = entry.getValue();

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
        this.lib99j$getActiveTranslationChecker().output().accept(new GuiUtils.PlayerTranslationsResponse(workedAtAll, false, misses, matches));

        this.lib99j$activeTranslationCheckers.removeFirst();
        if(!this.lib99j$activeTranslationCheckers.isEmpty()) this.lib99j$checkMods(this.lib99j$getActiveTranslationChecker());
    }

    @Unique
    private void lib99j$checkMods(GuiUtils.PlayerTranslationCheckerData data) {
        ServerPlayerEntity player = getPlayer();
        if(data.remainingTries().intValue() == 5) this.lib99j$modTranslationCheckerTimeout = 5;

        List<Map.Entry<String, String>> entries = data.translations();
        int total = entries.size();
        int lines = 3;
        int count = (int) Math.ceil((double) total / lines);
        BlockPos pos = new BlockPos(player.getBlockPos().getX(), player.getEntityWorld().getBottomY(), player.getBlockPos().getZ());

        for (int i = 0; i < count; i++) {
            NbtCompound nbt = new NbtCompound();
            SignText text = new SignText();

            for (int line = 0; line < lines; line++) {
                int index = i * lines + line;
                if (index < total) {
                    Map.Entry<String, String> entry = entries.get(index);
                    text = text.withMessage(line, Text.translatable(entry.getValue()));
                }
            }

            if (i == count - 1) text = text.withMessage(3, Text.literal("lib99j$final"));
            else text = text.withMessage(3, Text.literal("lib99j$checker"));

            DynamicOps<NbtElement> dynamicOps = player.getRegistryManager().getOps(NbtOps.INSTANCE);
            nbt.put("front_text", SignText.CODEC, dynamicOps, text);
            nbt.put("back_text", SignText.CODEC, dynamicOps, new SignText());
            nbt.putBoolean("is_waxed", false);

            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, Blocks.OAK_SIGN.getDefaultState()));
            player.networkHandler.sendPacket(ClientboundBlockEntityDataPacketAccessor.createBlockEntityUpdateS2CPacket(pos, BlockEntityType.SIGN, nbt));
            player.networkHandler.sendPacket(new SignEditorOpenS2CPacket(pos, true));
            player.networkHandler.sendPacket(new CloseScreenS2CPacket(0));
        }
        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, player.getEntityWorld().getBlockState(pos)));
        if (player.getEntityWorld().getBlockEntity(pos) != null)
            player.networkHandler.sendPacket(BlockEntityUpdateS2CPacket.create(player.getEntityWorld().getBlockEntity(pos)));
    }

    @Override
    public boolean lib99j$isModCheckerRunning() {
        return !this.lib99j$activeTranslationCheckers.isEmpty();
    }

    @Override
    public Vec3d lib99j$getCameraWorldPos() {
        return this.lib99j$cameraPoint.getOffset();
    }
}