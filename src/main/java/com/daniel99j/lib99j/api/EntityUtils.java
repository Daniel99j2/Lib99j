package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.impl.mixin.PlayerManagerAccessor;
import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.mixin.accessors.EntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Arm;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings({"unused"})
public class EntityUtils {
    /**
     * Kills an entity from a damage source.
     * <p>It will not kill the entity if it is invulnerable to the damage source
     */
    public static void killDamageSource(LivingEntity entity, DamageSource source) {
        if (!entity.isInvulnerableTo(((ServerWorld) entity.getEntityWorld()), source) && entity.isAlive() && !entity.isRemoved()) {
            entity.damage(((ServerWorld) entity.getEntityWorld()), source, Float.MAX_VALUE);
        }
    }

    /**
     * Creates a fake entity from an entity ID.
     * This is NOT to be used in anything other than an entity id getter
     * <p>(For example), some packets require an entity, but virtual entities only have an id
     */
    public static Entity fakeEntityFromId(int id) {
        Entity entity = new Entity(EntityType.PIG, null) {
            @Override
            protected void initDataTracker(DataTracker.Builder builder) {

            }

            @Override
            public boolean damage(ServerWorld world, DamageSource source, float amount) {
                return false;
            }

            @Override
            protected void readCustomData(ReadView view) {

            }

            @Override
            protected void writeCustomData(WriteView view) {

            }
        };
        entity.setId(id);
        return entity;
    }

    /**
     * Accelerates an entity to a pos
     */
    public static void accelerateTowards(Entity entity, double targetX, double targetY, double targetZ, double acceleration) {
        Vec3d startVelocity = entity.getVelocity();

        Vec3d currentPos = entity.getEntityPos();
        Vec3d targetPos = new Vec3d(targetX, targetY, targetZ);
        Vec3d direction = targetPos.subtract(currentPos).normalize();
        Vec3d accelerationVector = direction.multiply(acceleration);

        entity.addVelocity(accelerationVector.x, accelerationVector.y, accelerationVector.z);

        if (entity instanceof ServerPlayerEntity player)
            sendVelocityDelta(player, player.getVelocity().add(-startVelocity.x, -startVelocity.y, -startVelocity.z));
        if (entity instanceof VehicleEntity vehicle && vehicle.getControllingPassenger() instanceof ServerPlayerEntity player)
            sendVelocityDelta(player, player.getVelocity().add(-startVelocity.x, -startVelocity.y, -startVelocity.z));
    }

    /**
     * Accelerates an entity to a pos
     */
    public static void accelerateTowards(Entity entity, Vec3d target, double acceleration) {
        accelerateTowards(entity, target.x, target.y, target.z, acceleration);

    }

    /**
     * Accelerates an entity based on it's look direction
     */
    public static void accelerateEntityFacing(Entity entity, double acceleration) {
        accelerateEntityPitchYaw(entity, acceleration, entity.getPitch(), entity.getYaw());
    }

    /**
     * Accelerates an entity based on a pitch and yaw
     */
    public static void accelerateEntityPitchYaw(Entity entity, double acceleration, float pitch, float yaw) {
        Vec3d startVelocity = entity.getVelocity();

        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);

        Vec3d accelerationVector = new Vec3d(x, y, z).normalize().multiply(acceleration);

        entity.addVelocity(accelerationVector.x, accelerationVector.y, accelerationVector.z);

