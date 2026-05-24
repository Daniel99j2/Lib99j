package com.daniel99j.lib99j.impl.mixin.dev;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.impl.Cube;
import com.daniel99j.lib99j.impl.sable.SableDisplayData;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.data.DisplayEntityData;
import eu.pb4.polymer.virtualentity.api.data.EntityData;
import eu.pb4.polymer.virtualentity.impl.SafeBundler;
import eu.pb4.polymer.virtualentity.mixin.accessors.BlockDisplayAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(PlayerChunkSender.class)
public abstract class ChunkSenderMixin {
    @Unique
    private static final Map<Integer, List<Cube>> blockStateToCollision = new HashMap<>();

    @Inject(method = "sendChunk", at = @At(value = "HEAD"))
    private static void lib99j$testChunkEdit(ServerGamePacketListenerImpl connection, ServerLevel level, LevelChunk chunk, CallbackInfo ci) {
        if(!Lib99j.isDevelopingLib99j) return;

        if(chunk.getPos().x() == 5 && chunk.getPos().z() == 5) {
            Lib99j.sableCollisionMap.clear();
            Lib99j.sableDisplayData.clear();
            //ci.cancel();

            //connection.send(new ClientboundLevelChunkWithLightPacket(new LevelChunk(PolymerCommonUtils.getFakeWorld(), chunk.getPos()), level.getLightEngine(), null, null));

            SafeBundler safeBundler = new SafeBundler(connection::send);

            int sectionI = 0;

            byte invisible = 0;
            invisible |= (byte) (1 << EntityData.INVISIBLE_FLAG_INDEX);
            //invisible |= (byte) (1 << EntityData.GLOWING_FLAG_INDEX);
            for (LevelChunkSection section : chunk.getSections()) {
                if(!section.hasOnlyAir()) {
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                BlockState state = section.getBlockState(x, y, z);
                                if (!state.isAir()) {
                                    BlockPos pos = new BlockPos(chunk.getPos().x()*16+x,  level.getMinY()+sectionI*16+y, chunk.getPos().z()*16+z);

                                    boolean cull = true;
                                    for (Direction dir : Direction.values()) {
                                        BlockState otherState = level.getBlockState(pos.relative(dir));
                                        if (!otherState.isFaceSturdy(level, pos, dir.getOpposite())) {
                                            cull = false;
                                            break;
                                        }
                                    }
                                    if(cull) continue;

                                    int id = VirtualEntityUtils.requestEntityId();
//                                    ids.add(id);
//                                    posList.add();

                                    BlockPos pos1 = new BlockPos(chunk.getPos().x()*16,  level.getMinY(), chunk.getPos().z()*16);
                                    safeBundler.accept(new ClientboundAddEntityPacket(id, UUID.randomUUID(), pos1.getX(), pos1.getY()+100, pos1.getZ(), 0, 0, EntityType.BLOCK_DISPLAY, 0, Vec3.ZERO, 0));
                                    //int brightness = level.getBrightness(LightLayer.BLOCK, pos);
                                    //brightness = Math.max(brightness, level.getEffectiveSkyBrightness(pos));
                                    //, SynchedEntityData.DataValue.create(DisplayEntityData.BRIGHTNESS, brightness)
                                    safeBundler.accept(new ClientboundSetEntityDataPacket(id, List.of(SynchedEntityData.DataValue.create(EntityData.FLAGS, invisible), SynchedEntityData.DataValue.create(BlockDisplayAccessor.getDATA_BLOCK_STATE_ID(), section.getBlockState(x, y, z)), SynchedEntityData.DataValue.create(DisplayEntityData.HEIGHT, 1.0f), SynchedEntityData.DataValue.create(DisplayEntityData.VIEW_RANGE, 16.0f), SynchedEntityData.DataValue.create(DisplayEntityData.WIDTH, 1.0f))));

                                    ArrayList<Integer> ids = new ArrayList<>();
                                    ArrayList<Vec3> offsets = new ArrayList<>();
                                    getCollision(level, pos, safeBundler, ids, offsets);

                                    Lib99j.sableDisplayData.put(pos.asLong(), new SableDisplayData(id, ids.toArray(new Integer[]{}), offsets.toArray(new Vec3[]{}), pos, new Vec3(x,pos.getY(),z), new ArrayList<>()));

                                    //getCollision(level, pos, safeBundler);
//                                    int id2 = VirtualEntityUtils.requestEntityId();
//                                    safeBundler.accept(new ClientboundAddEntityPacket(id2, UUID.randomUUID(), pos.getX()+0.5f, pos.getY()+100, pos.getZ()+0.5f, 0, 0, EntityType.SHULKER, 0, Vec3.ZERO, 0));
//                                    safeBundler.accept(new ClientboundSetEntityDataPacket(id2, List.of(SynchedEntityData.DataValue.create(EntityData.FLAGS, invisible))));
                                }
                            }
                        }
                    }
                }
                sectionI++;
            }

            safeBundler.finish();
        }
    }



    @Unique
    private static void getCollision(ServerLevel level, BlockPos pos, SafeBundler safeBundler, List<Integer> ids, List<Vec3> offsets) {
        //List<Cube> cubes = new ArrayList<>();
        BlockState state = level.getBlockState(pos);

        if(!blockStateToCollision.containsKey(Block.getId(state))) {
            ArrayList<Cube> cubes = new ArrayList<>();

//        for (int x = 0; x < 16; x++) {
//            for (int y = 0; y < 16; y++) {
//                for (int z = 0; z < 16; z++) {
//                    if(state.getCollisionShape(level, pos).clip(new Vec3(x,y,z).multiply(0.0625f, 0.0625f, 0.0625f), new Vec3(x,y,z).add(0.99f).multiply(0.0625f, 0.0625f, 0.0625f), BlockPos.ZERO) != null) cubes.add(new Cube(1, x, y, z));
//                }
//            }
//        }

            boolean[][][] solid = new boolean[16][16][16];
            boolean[][][] alreadyChecked = new boolean[16][16][16];

            //creates the grid of cubes
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (state.getCollisionShape(level, pos).clip(
                                new Vec3(x, y, z).multiply(0.0625f, 0.0625f, 0.0625f),
                                new Vec3(x, y, z).add(0.99f).multiply(0.0625f, 0.0625f, 0.0625f),
                                BlockPos.ZERO
                        ) != null) {
                            solid[x][y][z] = true;
                        }
                    }
                }
            }

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {

                        if (!solid[x][y][z] || alreadyChecked[x][y][z]) continue;

                        int maxSize = 1;

                        //expands it if it can
                        outer:
                        while (true) {
                            int nextSize = maxSize + 1;

                            if (x + nextSize > 16 || y + nextSize > 16 || z + nextSize > 16) {
                                break;
                            }

                            for (int xi = x; xi < x + nextSize; xi++) {
                                for (int yi = y; yi < y + nextSize; yi++) {
                                    for (int zi = z; zi < z + nextSize; zi++) {
                                        if (!solid[xi][yi][zi] || alreadyChecked[xi][yi][zi]) {
                                            break outer;
                                        }
                                    }
                                }
                            }

                            maxSize = nextSize;
                        }

                        //set checked
                        for (int xi = x; xi < x + maxSize; xi++) {
                            for (int yi = y; yi < y + maxSize; yi++) {
                                for (int zi = z; zi < z + maxSize; zi++) {
                                    alreadyChecked[xi][yi][zi] = true;
                                }
                            }
                        }

                        cubes.add(new Cube(maxSize, x, y, z));
                    }
                }
            }
            blockStateToCollision.put(Block.getId(state), cubes);
        }

        for (Cube square : blockStateToCollision.get(Block.getId(state))) {
            byte invisible = 0;
            invisible |= (byte) (1 << EntityData.INVISIBLE_FLAG_INDEX);

            int id = VirtualEntityUtils.requestEntityId();
            Vec3 offset = new Vec3(square.x()/16.0f+square.size()/16.0f/2, square.y()/16.0f, square.z()/16.0f+square.size()/16.0f/2);
            safeBundler.accept(new ClientboundAddEntityPacket(id, UUID.randomUUID(), pos.getX()+offset.x, pos.getY()+offset.y, pos.getZ()+offset.z, 0, 0, EntityType.BLOCK_DISPLAY, 0, Vec3.ZERO, 0));
            safeBundler.accept(new ClientboundSetEntityDataPacket(id, List.of(SynchedEntityData.DataValue.create(EntityData.FLAGS, invisible), SynchedEntityData.DataValue.create(DisplayEntityData.HEIGHT, 0.0f), SynchedEntityData.DataValue.create(DisplayEntityData.VIEW_RANGE, 0.0f), SynchedEntityData.DataValue.create(DisplayEntityData.WIDTH, 0.0f))));

            ids.add(id);
            offsets.add(offset);

            int id2 = VirtualEntityUtils.requestEntityId();
            safeBundler.accept(new ClientboundAddEntityPacket(id2, UUID.randomUUID(), 0, 0, 0, 0, 0, EntityType.SHULKER, 0, Vec3.ZERO, 0));
            safeBundler.accept(new ClientboundSetEntityDataPacket(id2, List.of(SynchedEntityData.DataValue.create(EntityData.FLAGS, invisible))));
            AttributeInstance a = new AttributeInstance(Attributes.SCALE, (c) -> {});
            a.setBaseValue(square.size()/16.0f);
            safeBundler.accept(new ClientboundUpdateAttributesPacket(id2, List.of(a)));

            Lib99j.sableCollisionMap.put(id2, pos.asLong());

            safeBundler.accept(VirtualEntityUtils.createClientboundSetPassengersPacket(id, IntList.of(id2)));
        }

