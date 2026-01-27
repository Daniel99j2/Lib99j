package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.explosion.ExplosionImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings({"unused"})
public class VFXUtils {
    private static final List<CameraShakeInstance> cameraShakeInstances = new ArrayList<>();
    private static final List<GenericScreenEffectInstance> genericScreenEffectInstances = new ArrayList<>();

    @ApiStatus.Internal
    public static void tick() {
        if (!cameraShakeInstances.isEmpty()) {
            Iterator<CameraShakeInstance> iterator = cameraShakeInstances.iterator();
            while (iterator.hasNext()) {
                CameraShakeInstance instance = iterator.next();
                ServerPlayerEntity player = instance.player;
                player.rotate(-instance.lastYaw, true, -instance.lastPitch, true);
                instance.lastYaw = NumberUtils.getRandomFloat(-instance.strength, instance.strength);
                instance.lastPitch = NumberUtils.getRandomFloat(-instance.strength, instance.strength);
                player.rotate(instance.lastYaw, true, instance.lastPitch, true);
                instance.remainingTicks--;

                if (instance.remainingTicks == 0) {
                    iterator.remove();
                }
            }
        }

        if (!genericScreenEffectInstances.isEmpty()) {
            Iterator<GenericScreenEffectInstance> iterator1 = genericScreenEffectInstances.iterator();
            while (iterator1.hasNext()) {
                GenericScreenEffectInstance instance = iterator1.next();
                ServerPlayerEntity player = instance.player;
                if (instance.remainingTicks != -1) instance.remainingTicks--;

                if (instance.remainingTicks == 0) {
                    endGenericScreenEffect(instance);
                    iterator1.remove();
                }
            }
        }
    }

    public static void shake(ServerPlayerEntity player, int ticks, float strength, Identifier source) {
        CameraShakeInstance instance = new CameraShakeInstance(player, ticks, strength, source);
        cameraShakeInstances.add(instance);
    }

    public static void stopShaking(ServerPlayerEntity player, Identifier source) {
        cameraShakeInstances.removeIf(instance -> instance.player == player && instance.source == source);
    }

    public static void stopAllShaking(ServerPlayerEntity player) {
        cameraShakeInstances.removeIf(instance -> instance.player == player);
    }

