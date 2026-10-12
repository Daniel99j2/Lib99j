package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.api.mixin.MixinConditionPlugin;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

public class Lib99jMixinPlugin implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if(mixinClassName.contains("dev")) {
            return LoadingBlastDoor.isDevelopmentEnvironment;
        }
        if(mixinClassName.contains("PolydexPageMixin")) return FabricLoader.getInstance().isModLoaded("polydex");
        return MixinConditionPlugin.meetsConditions(mixinClassName);
    }
}
