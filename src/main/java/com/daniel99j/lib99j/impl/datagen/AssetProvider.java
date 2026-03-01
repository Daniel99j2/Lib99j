package com.daniel99j.lib99j.impl.datagen;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.GameProperties;
import com.daniel99j.lib99j.api.SoundUtils;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.impl.ServerParticleManager;
import com.google.common.hash.HashCode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * This is executed through polymer's resource pack generator, not the traditional data generator
 * <p>This is so that it will not break other mods datagen
 */
public class AssetProvider implements DataProvider {
    private final PackOutput output;
    private static final Map<String, String> translationOverrides = new HashMap<>();

    public AssetProvider(FabricDataOutput output) {
        this.output = output;
    }

    public static void runWriters(BiConsumer<String, byte[]> assetWriter) {
        GuiUtils.generateAssets(assetWriter);

        ServerParticleManager.generateAssets(assetWriter);


        var sounds = new JsonObject();

        SoundUtils.getCustomSubtitles().forEach((entry) -> {
            var event = new JsonObject();

            var soundArray = new JsonArray();
            var soundObject = new JsonObject();
            soundObject.addProperty("name", entry.sound().location().toString());
            soundObject.addProperty("type", "event");
            soundArray.add(soundObject);

            event.add("sounds", soundArray);
            if(entry.show()) event.addProperty("subtitle", "subtitles."+entry.id().toString().replace(":", ".").replace("/", "."));

            sounds.add(entry.id().toString().replace(":", ".").replace("/", "."), event);
        });

        assetWriter.accept("assets/" + Lib99j.MOD_ID + "/sounds.json", sounds.toString().getBytes(StandardCharsets.UTF_8));

        if(GameProperties.isHideableBossBar()) {
            try {
                assetWriter.accept("assets/minecraft/textures/gui/sprites/boss_bar/yellow_progress.png", Lib99j.class.getResourceAsStream("/assets/lib99j/textures/asset/boss_bar/yellow_progress.png").readAllBytes());
                assetWriter.accept("assets/minecraft/textures/gui/sprites/boss_bar/yellow_background.png", Lib99j.class.getResourceAsStream("/assets/lib99j/textures/asset/boss_bar/yellow_background.png").readAllBytes());
            } catch (IOException e) {
                Lib99j.LOGGER.error("Failed to load invisible bossbar", e);
            }
        }

        if(GameProperties.isBadLuckCustomEffect()) {
            try {
                assetWriter.accept("assets/minecraft/textures/mob_effect/unluck.png", Lib99j.class.getResourceAsStream("/assets/lib99j/textures/asset/custom_effect.png").readAllBytes());
                JsonObject json = new JsonObject();
                translationOverrides.forEach(json::addProperty);
                for (String supportedLanguage : Lib99j.SUPPORTED_LANGUAGES) {
                    assetWriter.accept("assets/minecraft/lang/"+supportedLanguage+".json", json.toString().getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                Lib99j.LOGGER.error("Failed to make bad luck a custom effect", e);
            }
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        BiConsumer<String, byte[]> assetWriter = (path, data) -> {
            try {
                writer.writeIfNeeded(this.output.getOutputFolder().resolve(path), data, HashCode.fromBytes(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        return CompletableFuture.runAsync(() -> {
            runWriters(assetWriter);
        }, Util.backgroundExecutor());
    }

    @Override
    public String getName() {
        return "Assets";
    }

    public static void addOverrideTranslationAllSupportedLanguages(String key, String override) {
        translationOverrides.put(key, override);
    }
}
