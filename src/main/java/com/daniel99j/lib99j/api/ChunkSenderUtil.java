package com.daniel99j.lib99j.api;

import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.ArrayList;
import java.util.List;

public class ChunkSenderUtil {
    public static void sendRegion(ServerPlayer player, ChunkPos pos1, ChunkPos pos2, ServerLevel from) {
        List<ChunkPos> chunks = new ArrayList<>();
        for (int i = pos1.x; i < pos2.x; i++) {
            for (int j = pos1.z; j < pos2.z; j++) {
                chunks.add(new ChunkPos(i, j));
            }
        }
        sendChunkBatches(player, chunks, from);
    }

    public static void sendChunkBatches(ServerPlayer player, List<ChunkPos> chunkPositions, ServerLevel from) {
        if (!chunkPositions.isEmpty()) {
            List<LevelChunk> list = makeBatch(chunkPositions, from);
            if (!list.isEmpty()) {
                player.connection.send(ClientboundChunkBatchStartPacket.INSTANCE);

                for (LevelChunk worldChunk : list) {
                    sendChunkData(player.connection, from, worldChunk);
                }

                player.connection.send(new ClientboundChunkBatchFinishedPacket(list.size()));
            }
        }
    }

    private static List<LevelChunk> makeBatch(List<ChunkPos> chunkPositions, ServerLevel from) {
        List<LevelChunk> list = new ArrayList<>();
        for (ChunkPos chunkPos : chunkPositions) {
            LevelChunk c = from.getChunk(chunkPos.x, chunkPos.z);
            if(c != null && c.getPersistedStatus() == ChunkStatus.FULL) list.add(c);
        }
        return list;
    }

    private static void sendChunkData(ServerGamePacketListenerImpl handler, ServerLevel world, LevelChunk chunk) {
        handler.send(new ClientboundLevelChunkWithLightPacket(chunk, world.getLightEngine(), null, null));
        world.debugSynchronizers().startTrackingChunk(handler.player, chunk.getPos());


        for (var hologram : ((HolderAttachmentHolder) chunk).polymerVE$getHolders()) {
            hologram.startWatching(handler);
        }
    }
}
