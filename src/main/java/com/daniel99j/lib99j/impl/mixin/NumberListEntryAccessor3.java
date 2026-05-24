package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.api.mixin.MixinCondition;
import com.daniel99j.lib99j.api.mixin.MixinLoadConditions;
import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@MixinLoadConditions(conditions = @MixinCondition(mod = "cloth-config", condition = "*"))
@Mixin({IntegerSliderEntry.class})
public interface NumberListEntryAccessor3 {
    @Mutable
    @Accessor("minimum")
    int getMin();

    @Mutable
    @Accessor("maximum")
    int getMax();
}