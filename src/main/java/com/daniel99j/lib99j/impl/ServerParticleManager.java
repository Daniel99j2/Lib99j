package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.ServerParticle;
import com.google.common.collect.EvictingQueue;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.ApiStatus;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class ServerParticleManager {
    @ApiStatus.Internal
    public static final ArrayList<ServerParticleType> particleTypes = new ArrayList<>();
    private static final EvictingQueue<ServerParticle> particles = EvictingQueue.create(32767 * 4);
    private static final ArrayList<ParticleFrame> PARTICLE_TEXTURES = new ArrayList<>();
    private static final ArrayList<Character> blacklistedChars = new ArrayList<>();
    public static ParticleFrame INVISIBLE_FRAME;
    private static char currentGuiChar = '*';

    static {
        blacklistedChars.add('§');
        blacklistedChars.add('\\');
    }

    @ApiStatus.Internal
    public static void tick() {
        for (ServerParticle particle : particles) {
            particle.tick();
        }

        particles.removeIf(ServerParticle::isDead);
    }

    @ApiStatus.Internal
    public static void addParticle(ServerParticle particle) {
        particles.add(particle);
    }

    public static void clearParticles() {
        particles.clear();
    }

    public static void registerParticle(ServerParticleType serverParticleType) {
        particleTypes.add(serverParticleType);
    }

    public static void load() {
        currentGuiChar = '*';
        blacklistedChars.clear();
        blacklistedChars.add('§');
        blacklistedChars.add('\\');
        INVISIBLE_FRAME = new ParticleFrame(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "invisible"), 0, 16, 16);
    }

    public static void generateAssets(BiConsumer<String, byte[]> assetWriter) {
        var fontBase = new JsonObject();
        var providers = new JsonArray();

        PARTICLE_TEXTURES.forEach((entry) -> {
            var bitmap = new JsonObject();
            bitmap.addProperty("type", "bitmap");
            bitmap.addProperty("file", entry.path.getNamespace() + ":particle/" + entry.path.getPath() + ".png");
            bitmap.addProperty("ascent", entry.ascent);
            bitmap.addProperty("height", entry.height);
            var chars = new JsonArray();
            chars.add(String.valueOf(entry.character));
            bitmap.add("chars", chars);
            providers.add(bitmap);
        });

        fontBase.add("providers", providers);

        assetWriter.accept("assets/" + Lib99j.MOD_ID + "/font/particles.json", fontBase.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static ArrayList<ParticleFrame> loadFrames(Identifier base, int frames, int ascent, int height, int width) {
        ArrayList<ParticleFrame> frames1 = new ArrayList<>();
        for (int i = 0; i < frames; i++) {
            frames1.add(new ParticleFrame(Identifier.fromNamespaceAndPath(base.getNamespace(), base.getPath() + "_" + i), ascent, height, width));
        }
        return frames1;
    }

    private static char getNextGuiChar() {
        char c = currentGuiChar++;
        if (blacklistedChars.contains(c)) getNextGuiChar();
        return c;
    }

    @FunctionalInterface
    public interface QuadFunction<K, V, S, T, R> {
        R accept(K k, V v, S s, T t);
    }

    public static class ParticleFrame {
        public final Identifier path;
        public final int ascent;
        public final int height;
        public final int width;
        final char character;

        public ParticleFrame(Identifier path, int ascent, int height, int width) {
            this.ascent = ascent;
            this.path = path;
            this.height = height;
            this.width = width;
            this.character = getNextGuiChar();
            PARTICLE_TEXTURES.add(this);
        }

        public MutableComponent text() {
            return Component.literal(Character.toString(character)).withStyle(ChatFormatting.WHITE).withStyle(Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "particles"))));
        }
    }

    public record ServerParticleType(Identifier id, Class<? extends ServerParticle> particleClass,
                                     ServerParticleManager.QuadFunction<ServerLevel, Double, Double, Double, ServerParticle> spawner) {
    }
}
