package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.api.config.ConfigHolder;
import com.daniel99j.lib99j.impl.ConfigEntryValueHolder;
import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({AbstractConfigEntry.class})
public abstract class FieldEntryMixin implements ConfigEntryValueHolder {
    @Unique
    private ConfigHolder.ConfigField lib99j$configValueName = null;

    @Override
    public ConfigHolder.ConfigField lib99j$getValue() {
        return this.lib99j$configValueName;
    }

    @Override
    public void lib99j$setValue(ConfigHolder.ConfigField value) {
        this.lib99j$configValueName = value;
    }
}