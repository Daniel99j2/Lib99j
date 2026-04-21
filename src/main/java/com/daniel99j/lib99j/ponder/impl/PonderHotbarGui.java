package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.ponder.api.PonderScene;
import eu.pb4.sgui.api.gui.HotbarGui;
import net.minecraft.server.level.ServerPlayer;

//mostly just to hide the inventory
public class PonderHotbarGui extends HotbarGui {
    private final PonderScene scene;

    public PonderHotbarGui(ServerPlayer player, PonderScene scene) {
        super(player);
        this.scene = scene;
    }

    @Override
    public boolean open() {
        if(!super.open()) return false;
        this.setSelectedSlot(4);
        return true;
    }

    @Override
    public boolean onSelectedSlotChange(int slot) {
        if (slot != 4) {
            this.scene.addToSelectedStep(slot-4);
        }
        return false;
    }
}
