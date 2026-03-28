package com.daniel99j.lib99j.impl.mixin;

import me.shedaniel.clothconfig2.gui.entries.LongSliderEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({LongSliderEntry.class})
public interface NumberListEntryAccessor2 {
    @Mutable
    @Accessor("minimum")
    long getMin();

    @Mutable
    @Accessor("maximum")
    long getMax();
}