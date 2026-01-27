package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.impl.mixin.PlayerListAccessor;
import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.mixin.accessors.EntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings({"unused"})
public class EntityUtils {
    @ApiStatus.Internal
    private static int playerIdPrefix = 0;

    /**
     * Kills an entity from a damage source.
     * <p>It will not kill the entity if it is invulnerable to the damage source
     */
    public static void killDamageSource(LivingEntity entity, DamageSource source) {
        if (!entity.isInvulnerableTo(((ServerLevel) entity.level()), source) && entity.isAlive() && !entity.isRemoved()) {
            entity.hurtServer(((ServerLevel) entity.level()), source, Float.MAX_VALUE);
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
            protected void defineSynchedData(SynchedEntityData.Builder builder) {

            }

            @Override
            public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
                return false;
            }

            @Override
            protected void readAdditionalSaveData(ValueInput view) {

            }

            @Override
            protected void addAdditionalSaveData(ValueOutput view) {

            }
        };
        entity.setId(id);
        return entity;
    }

    /**
     * Accelerates an entity to a pos
     */
    public static void accelerateTowards(Entity entity, double targetX, double targetY, double targetZ, double acceleration) {
        Vec3 startVelocity = entity.getDeltaMovement();

        Vec3 currentPos = entity.position();
        Vec3 targetPos = new Vec3(targetX, targetY, targetZ);
        Vec3 direction = targetPos.subtract(currentPos).normalize();
        Vec3 accelerationVector = direction.scale(acceleration);

        entity.push(accelerationVector.x, accelerationVector.y, accelerationVector.z);

        if (entity instanceof ServerPlayer player)
            sendVelocityDelta(player, player.getDeltaMovement().add(-startVelocity.x, -startVelocity.y, -startVelocity.z));
        if (entity instanceof VehicleEntity vehicle && vehicle.getControllingPassenger() instanceof ServerPlayer player)
            sendVelocityDelta(player, player.getDeltaMovement().add(-startVelocity.x, -startVelocity.y, -startVelocity.z));
    }

    /**
     * Accelerates an entity to a pos
     */
    public static void accelerateTowards(Entity entity, Vec3 target, double acceleration) {
        accelerateTowards(entity, target.x, target.y, target.z, acceleration);

    }

    /**
     * Accelerates an entity based on it's look direction
     */
    public static void accelerateEntityFacing(Entity entity, double acceleration) {
        accelerateEntityPitchYaw(entity, acceleration, entity.getXRot(), entity.getYRot());
    }

    /**
     * Accelerates an entity based on a pitch and yaw
     */
    public static void accelerateEntityPitchYaw(Entity entity, double acceleration, float pitch, float yaw) {
        Vec3 startVelocity = entity.getDeltaMovement();

        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);

        Vec3 accelerationVector = new Vec3(x, y, z).normalize().scale(acceleration);

        entity.push(accelerationVector.x, accelerationVector.y, accelerationVector.z);

        if (entity instanceof ServerPlayer player)
            sendVelocityDelta(player, player.getDeltaMovement().add(-startVelocity.x, -startVelocity.y, -startVelocity.z));
    }

    /**
     * Sends motion data to a player
     * <p>To do this, it creates a fake client-side explosion for the player
     */
    public static void sendVelocityDelta(@NotNull ServerPlayer player, Vec3 delta) {
        player.connection.send(new ClientboundExplodePacket(new Vec3(player.getX(), player.getY() - 9999, player.getZ()), 1, 0, Optional.of(delta), ParticleTypes.BUBBLE, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY), WeightedList.of()));
    }

    /**
     * Sets an entities look direction from a Direction
     */
    public static void setRotationFromDirection(Direction direction, Entity entity) {
        if (direction == Direction.UP) {
            entity.setXRot(270);
            entity.setYRot(0);
        }
        if (direction == Direction.DOWN) {
            entity.setXRot(90);
            entity.setYRot(0);
        }
        if (direction == Direction.NORTH) {
            entity.setXRot(0);
            entity.setYRot(180);
        }
        if (direction == Direction.SOUTH) {
            entity.setXRot(0);
            entity.setYRot(0);
        }
        if (direction == Direction.EAST) {
            entity.setXRot(0);
            entity.setYRot(270);
        }
        if (direction == Direction.WEST) {
            entity.setXRot(0);
            entity.setYRot(90);
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
    public static Player fakePlayer(Level world, BlockPos pos) {
        return new Player(world, new GameProfile(UUID.fromString("58067007-10db-47f1-8844-b142275b76f15"), "LIB99j FAKE PLAYER")) {
            @Override
            public @NotNull GameType gameMode() {
                return GameType.CREATIVE;
            }
        };
    }

    /**
     * Creates a fake server-player
     */
    public static ServerPlayer fakeServerPlayer(ServerLevel world, BlockPos pos) {
        return fakeServerPlayerInternal(world, pos, GameType.SURVIVAL, UUID.fromString("58067007-10db-47f1-8844-b142275b76f1"), "LIB99j FAKE PLAYER", true, true);
    }

    /**
     * Creates a fake server-player
     * @param autoEditName specifies if a prefix on an integer is permitted so the GameProfile is always unique.
     */
    public static ServerPlayer fakeServerPlayer(ServerLevel world, BlockPos pos, GameType gameMode, UUID uuid, String name, boolean autoEditName) {
        return fakeServerPlayerInternal(world, pos, gameMode, uuid, name, true, autoEditName);
    }

    /**
     * Creates a fake server-player, but does NOT delete the entity from the world.
     * <p>Only use this if you know what you are doing, as the player will stay loaded in memory.
     * @param autoEditName specifies if a prefix on an integer is permitted so the GameProfile is always unique.
     */
    public static ServerPlayer fakeServerPlayerAddToWorld(ServerLevel world, BlockPos pos, GameType gameMode, UUID uuid, String name, boolean autoEditName) {
        return fakeServerPlayerInternal(world, pos, gameMode, uuid, name, false, autoEditName);
    }

    @ApiStatus.Internal
    private static ServerPlayer fakeServerPlayerInternal(ServerLevel world, BlockPos pos, GameType gameMode, UUID uuid, String name1, boolean remove, boolean autoEditName) {
        String name = autoEditName ? name1 + playerIdPrefix++ : name1;
        if(name.length() > 16) throw new IllegalStateException("Player name is too long");
        if(Lib99j.getServer() != null && Lib99j.getServer().getPlayerList().getPlayerByName(name) != null) {
            throw new IllegalStateException("Player name is already taken");
        }
        ServerPlayer player = new ServerPlayer(Lib99j.getServerOrThrow(), world, new GameProfile(uuid, name), new ClientInformation("bot", 0, ChatVisiblity.HIDDEN, false, 0, HumanoidArm.RIGHT, false, false, ParticleStatus.MINIMAL));
        player.connection = new ServerGamePacketListenerImpl(Lib99j.getServerOrThrow(),
                new Connection(PacketFlow.CLIENTBOUND),
                player,
                CommonListenerCookie.createInitial(player.getGameProfile(), false)
        ) {
            @Override
            public boolean isAcceptingMessages() {
                return false;
            }

            @Override
            public boolean shouldHandleMessage(Packet<?> packet) {
                return false;
            }
        };
        if(!remove) world.addNewPlayer(player);
        if(remove) world.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        player.gameMode.changeGameModeForPlayer(gameMode);
        player.getAdvancements().stopListening();
        PlayerListAccessor playerManagerAccessor = ((PlayerListAccessor) Lib99j.getServerOrThrow().getPlayerList());
        if(remove) playerManagerAccessor.getPlayers().remove(player);
        else playerManagerAccessor.getPlayers().add(player);
        UUID uUID = player.getUUID();
        ServerPlayer serverPlayerEntity = playerManagerAccessor.getPlayersByUUID().get(uUID);
        if (serverPlayerEntity == player) {
            if(remove) playerManagerAccessor.getPlayersByUUID().remove(uUID);
            else playerManagerAccessor.getPlayersByUUID().put(uUID, player);
            playerManagerAccessor.getStats().remove(uUID);
            playerManagerAccessor.getAdvancements().remove(uUID);
        }
        player.connection.resumeFlushing();
        player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
        return player;
    }

    /**
     * Gets the players who are receiving packets for an entity
     */
    public static List<ServerPlayer> getWatching(Entity entity) {
        return getWatching(((ServerLevel) entity.level()), entity.chunkPosition());
    }

    /**
     * Gets the players who are receiving packets for a chunk
     */
    public static List<ServerPlayer> getWatching(ServerLevel world, ChunkPos pos) {
        return new ArrayList<>(world.getChunkSource().chunkMap.playerMap.getAllPlayers());
    }

    /**
     * Sets a data tracker's data to be an invisible, small, marker armor stand
     */
    public static void dummyArmorStandData(List<SynchedEntityData.DataValue<?>> data) {
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, (byte) (1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
        data.add(new SynchedEntityData.DataValue<>(EntityAccessor.getDATA_NO_GRAVITY().id(), EntityAccessor.getDATA_NO_GRAVITY().serializer(), true));
        data.add(SynchedEntityData.DataValue.create(ArmorStand.DATA_CLIENT_FLAGS, (byte) (ArmorStand.CLIENT_FLAG_SMALL | ArmorStand.CLIENT_FLAG_MARKER)));
    }

    public static Entity getEntityFromType(EntityType<?> entityType) {
        return entityType.create(PolymerCommonUtils.getFakeWorld(), EntitySpawnReason.COMMAND);
    }

    public static BlockPos getFarPos(Entity entity) {
        int far = 1000;
        BlockPos[] corners = {
                new BlockPos( far, 0,  far),
                new BlockPos(-far, 0,  far),
                new BlockPos( far, 0, -far),
                new BlockPos(-far, 0, -far)
        };

        BlockPos furthest = BlockPos.ZERO;
        double furthestDistance = 0;

        for (int i = 0; i < corners.length; i++) {
            double distance = entity.blockPosition().distToLowCornerSqr(corners[i].getX(), corners[i].getY(), corners[i].getZ());
            if (distance > furthestDistance) {
                furthestDistance = distance;
                furthest = corners[i];
            }
        }
        return furthest;
    };

    public static boolean exists(Entity e) {
        return !(e == null || e.isRemoved() || e.touchingUnloadedChunk());
    }
}
