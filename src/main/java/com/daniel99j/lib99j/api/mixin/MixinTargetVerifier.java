package com.daniel99j.lib99j.api.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

public class MixinTargetVerifier {
    public static void verify(String modId) {
        try {
            JsonObject object = JsonParser.parseString(Files.readString(FabricLoader.getInstance().getModContainer(modId).get().findPath(modId + ".mixins.json").get())).getAsJsonObject();
            String _package = object.get("package").getAsString();
            for (Path mixinFile : FabricLoader.getInstance().getModContainer(modId).get().findPath("src/main/" + _package.replace(".", "/")+".class").get()) {
                Class<?> mixinClass = Class.forName(_package+mixinFile.toString().replace("/", ".").replace(".class", ""));

                for (Method method : mixinClass.getMethods()) {
                    MixinTargetVerify v = method.getAnnotation(MixinTargetVerify.class);
                    if(v != null) {
                        //method.getAnnotation(Inject.class).target()[0].id()
                    }
                }
            }
        } catch (Exception e) {

        }
    }
}
