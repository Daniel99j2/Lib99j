package com.daniel99j.lib99j.api;

import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkSentS2CPacket;
import net.minecraft.network.packet.s2c.play.StartChunkSendS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class ChunkSenderUtil {
    public static void sendRegion(ServerPlayerEntity player, ChunkPos pos1, ChunkPos pos2, ServerWorld from) {
        List<ChunkPos> chunks = new ArrayList<>();
        for (int i = pos1.x; i < pos2.x; i++) {
            for (int j = pos1.z; j < pos2.z; j++) {
                chunks.add(new ChunkPos(i, j));
            }
        }
        sendChunkBatches(player, chunks, from);
    }

    public static void sendChunkBatches(ServerPlayerEntity player, List<ChunkPos> chunkPositions, ServerWorld from) {
        if (!chunkPositions.isEmpty()) {
            List<WorldChunk> list = makeBatch(chunkPositions, from);
            if (!list.isEmpty()) {
                player.networkHandler.sendPacket(StartChunkSendS2CPacket.INSTANCE);

                for (WorldChunk worldChunk : list) {
                    sendChunkData(player.networkHandler, from, worldChunk);
                }

                player.networkHandler.sendPacket(new ChunkSentS2CPacket(list.size()));
            }
        }
    }

    private static List<WorldChunk> makeBatch(List<ChunkPos> chunkPositions, ServerWorld from) {
        List<WorldChunk> list = new ArrayList<>();
        for (ChunkPos chunkPos : chunkPositions) {
            WorldChunk c = from.getChunk(chunkPos.x, chunkPos.z);
            if(c != null && c.getStatus() == ChunkStatus.FULL) list.add(c);
        }
        return list;
    }

    private static void sendChunkData(ServerPlayNetworkHandler handler, ServerWorld world, WorldChunk chunk) {
        handler.sendPacket(new ChunkDataS2CPacket(chunk, world.getLightingProvider(), null, null));
        world.getSubscriptionTracker().sendInitialIfSubscribed(handler.player, chunk.getPos());


        for (var hologram : ((HolderAttachmentHolder) chunk).polymerVE$getHolders()) {
            hologram.startWatching(handler);
        }
    }
}
