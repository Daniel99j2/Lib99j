package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.server.translations.api.language.ServerLanguageDefinition;
import xyz.nucleoid.server.translations.impl.ServerTranslations;
import xyz.nucleoid.server.translations.impl.language.TranslationMap;
import xyz.nucleoid.server.translations.impl.language.TranslationStore;

import java.io.IOException;

@Mixin(ServerTranslations.class)
public abstract class SystemLanguageMixin {
    @Shadow @Final private TranslationStore translations;

    @Inject(method = "setSystemLanguage", at = @At("TAIL"))
    private static void warnLanguage(ServerLanguageDefinition definition, CallbackInfo ci) {
        if(!Lib99j.SUPPORTED_LANGUAGES.contains(definition.code())) {
            Lib99j.LOGGER.warn("Your system's language is set to {}, which is not officially supported by Lib99j and may cause issues", definition.code());
        }
    }
}