//        state.getCollisionShape(level, pos).forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
//            byte invisible = 0;
//            invisible |= (byte) (1 << EntityData.INVISIBLE_FLAG_INDEX);
//            double maxSize = 1;
//            if(x2-x1 < maxSize) maxSize = x2-x1;
//            if(y2-y1 < maxSize) maxSize = y2-y1;
//            if(z2-z1 < maxSize) maxSize = z2-z1;
//
//            for (int w = 0; w < 10; w++) {
//                for (int h = 0; h < 10; h++) {
//                    for (int d = 0; d < 10; d++) {
//
//                        int id2 = VirtualEntityUtils.requestEntityId();
//                        safeBundler.accept(new ClientboundAddEntityPacket(id2, UUID.randomUUID(), pos.getX()-0.5f+ 0, pos.getY()+0, pos.getZ()-0.5f+ 0, 0, 0, EntityType.SHULKER, 0, Vec3.ZERO, 0));
//                        safeBundler.accept(new ClientboundSetEntityDataPacket(id2, List.of(SynchedEntityData.DataValue.create(EntityData.FLAGS, invisible))));
//                        AttributeInstance a = new AttributeInstance(Attributes.SCALE, (c) -> {});
//                        a.setBaseValue(maxSize);
//                        safeBundler.accept(new ClientboundUpdateAttributesPacket(id2, List.of(a)));
//                    }
//                }
//            }
//
//        });
    }
}