package com.daniel99j.lib99j.impl.datagen;

import com.daniel99j.lib99j.api.CustomEvents;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class Lib99jDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        CustomEvents.GAME_LOADED.invoke();
        var pack = fabricDataGenerator.createPack();
        pack.addProvider(AssetProvider::new);
        CustomEvents.DATAGEN_RUN.invoke(fabricDataGenerator);
    }
}
