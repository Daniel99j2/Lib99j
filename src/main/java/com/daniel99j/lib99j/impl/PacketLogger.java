package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.GameCommonPacketList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugEntryChunkGeneration;
import net.minecraft.client.gui.components.debug.DebugEntryNoop;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class PacketLogger {
    private static final List<Packet<?>> log = new ArrayList<>();
    public static final Identifier LOG_PACKETS = Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "log_packets");

    public static void load() {
        DebugScreenEntries.register(LOG_PACKETS, new DebugEntryNoop());
    }

    public static void store(Packet<?> packet) {
        if(!Minecraft.getInstance().debugEntries.isCurrentlyEnabled(LOG_PACKETS)) {
            log.clear();
            return;
        }
        if(log.size() >= 200) {
            log.removeFirst();
        }
        log.add(packet);
    }

    public static void dump() {
        Lib99j.LOGGER.info("=== PACKETS ===");
        for (Packet<?> packet : new ArrayList<>(log)) {
            System.out.println(packet.toString());
        }
        Lib99j.LOGGER.info("End");
    }
}
