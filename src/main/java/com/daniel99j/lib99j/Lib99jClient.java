package com.daniel99j.lib99j;

import com.daniel99j.lib99j.api.ItemUtils;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;

public class Lib99jClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArrayList<Block> blocksWithoutBlockItem = new ArrayList<>();

        for(Block block : Registries.BLOCK) {
            blocksWithoutBlockItem.add(block);
        }

        for (Item item : Registries.ITEM) {
            if (item instanceof BlockItem blockItem) {
                blocksWithoutBlockItem.remove(blockItem.getBlock());
            }
        }

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
            for(Block block : blocksWithoutBlockItem.stream().sorted(Comparator.comparing(block -> block.getSettings().registryKey.getValue().toString())).toList()) {
                if(!block.getDefaultState().isAir()) {
                    ItemStack stack = ItemUtils.fromString(PolymerCommonUtils.getFakeWorld().getRegistryManager(), "{components: {\"minecraft:block_entity_data\": {id:\"minecraft:command_block\", auto: 1b, Command: \"setblock ~ ~ ~ "+ Registries.BLOCK.getId(block).getNamespace() + ":" + Registries.BLOCK.getId(block).getPath()+ "\", SuccessCount: 0, TrackOutput: 1b, UpdateLastExecution: 1b}, \"minecraft:item_model\": \"minecraft:jigsaw\"}, count: 1, id: \"minecraft:command_block\"}");
                    stack.set(DataComponentTypes.ITEM_NAME, block.getName());
                    entries.add(stack);
                }
            }
        });

        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(Lib99j.MOD_ID, "models"), ItemGroup.create(ItemGroup.Row.BOTTOM, -1)
                .icon(Items.TEST_INSTANCE_BLOCK::getDefaultStack)
                .displayName(Text.literal("Item Models"))
                .build()
        );

        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(Lib99j.MOD_ID, "models"))).register(entries -> {
            MinecraftClient.getInstance().getBakedModelManager().bakedItemModels.keySet().stream().sorted(Comparator.comparing(Identifier::toString)).toList().forEach((id) -> {
                ItemStack i = ItemUtils.getBasicModelItemStack();
                i.set(DataComponentTypes.ITEM_MODEL, id);
                i.set(DataComponentTypes.ITEM_NAME, Text.literal(id.toString()).formatted(Formatting.YELLOW));
                entries.add(i, ItemGroup.StackVisibility.PARENT_TAB_ONLY);
            });
        });
    }
}
