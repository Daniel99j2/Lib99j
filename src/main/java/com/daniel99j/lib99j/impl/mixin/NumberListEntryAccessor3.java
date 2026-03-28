package com.daniel99j.lib99j.impl.mixin;

import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({IntegerSliderEntry.class})
public interface NumberListEntryAccessor3 {
    @Mutable
    @Accessor("minimum")
    int getMin();

    @Mutable
    @Accessor("maximum")
    int getMax();
}