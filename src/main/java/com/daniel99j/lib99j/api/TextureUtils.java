package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.Lib99j;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class TextureUtils {
    public static BufferedImage getMissingTexture() {
        int size = 16; // 16x16 texture
        BufferedImage missingTexture = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        // Draw the checkerboard pattern
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean isMagenta = (x / 8 + y / 8) % 2 == 0; // Alternating 8x8 squares
                Color color = isMagenta ? Color.MAGENTA : Color.BLACK;
                missingTexture.setRGB(x, y, color.getRGB());
            }
        }

        return missingTexture;
    }

    //All code below was... borrowed from polyfactory. https://github.com/Patbox/PolyFactory
    public static byte[] getJarData(String jarPath) throws IOException {
        var path = findAsset(jarPath);
        return path != null ? Files.readAllBytes(path) : null;
    }

    public static InputStream getJarStream(String jarPath) throws IOException {
        var path = findAsset(jarPath);
        return path != null ? Files.newInputStream(path) : null;
    }

    public static Path findAsset(String jarPath) {
        for (var mod : FabricLoader.getInstance().getAllMods()) {
            for (var basePath : mod.getRootPaths()) {
                var path = basePath.resolve(jarPath);
                if (Files.exists(path)) {
                    return path;
                }
            }
        }
        var path = Objects.requireNonNull(PolymerCommonUtils.getClientJarRoot()).resolve(jarPath);
        if (Files.exists(path)) {
            return path;
        }
        return null;
    }

    public static BufferedImage getTexture(Identifier identifier) {
        try {
            return ImageIO.read(getJarStream("assets/" + identifier.getNamespace() + "/textures/" + identifier.getPath() + ".png"));
        } catch (Throwable e) {
            Lib99j.LOGGER.error("Failed to load texture '{}'", identifier, e);
            return new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        }
    }

    public static JsonObject getModel(Identifier identifier) {
        try {
            return (JsonObject) JsonParser.parseString(new String(
                    Objects.requireNonNull(getJarData("assets/" + identifier.getNamespace() + "/models/" + identifier.getPath() + ".json")), StandardCharsets.UTF_8
            ));
        } catch (Throwable e) {
            e.printStackTrace();
            return new JsonObject();
        }
    }
}
