package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.impl.BossBarVisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.server.translations.api.language.ServerLanguageDefinition;
import xyz.nucleoid.server.translations.impl.ServerTranslations;

@Mixin(ServerTranslations.class)
public abstract class SystemLanguageMixin implements BossBarVisibility {
    @Inject(method = "setSystemLanguage", at = @At("TAIL"))
    private static void hideIfInvisible(ServerLanguageDefinition definition, CallbackInfo ci) {
        if(!Lib99j.SUPPORTED_LANGUAGES.contains(definition.code())) {
            Lib99j.LOGGER.warn("Your system's language is set to {}, which is not officially supported by Lib99j and may cause issues", definition.code());
        }
    }
}