package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.fabric.impl.resource.pack.ModNioPackResources;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.server.translations.api.language.ServerLanguageDefinition;
import xyz.nucleoid.server.translations.impl.ServerTranslations;
import xyz.nucleoid.server.translations.impl.language.LanguageReader;
import xyz.nucleoid.server.translations.impl.language.TranslationMap;
import xyz.nucleoid.server.translations.impl.language.TranslationStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mixin(LanguageReader.class)
public abstract class LanguageReaderMixin {

//    @ModifyExpressionValue(
//            method = "collectDataPackTranslations",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/Resource;open()Ljava/io/InputStream;")
//    )
//    private List<ServerPlayer> modifyState(List<ServerPlayer> original) {
//        List<ServerPlayer> newList = new ArrayList<>(original);
//        newList.addAll(Lib99j.additionalPlayers);
//        return newList;
//    }


    @Inject(method = "collectDataPackTranslations", at = @At("TAIL"))
    private static void addAssetTranslationFiles(ResourceManager manager, CallbackInfoReturnable<Multimap<String, Supplier<TranslationMap>>> cir) {
        if(manager instanceof MultiPackResourceManager multi) {
            multi.listPacks().forEach((pack) -> {
                if(pack instanceof ModNioPackResources modNioPackResources) {
                    modNioPackResources.getNamespaces(PackType.CLIENT_RESOURCES);
                    modNioPackResources.listResources(PackType.CLIENT_RESOURCES, "lib99j", "lang", (id, packResource) -> {
                        Lib99j.LOGGER.info(id.toString());
                });
                }
//                pack.listResources(PackType.CLIENT_RESOURCES, "lib99j", "lang", (packName, packResource) -> {
//                    Lib99j.LOGGER.info();
//                    multi.listResources()
//                });
            });
        }
        manager.listResources("lib99j", (id) -> true).forEach((id, resourec) -> {
            Lib99j.LOGGER.info(id.getNamespace());
        });
//        List<String> ids = new ArrayList<>();
//        ids.add("minecraft");
//
//        for (String id : ids) {
//            FabricLoader.getInstance().getModContainer(id).ifPresent(modContainer -> {
//                for (Path rootPath : modContainer.getRootPath()) {
//                    Path assets = rootPath.resolve("assets");
//                    if (Files.isDirectory(assets)) {
//                        try (Stream<Path> idAssetDirs = Files.walk(assets, 1)) {
//                            idAssetDirs.forEach((path -> {
//                                Path langFolder = assets.resolve("lang");
//                                try (Stream<Path> singleLangFile = Files.walk(langFolder, 1)) {
//                                    String key = ServerTranslations.INSTANCE.getCodeAlias("en_us");
//                                    if(!cir.getReturnValue().containsKey(key)) cir.getReturnValue().put(key, new )
//                                    cir.getReturnValue().(ServerTranslations.INSTANCE.getCodeAlias("en_us")).
//                                } catch (IOException e) {
//                                    throw new RuntimeException(e);
//                                }
//                            }));
//
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                }
//            });
//        }
//
//        String code = ;
//        translationSuppliers.put(code, () -> {
//            TranslationMap map = new TranslationMap();
//            try {
//                for (Resource resource : manager.getResourceStack(path)) {
//                    map.putAll(read(resource.open()));
//                }
//            } catch (RuntimeException | IOException e) {
//                ServerTranslations.LOGGER.warn("Failed to load language resource at {}", path, e);
//            }
//            return map;
//        });
//
//        this.translations.get()
//        if(!Lib99j.SUPPORTED_LANGUAGES.contains(definition.code())) {
//            Lib99j.LOGGER.warn("Your system's language is set to {}, which is not officially supported by Lib99j and may cause issues", definition.code());
//        }
    }
}