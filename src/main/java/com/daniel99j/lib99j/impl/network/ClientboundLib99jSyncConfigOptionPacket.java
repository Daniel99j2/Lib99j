package com.daniel99j.lib99j.impl.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundLib99jSyncConfigOptionPacket(String modId, String name, String value) implements CustomPacketPayload {
    public static final Type<ClientboundLib99jSyncConfigOptionPacket> ID = new Type<>(PacketIdentifiers.CLIENTBOUND_CONFIG_OPTION_SYNC);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLib99jSyncConfigOptionPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ClientboundLib99jSyncConfigOptionPacket::modId,
            ByteBufCodecs.STRING_UTF8,
            ClientboundLib99jSyncConfigOptionPacket::name,
            ByteBufCodecs.STRING_UTF8,
            ClientboundLib99jSyncConfigOptionPacket::value,
            ClientboundLib99jSyncConfigOptionPacket::new
    );
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}