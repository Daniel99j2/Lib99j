package com.daniel99j.lib99j.ponder.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PonderGroup {
    public final Identifier id;
    public final ItemStack icon;
    public final List<PonderBuilder> builders;
    private final List<PonderGroup> relatedGroups;

    public PonderGroup(Identifier id, ItemStack icon, List<PonderBuilder> builders) {
        this.id = id;
        this.icon = icon;
        this.builders = new ArrayList<>(builders);
        this.relatedGroups = new ArrayList<>();
    }

    public PonderGroup addRelatedGroup(PonderGroup group) {
        this.relatedGroups.add(group);
        group.relatedGroups.add(this);
        return this;
    }

    public List<PonderGroup> getRelatedGroups() {
        return relatedGroups;
    }
}
