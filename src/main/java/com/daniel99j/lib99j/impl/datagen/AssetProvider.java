package com.daniel99j.lib99j.impl.datagen;

import com.daniel99j.lib99j.api.GuiUtils;
import com.daniel99j.lib99j.impl.ServerParticleManager;
import com.google.common.hash.HashCode;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class AssetProvider implements DataProvider {
    private final DataOutput output;

    private static final String BASIC_ITEM_TEMPLATE = """
            {
              "parent": "%BASE%",
              "textures": {
                "layer0": "%ID%"
              }
            }
            """.replace(" ", "").replace("\n", "");

    public AssetProvider(FabricDataOutput output) {
        this.output = output;
    }

    public static void runWriters(BiConsumer<String, byte[]> assetWriter) {
        GuiUtils.generateAssets(assetWriter);

        for (GuiUtils.GuiTextures.ItemGuiTexture texture : GuiUtils.getItemGuiTextures()) {
            assetWriter.accept("assets/" + texture.path().getNamespace() + "/models/gui/" + texture.path().getPath() + ".json",
                    BASIC_ITEM_TEMPLATE.replace("%ID%", Identifier.of(texture.path().getNamespace(), "gui/"+texture.path().getPath()).toString()).replace("%BASE%", "minecraft:item/generated").getBytes(StandardCharsets.UTF_8));
        }

        ServerParticleManager.generateAssets(assetWriter);
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        BiConsumer<String, byte[]> assetWriter = (path, data) -> {
            try {
                writer.write(this.output.getPath().resolve(path), data, HashCode.fromBytes(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        return CompletableFuture.runAsync(() -> {
            runWriters(assetWriter);
        }, Util.getMainWorkerExecutor());
    }

    @Override
    public String getName() {
        return "Assets";
    }
}
