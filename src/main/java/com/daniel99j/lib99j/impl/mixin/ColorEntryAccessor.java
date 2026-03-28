package com.daniel99j.lib99j.impl.mixin;

import me.shedaniel.clothconfig2.gui.entries.ColorEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ColorEntry.class})
public interface ColorEntryAccessor {
    @Mutable
    @Accessor("alpha")
    boolean hasAlpha();
}