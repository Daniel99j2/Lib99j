package com.daniel99j.lib99j.ships;

import eu.pb4.polymer.virtualentity.mixin.accessors.EntityAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;

public class ShipInstance {
    private BlockPos size;
    private final BlockPos origin;
    private final int beginId;
    public static final int MAX_ENTITIES_PER_BLOCK = 8; //display, shulker (full), 6x shulker extra
    private int[] usedIds = {};
    private final ArrayList<ShipDisplay> activeDisplays = new ArrayList<>();

    public ShipInstance() {
        this.beginId = EntityAccessor.getENTITY_COUNTER().addAndGet(1000);
        this.origin = BlockPos.ZERO;
    }

    public void setSize() {
    }

    public void acceptPacket(Packet<?> packet) {

    }

    public void reloadAll() {
        ServerLevel level = ShipUtil.getWorld();
        ArrayList<Packet<?>> packets = new ArrayList<>();
        packets.add(new ClientboundRemoveEntitiesPacket(IntList.of(usedIds)));

        usedIds = new int[]{};

        for (int x = 0; x < this.size.getX(); x++) {
            for (int y = 0; y < this.size.getY(); y++) {
                for (int z = 0; z < this.size.getZ(); z++) {
                    level.getBlockState(new BlockPos(x, y, z));
                }
            }
        }

//        ArrayList<Integer> usedLids = usedIds.li;
//
//
//
//        for (ShipDisplay activeDisplay : activeDisplays) {
//            activeDisplay.getPlayers()
//        }
    }
}
