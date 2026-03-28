package com.daniel99j.lib99j.impl.network;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record ServerboundLib99jInstalledModsPacket(List<String> installedMods) implements CustomPacketPayload {
    public static final Type<ServerboundLib99jInstalledModsPacket> ID = new Type<>(PacketIdentifiers.SERVERBOUND_LIB99J_INSTALLED_MODS);
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundLib99jInstalledModsPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, (packet) -> {
                CompoundTag tag = new CompoundTag();
                tag.store("mods", Codec.STRING.listOf(), packet.installedMods);
                return tag;
            },
            (tag) -> {
                return new ServerboundLib99jInstalledModsPacket(tag.read("mods", Codec.STRING.listOf()).orElseThrow());
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}