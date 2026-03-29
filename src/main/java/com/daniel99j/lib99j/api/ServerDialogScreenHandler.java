package com.daniel99j.lib99j.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ServerDialogScreenHandler extends AbstractContainerMenu {
    public ServerDialog dialog;

    public ServerDialogScreenHandler(ServerDialog dialog) {
        super(null, 0);
        this.dialog = dialog;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        throw new IllegalArgumentException();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    protected Slot addSlot(Slot slot) {
        throw new IllegalArgumentException();
    }

    @Override
    public void broadcastChanges() {
    }

    @Override
    public void broadcastFullState() {
    }

    @Override
    public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
        throw new IllegalArgumentException();
    }
}