    private static void initGenericScreenEffect(GenericScreenEffectInstance instance) {
        switch (instance.effect) {
            case RED_TINT -> {
                WorldBorder customWorldBorder = new WorldBorder();
                customWorldBorder.setWarningBlocks(1000000000);
                instance.player.networkHandler.sendPacket(new WorldBorderWarningBlocksChangedS2CPacket(customWorldBorder));
            }
            case SNOW ->
                    instance.player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(instance.player.getId(), List.of(DataTracker.SerializedEntry.of(Entity.FROZEN_TICKS, 2147483646))));
            case FIRE -> {
                byte flags = instance.player.getDataTracker().get(EntityTrackedData.FLAGS);
                byte updatedFlags = (byte) (flags | (1 << EntityTrackedData.ON_FIRE_FLAG_INDEX));
                instance.player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(instance.player.getId(), List.of(DataTracker.SerializedEntry.of(EntityTrackedData.FLAGS, updatedFlags))));
            }
            case NAUSEA ->
                    instance.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(instance.player.getId(), new StatusEffectInstance(StatusEffects.NAUSEA, -1, 0, true, false), false));
            case BLACK_HEARTS ->
                    instance.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(instance.player.getId(), new StatusEffectInstance(StatusEffects.WITHER, -1, 0, true, false), false));
            case GREEN_HEARTS ->
                    instance.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(instance.player.getId(), new StatusEffectInstance(StatusEffects.POISON, -1, 0, true, false), false));
            case GREEN_HUNGER ->
                    instance.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(instance.player.getId(), new StatusEffectInstance(StatusEffects.HUNGER, -1, 0, true, false), false));
            case LOCK_CAMERA_AND_POS -> ((Lib99jPlayerUtilController) instance.getPlayer()).lib99j$lockCamera();
            case NIGHT_VISION ->
                    instance.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(instance.player.getId(), new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0, true, false), false));
            case BLINDNESS ->
                    instance.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(instance.player.getId(), new StatusEffectInstance(StatusEffects.BLINDNESS, -1, 0, true, false), false));
        }
    }

    private static void endGenericScreenEffect(GenericScreenEffectInstance instance) {
        instance.markFinished();
        if (!hasGenericScreenEffect(instance.player, instance.effect)) {
            switch (instance.effect) {
                case RED_TINT -> {
                    instance.player.networkHandler.sendPacket(new WorldBorderWarningBlocksChangedS2CPacket(instance.getPlayer().getEntityWorld().getWorldBorder()));
                }
                case SNOW ->
                        instance.player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(instance.player.getId(), List.of(DataTracker.SerializedEntry.of(Entity.FROZEN_TICKS, 0))));
                case FIRE -> {
                    byte flags = instance.player.getDataTracker().get(EntityTrackedData.FLAGS);
                    byte updatedFlags = (byte) (flags & ~(1 << EntityTrackedData.ON_FIRE_FLAG_INDEX));
                    instance.player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(instance.player.getId(), List.of(DataTracker.SerializedEntry.of(EntityTrackedData.FLAGS, updatedFlags))));
                }
                case NAUSEA -> {
                    if (instance.player.hasStatusEffect(StatusEffects.NAUSEA)) {
                        instance.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(instance.player.getId(), Objects.requireNonNull(instance.player.getStatusEffect(StatusEffects.NAUSEA)), false));
                    } else {
                        instance.player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(instance.player.getId(), StatusEffects.NAUSEA));
                    }
                }
                case BLACK_HEARTS -> {
                    if (instance.player.hasStatusEffect(StatusEffects.WITHER)) {
                        instance.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(instance.player.getId(), Objects.requireNonNull(instance.player.getStatusEffect(StatusEffects.WITHER)), false));
                    } else {
                        instance.player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(instance.player.getId(), StatusEffects.WITHER));
                    }
                }
                case GREEN_HEARTS -> {
                    if (instance.player.hasStatusEffect(StatusEffects.POISON)) {
                        instance.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(instance.player.getId(), Objects.requireNonNull(instance.player.getStatusEffect(StatusEffects.POISON)), false));
                    } else {
                        instance.player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(instance.player.getId(), StatusEffects.POISON));
                    }
                }
                case GREEN_HUNGER -> {
                    if (instance.player.hasStatusEffect(StatusEffects.HUNGER)) {
                        instance.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(instance.player.getId(), Objects.requireNonNull(instance.player.getStatusEffect(StatusEffects.HUNGER)), false));
                    } else {
                        instance.player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(instance.player.getId(), StatusEffects.HUNGER));
                    }
                }
                case LOCK_CAMERA_AND_POS -> ((Lib99jPlayerUtilController) instance.getPlayer()).lib99j$unlockCamera();
                case BLINDNESS -> {
                    if (instance.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
                        instance.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(instance.player.getId(), Objects.requireNonNull(instance.player.getStatusEffect(StatusEffects.BLINDNESS)), false));
                    } else {
                        instance.player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(instance.player.getId(), StatusEffects.BLINDNESS));
                    }
                }
                case NIGHT_VISION -> {
                    if (instance.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                        instance.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(instance.player.getId(), Objects.requireNonNull(instance.player.getStatusEffect(StatusEffects.NIGHT_VISION)), false));
                    } else {
                        instance.player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(instance.player.getId(), StatusEffects.NIGHT_VISION));
                    }
                }
            }
        }
    }

    public static void addGenericScreenEffect(ServerPlayerEntity player, int ticks, GENERIC_SCREEN_EFFECT effect, Identifier source) {
        GenericScreenEffectInstance instance = new GenericScreenEffectInstance(player, ticks, effect, source);
        genericScreenEffectInstances.add(instance);
    }

    public static void setCameraPos(ServerPlayerEntity player, Vec3d pos) {
        if(!hasGenericScreenEffect(player, GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS)) throw new IllegalStateException("Camera must be locked");
        ((Lib99jPlayerUtilController) player).lib99j$setCameraPos(pos);
    }

    public static void setCameraPitch(ServerPlayerEntity player, float pitch) {
        if(!hasGenericScreenEffect(player, GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS)) throw new IllegalStateException("Camera must be locked");
        ((Lib99jPlayerUtilController) player).lib99j$setCameraPitch(pitch);
    }

    public static void setCameraYaw(ServerPlayerEntity player, float yaw) {
        if(!hasGenericScreenEffect(player, GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS)) throw new IllegalStateException("Camera must be locked");
        ((Lib99jPlayerUtilController) player).lib99j$setCameraYaw(yaw);
    }

    public static void addGenericScreenEffectUnlessExists(ServerPlayerEntity player, int ticks, GENERIC_SCREEN_EFFECT effect, Identifier source) {
        if (!hasGenericScreenEffectSource(player, effect, source))
            addGenericScreenEffect(player, ticks, effect, source);
    }

    public static void removeGenericScreenEffect(ServerPlayerEntity player, Identifier source) {
        genericScreenEffectInstances.removeIf(instance -> {
            if (instance.player == player && instance.source.toString().equals(source.toString())) {
                endGenericScreenEffect(instance);
                return true;
            }
            return false;
        });
    }

    public static void clearGenericScreenEffects(ServerPlayerEntity player) {
        genericScreenEffectInstances.removeIf(instance -> {
            if (instance.player == player) {
                endGenericScreenEffect(instance);
                return true;
            }
            return false;
        });
    }

    @ApiStatus.Internal
    public static boolean hasEffectEffect(ServerPlayerEntity player) {
        return genericScreenEffectInstances.stream().anyMatch(instance -> instance.player == player  && !instance.isFinished() && (
                instance.effect == GENERIC_SCREEN_EFFECT.NIGHT_VISION ||
                        instance.effect == GENERIC_SCREEN_EFFECT.BLINDNESS ||
                        instance.effect == GENERIC_SCREEN_EFFECT.BLACK_HEARTS ||
                        instance.effect == GENERIC_SCREEN_EFFECT.GREEN_HEARTS ||
                        instance.effect == GENERIC_SCREEN_EFFECT.GREEN_HUNGER ||
                        instance.effect == GENERIC_SCREEN_EFFECT.NAUSEA
        ));
    }

    public static boolean hasGenericScreenEffect(ServerPlayerEntity player, GENERIC_SCREEN_EFFECT effect) {
        return genericScreenEffectInstances.stream().anyMatch(instance -> instance.player == player && instance.effect == effect && !instance.isFinished());
    }

    public static boolean hasGenericScreenEffectSource(ServerPlayerEntity player, GENERIC_SCREEN_EFFECT effect, Identifier source) {
        return genericScreenEffectInstances.stream().anyMatch(instance -> instance.player == player && instance.effect == effect && instance.source == source && !instance.isFinished());
    }

    public static List<GenericScreenEffectInstance> getGenericScreenEffectInstances() {
        return genericScreenEffectInstances;
    }

    public static void clientSideExplode(
            ArrayList<ServerPlayerEntity> players,
            @Nullable ExplosionBehavior behavior,
            double x,
            double y,
            double z,
            float power,
            boolean createFire,
            ParticleEffect smallParticle,
            ParticleEffect largeParticle,
            RegistryEntry<SoundEvent> soundEvent,
            boolean ignoreResistance) {
        Explosion.DestructionType destructionType = Explosion.DestructionType.DESTROY;
        Vec3d vec3d = new Vec3d(x, y, z);
        FakeExplosionImpl explosionImpl = new FakeExplosionImpl(players, (ServerWorld) players.getFirst().getEntityWorld(), behavior, vec3d, power, createFire, destructionType, ignoreResistance);
        explosionImpl.explode();
        ParticleEffect particleEffect = explosionImpl.isSmall() ? smallParticle : largeParticle;

        for (ServerPlayerEntity serverPlayerEntity : players) {
            if (serverPlayerEntity.squaredDistanceTo(vec3d) < 4096.0) {
                Optional<Vec3d> optional = Optional.ofNullable(explosionImpl.getKnockbackByPlayer().get(serverPlayerEntity));
                serverPlayerEntity.networkHandler.sendPacket(new ExplosionS2CPacket(vec3d, 1, 0, optional, particleEffect, soundEvent, Pool.empty()));
            }
        }
    }

    public static void fireworkExplode(List<ServerPlayerEntity> players, List<FireworkExplosionComponent> explosions, Vec3d pos, Vec3d velocity) {
        fireworkExplode(players, explosions, pos, velocity, 123456, 0);
    }

    public static void fireworkExplode(List<ServerPlayerEntity> players, List<FireworkExplosionComponent> explosions, Vec3d pos, Vec3d velocity, float pitch, float yaw) {
        boolean angle = pitch != 123456;
        ItemStack fireworkStack = Items.FIREWORK_ROCKET.getDefaultStack();
        fireworkStack.set(DataComponentTypes.FIREWORKS, new FireworksComponent(1, explosions));
        fireworkStack.set(DataComponentTypes.ITEM_MODEL, Identifier.ofVanilla("air"));

        int id = VirtualEntityUtils.requestEntityId();
        BundleS2CPacket bundle = new BundleS2CPacket(List.of(new EntitySpawnS2CPacket(id, UUID.randomUUID(),
                        pos.x, pos.y, pos.z, angle ? pitch : 0,  angle ? yaw : 0,
                        EntityType.FIREWORK_ROCKET, 0, Vec3d.ZERO, 0d),
                new EntityTrackerUpdateS2CPacket(id, List.of(
                        DataTracker.SerializedEntry.of(FireworkRocketEntity.ITEM, fireworkStack),
                        DataTracker.SerializedEntry.of(FireworkRocketEntity.SHOT_AT_ANGLE, angle)
                )),
                new EntityStatusS2CPacket(EntityUtils.fakeEntityFromId(id), (byte)17),
                new EntitiesDestroyS2CPacket(id)
        ));

        for(ServerPlayerEntity player : players) {
            player.networkHandler.sendPacket(bundle);
        };
    }

    public enum GENERIC_SCREEN_EFFECT {
        RED_TINT,
        SNOW,
        FIRE,
        NAUSEA,
        BLACK_HEARTS,
        GREEN_HEARTS,
        GREEN_HUNGER,
        BLINDNESS,
        NIGHT_VISION,
        LOCK_CAMERA_AND_POS
    }

    public static class CameraShakeInstance {
        ServerPlayerEntity player;
        int ticks;
        float strength;
        int remainingTicks;
        Identifier source;
        float lastYaw;
        float lastPitch;

        private CameraShakeInstance(ServerPlayerEntity player, int ticks, float strength, Identifier source) {
            this.ticks = ticks;
            this.remainingTicks = ticks;
            this.player = player;
            this.strength = strength;
            this.source = source;
        }
    }

    public static class GenericScreenEffectInstance {
        ServerPlayerEntity player;
        int ticks;
        int remainingTicks;
        GENERIC_SCREEN_EFFECT effect;
        Identifier source;
        List<Packet<ClientPlayPacketListener>> queuedPackets = new ArrayList<>();
        boolean finished;

        private GenericScreenEffectInstance(ServerPlayerEntity player, int ticks, GENERIC_SCREEN_EFFECT effect, Identifier source) {
            this.ticks = ticks;
            this.remainingTicks = ticks;
            this.player = player;
            this.effect = effect;
            this.source = source;
            initGenericScreenEffect(this);
        }

        public ServerPlayerEntity getPlayer() {
            return player;
        }

        public GENERIC_SCREEN_EFFECT getEffect() {
            return effect;
        }

        public List<Packet<ClientPlayPacketListener>> getQueuedPackets() {
            return queuedPackets;
        }

        public void markFinished() {
            this.finished = true;
        }

        public boolean isFinished() {
            return finished;
        }
    }

    private static class FakeExplosionImpl extends ExplosionImpl {
        ArrayList<ServerPlayerEntity> players;

        public FakeExplosionImpl(ArrayList<ServerPlayerEntity> players, ServerWorld world, @Nullable ExplosionBehavior behavior, Vec3d pos, float power, boolean createFire, DestructionType destructionType, boolean ignoreResistance) {
            super(world, null, null, new ExplosionBehavior() {
                @Override
                public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
                    return ignoreResistance ? Optional.of(0f) : super.getBlastResistance(explosion, world, pos, blockState, fluidState);
                }
            }, pos, power, createFire, destructionType);
            this.players = players;
        }

        @Override
        public int explode() {
            List<BlockPos> list = this.getBlocksToDestroy();
            for (BlockPos blockPos : list) {
                if (this.shouldDestroyBlocks()) {
                    for (ServerPlayerEntity player : players) {
                        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(blockPos, Blocks.AIR.getDefaultState()));
                    }
                }

                if (this.createFire && this.getWorld().random.nextInt(3) == 0 && this.getWorld().getBlockState(blockPos).isAir() && !list.contains(blockPos.down()) && this.getWorld().getBlockState(blockPos.down()).isOpaqueFullCube()) {
                    for (ServerPlayerEntity player : players) {
                        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(blockPos, Blocks.FIRE.getDefaultState()));
                    }
                }
            }
            return 0;
        }
    }
}
