package com.daniel99j.lib99j.ships;

import com.daniel99j.lib99j.Lib99j;
import net.minecraft.server.level.ServerLevel;

public class ShipUtil {

    public static ServerLevel getWorld() {
        return Lib99j.getServerOrThrow().overworld();
    }
}
