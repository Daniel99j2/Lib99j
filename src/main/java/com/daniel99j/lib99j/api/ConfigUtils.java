package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.Lib99j;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@SuppressWarnings({"unused"})
public class ConfigUtils {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final Gson GSON_PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public static <T> T loadConfig(String name, Path folder, Class<T> clazz) {
        try {
            if (!Files.isDirectory(folder)) {
                if (Files.exists(folder)) {
                    Files.deleteIfExists(folder);
                }
                Files.createDirectories(folder);
            }
            var path = folder.resolve(name + ".json");

            if (path.toFile().isFile()) {
                String json = IOUtils.toString(new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8));
                var obj = GSON.fromJson(json, clazz);

                if (obj != null) {
                    saveConfig(name, folder, obj);
                    return obj;
                }
            }
        } catch (Exception e) {
            Lib99j.LOGGER.warn("Couldn't load config! {}", clazz.toString());
            Lib99j.LOGGER.warn(e.toString());
        }

        try {
            var obj = clazz.getConstructor().newInstance();
            saveConfig(name, folder, obj);
            return obj;
        } catch (Exception e) {
            Lib99j.LOGGER.error("Invalid config class! {}", clazz.toString());
            throw new RuntimeException(e);
        }
    }

    public static void saveConfig(String name, Path folder, Object obj) {
        try {
            if (!Files.isDirectory(folder)) {
                if (Files.exists(folder)) {
                    Files.deleteIfExists(folder);
                }
                Files.createDirectories(folder);
            }
            var path = folder.resolve(name + ".json");

            Files.writeString(path, GSON_PRETTY.toJson(obj), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            Lib99j.LOGGER.warn("Couldn't save config!");
            Lib99j.LOGGER.warn(e.toString());
        }
    }
}
