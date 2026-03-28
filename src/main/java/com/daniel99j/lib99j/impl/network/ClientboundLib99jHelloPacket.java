package com.daniel99j.lib99j.impl.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundLib99jHelloPacket() implements CustomPacketPayload {
    public static final Type<ClientboundLib99jHelloPacket> ID = new Type<>(PacketIdentifiers.CLIENTBOUND_HELLO);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLib99jHelloPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, (ignored) -> true,
            (ignored) -> new ClientboundLib99jHelloPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}