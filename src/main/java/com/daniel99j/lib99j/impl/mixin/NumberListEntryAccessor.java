package com.daniel99j.lib99j.impl.mixin;

import me.shedaniel.clothconfig2.gui.entries.AbstractNumberListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({AbstractNumberListEntry.class})
public interface NumberListEntryAccessor {
    @Mutable
    @Accessor("minimum")
    <T> T getMin();

    @Mutable
    @Accessor("maximum")
    <T> T getMax();
}