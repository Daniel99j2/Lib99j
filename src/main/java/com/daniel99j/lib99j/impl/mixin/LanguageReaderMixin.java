package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.GameProperties;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.server.translations.impl.language.LanguageReader;
import xyz.nucleoid.server.translations.impl.language.TranslationMap;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mixin(LanguageReader.class)
public abstract class LanguageReaderMixin {
    @Inject(method = "collectDataPackTranslations", at = @At("TAIL"))
    private static void addAssetTranslationFiles(ResourceManager manager, CallbackInfoReturnable<Multimap<String, Supplier<TranslationMap>>> cir) {
        if(!GameProperties.shouldAddAssetTranslationsToServer()) return;

        Multimap<String, Supplier<TranslationMap>> map = cir.getReturnValue();

        try {
            Path zip = PolymerResourcePackUtils.getMainPath();
            try (FileSystem fs = FileSystems.newFileSystem(zip)) {
                Path assets = fs.getPath("/assets");

                if (!Files.exists(assets)) return;

                try (Stream<Path> namespaces = Files.list(assets)) {
                    namespaces.filter(Files::isDirectory).forEach(namespace -> {
                        Path langDir = namespace.resolve("lang");

                        if (!Files.exists(langDir)) return;

                        try (Stream<Path> files = Files.list(langDir)) {
                            files.filter(p -> p.toString().endsWith(".json")).forEach(file -> {
                                String lang = file.getFileName().toString().replace(".json", "");

                                TranslationMap translation = new TranslationMap();
                                try (InputStream stream = Files.newInputStream(file);
                                     Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {

                                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                                    for (String key : json.keySet()) {
                                        translation.put(key, json.get(key).getAsString());
                                    }

                                } catch (Exception e) {
                                    Lib99j.LOGGER.error("Failed loading translation {}", file, e);
                                }

                                map.put(lang, () -> translation);
                            });
                        } catch (Exception e) {
                            Lib99j.LOGGER.error("Failed listing lang files in {}", langDir, e);
                        }
                    });
                }

            }
        } catch (Exception e) {
            Lib99j.LOGGER.error("Failed to load asset lang files", e);
        }
    }
}