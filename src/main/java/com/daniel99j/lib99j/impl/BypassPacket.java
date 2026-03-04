package com.daniel99j.lib99j.impl;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

@SuppressWarnings({"rawtypes", "NullableProblems"})
public record BypassPacket(Packet<?> packet) implements Packet {
    @Override
    public PacketType<? extends Packet> type() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handle(PacketListener listener) {
        throw new UnsupportedOperationException();
    }
}
