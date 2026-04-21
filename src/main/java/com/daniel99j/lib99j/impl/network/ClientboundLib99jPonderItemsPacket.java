package com.daniel99j.lib99j.impl.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record ClientboundLib99jPonderItemsPacket(List<Identifier> items) implements CustomPacketPayload {
    public static final Type<ClientboundLib99jPonderItemsPacket> ID = new Type<>(PacketIdentifiers.CLIENTBOUND_PONDER_ITEMS);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLib99jPonderItemsPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, (packet) -> {
                CompoundTag tag = new CompoundTag();
                tag.store("items", Identifier.CODEC.listOf(), packet.items);
                return tag;
            },
            (tag) -> {
                return new ClientboundLib99jPonderItemsPacket(tag.read("items", Identifier.CODEC.listOf()).orElseThrow());
            });

    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}