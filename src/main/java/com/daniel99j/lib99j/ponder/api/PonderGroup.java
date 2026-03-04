package com.daniel99j.lib99j.ponder.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public record PonderGroup(Identifier id, ItemStack icon, ArrayList<PonderBuilder> builders) {
}
