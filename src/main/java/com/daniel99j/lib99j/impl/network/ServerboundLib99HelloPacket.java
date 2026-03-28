package com.daniel99j.lib99j.impl.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerboundLib99HelloPacket() implements CustomPacketPayload {
    public static final Type<ServerboundLib99HelloPacket> ID = new Type<>(PacketIdentifiers.SERVERBOUND_HELLO);

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundLib99HelloPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, (ignored) -> true,
            (ignored) -> new ServerboundLib99HelloPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}