package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.Lib99j;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.NullOps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class ExtraBiomeUtil {
    @ApiStatus.Internal
    public static final List<RegistrySynchronization.PackedRegistryEntry> EXTRAS = new ArrayList<>();
    @ApiStatus.Internal
    public static final Map<Identifier, Integer> PALETTE_IDS = new HashMap<>();

    public static void addNewBiome(Identifier id, Biome.BiomeBuilder biomeBuilder) {
        //RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, null);

        CompoundTag tag = (CompoundTag) Biome.NETWORK_CODEC.encodeStart(NbtOps.INSTANCE, biomeBuilder.build()).getOrThrow();
        EXTRAS.add(new RegistrySynchronization.PackedRegistryEntry(id, Optional.of(tag)));
    };

    public static void sendCustomBiomeTo(ServerPlayer player, Identifier id) {
        byte biomeId = (byte) (int) PALETTE_IDS.get(id);
        //biomeId = 15;
        int amount = player.level().getChunk(player.blockPosition()).getSections().length;
        byte[] data = new byte[amount*2]; //simulates a single palette chunk with all being the custom biome

        for (int i = 0; i < amount*2; i+=2) {
            data[i] = 0;
            data[i+1] = biomeId;
        }

        ClientboundChunksBiomesPacket packet = new ClientboundChunksBiomesPacket(List.of(new ClientboundChunksBiomesPacket.ChunkBiomeData(player.chunkPosition(), data)));
        var out = new byte[100];
        Arrays.fill(out, (byte) -128);
        FriendlyByteBuf buf2 = new FriendlyByteBuf(Unpooled.wrappedBuffer(out));
        buf2.writerIndex(0);
        ClientboundChunksBiomesPacket.STREAM_CODEC.encode(buf2, packet);
        player.sendSystemMessage(Component.literal(Arrays.toString(out)));
        player.connection.send(packet);
    }

//    public static void sendCustomBiomeTo(ServerPlayer player, Identifier id, LevelChunkSection section) {
//        PalettedContainer<Holder<Biome>> newBiomes = section.getBiomes().recreate();
//        //player.level.registryAccess().getOrThrow(Registries.BIOME).value().getId(id);
//        int biomeId = PALETTE_IDS.get(id);
//        int size = 4;
//        byte[] data = new byte[newBiomes.getSerializedSize()]; // Create a world to find out!
//
//        newBiomes.write(new FriendlyByteBuf(Unpooled.wrappedBuffer(data)));
//        player.connection.send(new ClientboundChunksBiomesPacket(List.of(new ClientboundChunksBiomesPacket.ChunkBiomeData(player.chunkPosition(), data))));
//    }
}
