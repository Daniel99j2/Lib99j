package com.daniel99j.lib99j.api;

import net.minecraft.registry.Registries;

public class ServerConvertorUtil {
    public static void makeItemsPolymer(String namespace) {
        Registries.ITEM.stream().toList().forEach(item -> {
//            if(Registries.ITEM.getKey(item).get().getValue().getNamespace() == namespace && !(item instanceof PolymerItem)) {
//                PolymerItemUtils.registerOverlay(item, new SimplePolymerItem(item.));
//            }
        });
    }
}
