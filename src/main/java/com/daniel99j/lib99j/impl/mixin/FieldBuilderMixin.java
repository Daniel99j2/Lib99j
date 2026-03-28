package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.api.config.ConfigHolder;
import com.daniel99j.lib99j.impl.ConfigEntryValueHolder;
import me.shedaniel.clothconfig2.impl.builders.FieldBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({FieldBuilder.class})
public abstract class FieldBuilderMixin implements ConfigEntryValueHolder {
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

    @Inject(method = "finishBuilding", at = @At("TAIL"))
    private void lib99j$addConfigFieldValue(CallbackInfoReturnable<?> cir) {
        ((ConfigEntryValueHolder) cir.getReturnValue()).lib99j$setValue(this.lib99j$getValue());
    }
}