package com.daniel99j.lib99j.api;

import net.minecraft.core.registries.BuiltInRegistries;

public class ServerConvertorUtil {
    public static void makeItemsPolymer(String namespace) {
        BuiltInRegistries.ITEM.stream().toList().forEach(item -> {
//            if(Registries.ITEM.getKey(item).get().getValue().getNamespace() == namespace && !(item instanceof PolymerItem)) {
//                PolymerItemUtils.registerOverlay(item, new SimplePolymerItem(item.));
//            }
        });
    }
}