        if (entity instanceof ServerPlayerEntity player)
            sendVelocityDelta(player, player.getVelocity().add(-startVelocity.x, -startVelocity.y, -startVelocity.z));
    }

    /**
     * Sends motion data to a player
     * <p>To do this, it creates a fake client-side explosion for the player
     */
    public static void sendVelocityDelta(@NotNull ServerPlayerEntity player, Vec3d delta) {
        player.networkHandler.sendPacket(new ExplosionS2CPacket(new Vec3d(player.getX(), player.getY() - 9999, player.getZ()), 1, 0, Optional.of(delta), ParticleTypes.BUBBLE, Registries.SOUND_EVENT.getEntry(SoundEvents.INTENTIONALLY_EMPTY), Pool.empty()));
    }

    /**
     * Sets an entities look direction from a Direction
     */
    public static void setRotationFromDirection(Direction direction, Entity entity) {
        if (direction == Direction.UP) {
            entity.setPitch(270);
            entity.setYaw(0);
        }
        if (direction == Direction.DOWN) {
            entity.setPitch(90);
            entity.setYaw(0);
        }
        if (direction == Direction.NORTH) {
            entity.setPitch(0);
            entity.setYaw(180);
        }
        if (direction == Direction.SOUTH) {
            entity.setPitch(0);
            entity.setYaw(0);
        }
        if (direction == Direction.EAST) {
            entity.setPitch(0);
            entity.setYaw(270);
        }
        if (direction == Direction.WEST) {
            entity.setPitch(0);
            entity.setYaw(90);
        }
    }

    /**
     * Sets an entity element's look direction from a Direction
     */
    public static void setRotationFromDirection(Direction direction, GenericEntityElement entity) {
        if (direction == Direction.UP) {
            entity.setPitch(270);
            entity.setYaw(0);
        }
        if (direction == Direction.DOWN) {
            entity.setPitch(90);
            entity.setYaw(0);
        }
        if (direction == Direction.NORTH) {
            entity.setPitch(0);
            entity.setYaw(180);
        }
        if (direction == Direction.SOUTH) {
            entity.setPitch(0);
            entity.setYaw(0);
        }
        if (direction == Direction.EAST) {
            entity.setPitch(0);
            entity.setYaw(270);
        }
        if (direction == Direction.WEST) {
            entity.setPitch(0);
            entity.setYaw(90);
        }
    }

    /**
     * Creates a fake player
     */
    public static PlayerEntity fakePlayer(World world, BlockPos pos) {
        return new PlayerEntity(world, new GameProfile(UUID.fromString("58067007-10db-47f1-8844-b142275b76f15"), "LIB99j FAKE PLAYER")) {
            @Override
            public @NotNull GameMode getGameMode() {
                return GameMode.CREATIVE;
            }
        };
    }

    /**
     * Creates a fake server-player
     */
    public static ServerPlayerEntity fakeServerPlayer(ServerWorld world, BlockPos pos) {
        ServerPlayerEntity player = new ServerPlayerEntity(Lib99j.getServerOrThrow(), world, new GameProfile(UUID.fromString("58067007-10db-47f1-8844-b142275b76f1"), "LIB99j FAKE PLAYER"), new SyncedClientOptions("bot", 0, ChatVisibility.HIDDEN, false, 0, Arm.RIGHT, false, false, ParticlesMode.MINIMAL)) {
            @Override
            public @NotNull GameMode getGameMode() {
                return GameMode.CREATIVE;
            }
        };
        player.networkHandler = new ServerPlayNetworkHandler(Lib99j.getServerOrThrow(),
                new ClientConnection(NetworkSide.CLIENTBOUND),
                player,
                ConnectedClientData.createDefault(player.getGameProfile(), false)
        ) {
            @Override
            public boolean isConnectionOpen() {
                return false;
            }

            @Override
            public boolean accepts(Packet<?> packet) {
                return false;
            }
        };
        world.removePlayer(player, Entity.RemovalReason.DISCARDED);
        player.getAdvancementTracker().clearCriteria();
        PlayerManagerAccessor playerManagerAccessor = ((PlayerManagerAccessor) Lib99j.getServerOrThrow().getPlayerManager());
        playerManagerAccessor.getPlayers().remove(player);
        UUID uUID = player.getUuid();
        ServerPlayerEntity serverPlayerEntity = playerManagerAccessor.getPlayerMap().get(uUID);
        if (serverPlayerEntity == player) {
            playerManagerAccessor.getPlayerMap().remove(uUID);
            playerManagerAccessor.getStatisticsMap().remove(uUID);
            playerManagerAccessor.getAdvancementTrackers().remove(uUID);
        }
        return player;
    }

    /**
     * Gets the players who are receiving packets for an entity
     */
    public static List<ServerPlayerEntity> getWatching(Entity entity) {
        return getWatching(((ServerWorld) entity.getEntityWorld()), entity.getChunkPos());
    }

    /**
     * Gets the players who are receiving packets for an entity
     */
    public static List<ServerPlayerEntity> getWatching(ServerWorld world, ChunkPos pos) {
        return new ArrayList<>(world.getChunkManager().chunkLoadingManager.playerChunkWatchingManager.getPlayersWatchingChunk());
    }

    /**
     * Sets a data tracker's data to be an invisible, small, marker armor stand
     */
    public static void dummyArmorStandData(List<DataTracker.SerializedEntry<?>> data) {
        data.add(DataTracker.SerializedEntry.of(EntityTrackedData.FLAGS, (byte) (1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
        data.add(new DataTracker.SerializedEntry<>(EntityAccessor.getDATA_NO_GRAVITY().id(), EntityAccessor.getDATA_NO_GRAVITY().dataType(), true));
        data.add(DataTracker.SerializedEntry.of(ArmorStandEntity.ARMOR_STAND_FLAGS, (byte) (ArmorStandEntity.SMALL_FLAG | ArmorStandEntity.MARKER_FLAG)));
    }

    public static Entity getEntityFromType(EntityType<?> entityType) {
        return entityType.create(PolymerCommonUtils.getFakeWorld(), SpawnReason.COMMAND);
    }
}
