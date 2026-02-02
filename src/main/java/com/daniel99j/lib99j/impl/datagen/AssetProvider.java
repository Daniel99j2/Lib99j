package com.daniel99j.lib99j.impl.datagen;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.SoundUtils;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.api.gui.ItemGuiTexture;
import com.daniel99j.lib99j.impl.ServerParticleManager;
import com.google.common.hash.HashCode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.BasicItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.CustomModelDataTintSource;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * This is executed through polymer's resource pack generator, not the traditional data generator
 * <p>This is so that it will not break other mods datagen
 */
public class AssetProvider implements DataProvider {
    private static final String BASIC_ITEM_TEMPLATE = """
            {
              "parent": "%BASE%",
              "textures": {
                "layer0": "%ID%"
              }
            }
            """.replace(" ", "").replace("\n", "");
    private final PackOutput output;

    public AssetProvider(FabricDataOutput output) {
        this.output = output;
    }

    public static void runWriters(BiConsumer<String, byte[]> assetWriter) {
        GuiUtils.generateAssets(assetWriter);

        for (ItemGuiTexture texture : GuiUtils.getItemGuiTextures()) {
            assetWriter.accept("assets/" + texture.path().getNamespace() + "/models/ui/" + texture.path().getPath() + ".json",
                    BASIC_ITEM_TEMPLATE.replace("%ID%", Identifier.fromNamespaceAndPath(texture.path().getNamespace(), "ui/" + texture.path().getPath()).toString()).replace("%BASE%", "minecraft:item/generated").getBytes(StandardCharsets.UTF_8));
        }

        ServerParticleManager.generateAssets(assetWriter);

        assetWriter.accept("assets/lib99j/models/ui/solid_colour.json",
                BASIC_ITEM_TEMPLATE.replace("%ID%", Identifier.fromNamespaceAndPath("lib99j", "ui/solid_colour").toString()).replace("%BASE%", "minecraft:item/generated").getBytes(StandardCharsets.UTF_8));

        assetWriter.accept("assets/lib99j/items/ui/solid_colour.json", new ItemAsset(new BasicItemModel(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ui/solid_colour"), List.of(new CustomModelDataTintSource(0, 0xFFFFFF)))).toJson().getBytes());


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
}
