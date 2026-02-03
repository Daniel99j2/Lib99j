package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
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
                ServerPlayer player = instance.player;
                player.forceSetRotation(-instance.lastYaw, true, -instance.lastPitch, true);
                instance.lastYaw = NumberUtils.getRandomFloat(-instance.strength, instance.strength);
                instance.lastPitch = NumberUtils.getRandomFloat(-instance.strength, instance.strength);
                player.forceSetRotation(instance.lastYaw, true, instance.lastPitch, true);
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
                ServerPlayer player = instance.player;
                if (instance.remainingTicks != -1) instance.remainingTicks--;

                if (instance.remainingTicks == 0) {
                    endGenericScreenEffect(instance);
                    iterator1.remove();
                }
            }
        }
    }

    public static void shake(ServerPlayer player, int ticks, float strength, Identifier source) {
        CameraShakeInstance instance = new CameraShakeInstance(player, ticks, strength, source);
        cameraShakeInstances.add(instance);
    }

    public static void stopShaking(ServerPlayer player, Identifier source) {
        cameraShakeInstances.removeIf(instance -> instance.player == player && instance.source == source);
    }

    public static void stopAllShaking(ServerPlayer player) {
        cameraShakeInstances.removeIf(instance -> instance.player == player);
    }

    private static void initGenericScreenEffect(GenericScreenEffectInstance instance) {
        switch (instance.effect) {
            case RED_TINT -> {
                WorldBorder customWorldBorder = new WorldBorder();
                customWorldBorder.setWarningBlocks(1000000000);
                instance.player.connection.send(new ClientboundSetBorderWarningDistancePacket(customWorldBorder));
            }
            case SNOW ->
                    instance.player.connection.send(new ClientboundSetEntityDataPacket(instance.player.getId(), List.of(SynchedEntityData.DataValue.create(Entity.DATA_TICKS_FROZEN, 2147483646))));
            case FIRE -> {
                byte flags = instance.player.getEntityData().get(EntityTrackedData.FLAGS);
                byte updatedFlags = (byte) (flags | (1 << EntityTrackedData.ON_FIRE_FLAG_INDEX));
                instance.player.connection.send(new ClientboundSetEntityDataPacket(instance.player.getId(), List.of(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, updatedFlags))));
            }
            case NAUSEA ->
                    instance.player.connection.send(new ClientboundUpdateMobEffectPacket(instance.player.getId(), new MobEffectInstance(MobEffects.NAUSEA, -1, 0, true, false), false));
            case BLACK_HEARTS ->
                    instance.player.connection.send(new ClientboundUpdateMobEffectPacket(instance.player.getId(), new MobEffectInstance(MobEffects.WITHER, -1, 0, true, false), false));
            case GREEN_HEARTS ->
                    instance.player.connection.send(new ClientboundUpdateMobEffectPacket(instance.player.getId(), new MobEffectInstance(MobEffects.POISON, -1, 0, true, false), false));
            case GREEN_HUNGER ->
                    instance.player.connection.send(new ClientboundUpdateMobEffectPacket(instance.player.getId(), new MobEffectInstance(MobEffects.HUNGER, -1, 0, true, false), false));
            case LOCK_CAMERA_AND_POS -> ((Lib99jPlayerUtilController) instance.getPlayer()).lib99j$lockCamera();
            case NIGHT_VISION ->
                    instance.player.connection.send(new ClientboundUpdateMobEffectPacket(instance.player.getId(), new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 0, true, false), false));
            case DARKNESS ->
                    instance.player.connection.send(new ClientboundUpdateMobEffectPacket(instance.player.getId(), new MobEffectInstance(MobEffects.DARKNESS, -1, 0, true, false), false));
            case BLINDNESS ->
                    instance.player.connection.send(new ClientboundUpdateMobEffectPacket(instance.player.getId(), new MobEffectInstance(MobEffects.BLINDNESS, -1, 0, true, false), false));
        }
    }

    private static void endGenericScreenEffect(GenericScreenEffectInstance instance) {
        instance.markFinished();
        if (!hasGenericScreenEffect(instance.player, instance.effect)) {
            switch (instance.effect) {
                case RED_TINT -> {
                    instance.player.connection.send(new ClientboundSetBorderWarningDistancePacket(instance.getPlayer().level().getWorldBorder()));
                }
                case SNOW ->
                        instance.player.connection.send(new ClientboundSetEntityDataPacket(instance.player.getId(), List.of(SynchedEntityData.DataValue.create(Entity.DATA_TICKS_FROZEN, 0))));
                case FIRE -> {
                    byte flags = instance.player.getEntityData().get(EntityTrackedData.FLAGS);
                    byte updatedFlags = (byte) (flags & ~(1 << EntityTrackedData.ON_FIRE_FLAG_INDEX));
                    instance.player.connection.send(new ClientboundSetEntityDataPacket(instance.player.getId(), List.of(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, updatedFlags))));
                }
                case NAUSEA -> {
                    if (instance.player.hasEffect(MobEffects.NAUSEA)) {
                        instance.player.connection.send(new ClientboundUpdateMobEffectPacket(instance.player.getId(), Objects.requireNonNull(instance.player.getEffect(MobEffects.NAUSEA)), false));
                    } else {
                        instance.player.connection.send(new ClientboundRemoveMobEffectPacket(instance.player.getId(), MobEffects.NAUSEA));
                    }
                }
                case BLACK_HEARTS -> {
                    if (instance.player.hasEffect(MobEffects.WITHER)) {
                        instance.player.connection.send(new ClientboundUpdateMobEffectPacket(instance.player.getId(), Objects.requireNonNull(instance.player.getEffect(MobEffects.WITHER)), false));
                    } else {
                        instance.player.connection.send(new ClientboundRemoveMobEffectPacket(instance.player.getId(), MobEffects.WITHER));
                    }
                }
                case GREEN_HEARTS -> {
                    if (instance.player.hasEffect(MobEffects.POISON)) {
                        instance.player.connection.send(new ClientboundUpdateMobEffectPacket(instance.player.getId(), Objects.requireNonNull(instance.player.getEffect(MobEffects.POISON)), false));
                    } else {
                        instance.player.connection.send(new ClientboundRemoveMobEffectPacket(instance.player.getId(), MobEffects.POISON));
                    }
                }
                case GREEN_HUNGER -> {
                    if (instance.player.hasEffect(MobEffects.HUNGER)) {
                        instance.player.connection.send(new ClientboundUpdateMobEffectPacket(instance.player.getId(), Objects.requireNonNull(instance.player.getEffect(MobEffects.HUNGER)), false));
                    } else {
                        instance.player.connection.send(new ClientboundRemoveMobEffectPacket(instance.player.getId(), MobEffects.HUNGER));
                    }
                }
                case LOCK_CAMERA_AND_POS -> ((Lib99jPlayerUtilController) instance.getPlayer()).lib99j$unlockCamera();
                case BLINDNESS -> {
                    if (instance.player.hasEffect(MobEffects.BLINDNESS)) {
                        instance.player.connection.send(new ClientboundUpdateMobEffectPacket(instance.player.getId(), Objects.requireNonNull(instance.player.getEffect(MobEffects.BLINDNESS)), false));
                    } else {
                        instance.player.connection.send(new ClientboundRemoveMobEffectPacket(instance.player.getId(), MobEffects.BLINDNESS));
                    }
                }
                case NIGHT_VISION -> {
                    if (instance.player.hasEffect(MobEffects.NIGHT_VISION)) {
                        instance.player.connection.send(new ClientboundUpdateMobEffectPacket(instance.player.getId(), Objects.requireNonNull(instance.player.getEffect(MobEffects.NIGHT_VISION)), false));
                    } else {
                        instance.player.connection.send(new ClientboundRemoveMobEffectPacket(instance.player.getId(), MobEffects.NIGHT_VISION));
                    }
                }
                case DARKNESS -> {
                    if (instance.player.hasEffect(MobEffects.DARKNESS)) {
                        instance.player.connection.send(new ClientboundUpdateMobEffectPacket(instance.player.getId(), Objects.requireNonNull(instance.player.getEffect(MobEffects.DARKNESS)), false));
                    } else {
                        instance.player.connection.send(new ClientboundRemoveMobEffectPacket(instance.player.getId(), MobEffects.DARKNESS));
                    }
                }
            }
        }
    }

    public static void addGenericScreenEffect(ServerPlayer player, int ticks, GENERIC_SCREEN_EFFECT effect, Identifier source) {
        GenericScreenEffectInstance instance = new GenericScreenEffectInstance(player, ticks, effect, source);
        genericScreenEffectInstances.add(instance);
    }

    public static void setCameraInterpolation(ServerPlayer player, int time) {
        if(!hasGenericScreenEffect(player, GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS)) throw new IllegalStateException("Camera must be locked");
        ((Lib99jPlayerUtilController) player).lib99j$setCameraInterpolationTime(time);
    }

    public static void setCameraPos(ServerPlayer player, Vec3 pos) {
        if(!hasGenericScreenEffect(player, GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS)) throw new IllegalStateException("Camera must be locked");
        ((Lib99jPlayerUtilController) player).lib99j$setCameraPos(pos);
    }

    public static void setCameraPitch(ServerPlayer player, float pitch) {
        if(!hasGenericScreenEffect(player, GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS)) throw new IllegalStateException("Camera must be locked");
        ((Lib99jPlayerUtilController) player).lib99j$setCameraPitch(pitch);
    }

    public static void setCameraYaw(ServerPlayer player, float yaw) {
        if(!hasGenericScreenEffect(player, GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS)) throw new IllegalStateException("Camera must be locked");
        ((Lib99jPlayerUtilController) player).lib99j$setCameraYaw(yaw);
    }

    public static void addGenericScreenEffectUnlessExists(ServerPlayer player, int ticks, GENERIC_SCREEN_EFFECT effect, Identifier source) {
        if (!hasGenericScreenEffectSource(player, effect, source))
            addGenericScreenEffect(player, ticks, effect, source);
    }

    public static void removeGenericScreenEffect(ServerPlayer player, Identifier source) {
        genericScreenEffectInstances.removeIf(instance -> {
            if (instance.player == player && instance.source.toString().equals(source.toString())) {
                endGenericScreenEffect(instance);
                return true;
            }
            return false;
        });
    }

    public static void clearGenericScreenEffects(ServerPlayer player) {
        genericScreenEffectInstances.removeIf(instance -> {
            if (instance.player == player) {
                endGenericScreenEffect(instance);
                return true;
            }
            return false;
        });
    }

    @ApiStatus.Internal
    public static boolean hasEffectEffect(ServerPlayer player) {
        return genericScreenEffectInstances.stream().anyMatch(instance -> instance.player == player  && !instance.isFinished() && (
                instance.effect == GENERIC_SCREEN_EFFECT.NIGHT_VISION ||
                        instance.effect == GENERIC_SCREEN_EFFECT.DARKNESS ||
                        instance.effect == GENERIC_SCREEN_EFFECT.BLINDNESS ||
                        instance.effect == GENERIC_SCREEN_EFFECT.BLACK_HEARTS ||
                        instance.effect == GENERIC_SCREEN_EFFECT.GREEN_HEARTS ||
                        instance.effect == GENERIC_SCREEN_EFFECT.GREEN_HUNGER ||
                        instance.effect == GENERIC_SCREEN_EFFECT.NAUSEA
        ));
    }

    public static boolean hasGenericScreenEffect(ServerPlayer player, GENERIC_SCREEN_EFFECT effect) {
        return genericScreenEffectInstances.stream().anyMatch(instance -> instance.player == player && instance.effect == effect && !instance.isFinished());
    }

    public static boolean hasGenericScreenEffectSource(ServerPlayer player, GENERIC_SCREEN_EFFECT effect, Identifier source) {
        return genericScreenEffectInstances.stream().anyMatch(instance -> instance.player == player && instance.effect == effect && instance.source == source && !instance.isFinished());
    }

    public static List<GenericScreenEffectInstance> getGenericScreenEffectInstances() {
        return genericScreenEffectInstances;
    }

    public static void clientSideExplode(
            ArrayList<ServerPlayer> players,
            @Nullable ExplosionDamageCalculator behavior,
            double x,
            double y,
            double z,
            float power,
            boolean createFire,
            ParticleOptions smallParticle,
            ParticleOptions largeParticle,
            Holder<SoundEvent> soundEvent,
            boolean ignoreResistance) {
        Explosion.BlockInteraction destructionType = Explosion.BlockInteraction.DESTROY;
        Vec3 vec3d = new Vec3(x, y, z);
        FakeExplosionImpl explosionImpl = new FakeExplosionImpl(players, (ServerLevel) players.getFirst().level(), behavior, vec3d, power, createFire, destructionType, ignoreResistance);
        explosionImpl.explode();
        ParticleOptions particleEffect = explosionImpl.isSmall() ? smallParticle : largeParticle;

        for (ServerPlayer serverPlayerEntity : players) {
            if (serverPlayerEntity.distanceToSqr(vec3d) < 4096.0) {
                Optional<Vec3> optional = Optional.ofNullable(explosionImpl.getHitPlayers().get(serverPlayerEntity));
                serverPlayerEntity.connection.send(new ClientboundExplodePacket(vec3d, 1, 0, optional, particleEffect, soundEvent, WeightedList.of()));
            }
        }
    }

    public static void fireworkExplode(List<ServerPlayer> players, List<FireworkExplosion> explosions, Vec3 pos, Vec3 velocity) {
        fireworkExplode(players, explosions, pos, velocity, 123456, 0);
    }

    public static void fireworkExplode(List<ServerPlayer> players, List<FireworkExplosion> explosions, Vec3 pos, Vec3 velocity, float pitch, float yaw) {
        boolean angle = pitch != 123456;
        ItemStack fireworkStack = Items.FIREWORK_ROCKET.getDefaultInstance();
        fireworkStack.set(DataComponents.FIREWORKS, new Fireworks(1, explosions));
        fireworkStack.set(DataComponents.ITEM_MODEL, Identifier.withDefaultNamespace("air"));

        int id = VirtualEntityUtils.requestEntityId();
        ClientboundBundlePacket bundle = new ClientboundBundlePacket(List.of(new ClientboundAddEntityPacket(id, UUID.randomUUID(),
                        pos.x, pos.y, pos.z, angle ? pitch : 0,  angle ? yaw : 0,
                        EntityType.FIREWORK_ROCKET, 0, velocity, 0d),
                new ClientboundSetEntityDataPacket(id, List.of(
                        SynchedEntityData.DataValue.create(FireworkRocketEntity.DATA_ID_FIREWORKS_ITEM, fireworkStack),
                        SynchedEntityData.DataValue.create(FireworkRocketEntity.DATA_SHOT_AT_ANGLE, angle)
                )),
                new ClientboundEntityEventPacket(EntityUtils.fakeEntityFromId(id), (byte)17),
                new ClientboundRemoveEntitiesPacket(id)
        ));

        for(ServerPlayer player : players) {
            player.connection.send(bundle);
        };
    }

    public static void sendFakeEntity(List<ServerPlayer> players, Vec3 pos, EntityType<?> entityType) {
        int id = VirtualEntityUtils.requestEntityId();
        for(ServerPlayer player : players) {
            player.connection.send(new ClientboundAddEntityPacket(id, UUID.randomUUID(),
                    pos.x, pos.y, pos.z, 0, 0,
                    entityType, 0, Vec3.ZERO, 0d));
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
        DARKNESS,
        LOCK_CAMERA_AND_POS
    }

    public static class CameraShakeInstance {
        ServerPlayer player;
        int ticks;
        float strength;
        int remainingTicks;
        Identifier source;
        float lastYaw;
        float lastPitch;

        private CameraShakeInstance(ServerPlayer player, int ticks, float strength, Identifier source) {
            this.ticks = ticks;
            this.remainingTicks = ticks;
            this.player = player;
            this.strength = strength;
            this.source = source;
        }
    }

    public static class GenericScreenEffectInstance {
        ServerPlayer player;
        int ticks;
        int remainingTicks;
        GENERIC_SCREEN_EFFECT effect;
        Identifier source;
        List<Packet<ClientGamePacketListener>> queuedPackets = new ArrayList<>();
        boolean finished;

        private GenericScreenEffectInstance(ServerPlayer player, int ticks, GENERIC_SCREEN_EFFECT effect, Identifier source) {
            this.ticks = ticks;
            this.remainingTicks = ticks;
            this.player = player;
            this.effect = effect;
            this.source = source;
            initGenericScreenEffect(this);
        }

        public ServerPlayer getPlayer() {
            return player;
        }

        public GENERIC_SCREEN_EFFECT getEffect() {
            return effect;
        }

        public List<Packet<ClientGamePacketListener>> getQueuedPackets() {
            return queuedPackets;
        }

        public void markFinished() {
            this.finished = true;
        }

        public boolean isFinished() {
            return finished;
        }
    }

    private static class FakeExplosionImpl extends ServerExplosion {
        ArrayList<ServerPlayer> players;

        public FakeExplosionImpl(ArrayList<ServerPlayer> players, ServerLevel world, @Nullable ExplosionDamageCalculator behavior, Vec3 pos, float power, boolean createFire, BlockInteraction destructionType, boolean ignoreResistance) {
            super(world, null, null, new ExplosionDamageCalculator() {
                @Override
                public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter world, BlockPos pos, BlockState blockState, FluidState fluidState) {
                    return ignoreResistance ? Optional.of(0f) : super.getBlockExplosionResistance(explosion, world, pos, blockState, fluidState);
                }
            }, pos, power, createFire, destructionType);
            this.players = players;
        }

        @Override
        public int explode() {
            List<BlockPos> list = this.calculateExplodedPositions();
            for (BlockPos blockPos : list) {
                if (this.interactsWithBlocks()) {
                    for (ServerPlayer player : players) {
                        player.connection.send(new ClientboundBlockUpdatePacket(blockPos, Blocks.AIR.defaultBlockState()));
                    }
                }

                if (this.fire && this.level().random.nextInt(3) == 0 && this.level().getBlockState(blockPos).isAir() && !list.contains(blockPos.below()) && this.level().getBlockState(blockPos.below()).isSolidRender()) {
                    for (ServerPlayer player : players) {
                        player.connection.send(new ClientboundBlockUpdatePacket(blockPos, Blocks.FIRE.defaultBlockState()));
                    }
                }
            }
            return 0;
        }
    }
}
