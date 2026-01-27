package com.daniel99j.lib99j;

import com.daniel99j.lib99j.api.ItemUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.ArrayList;
import java.util.Comparator;

public class Lib99jClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "models"), CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, -1)
                .icon(Items.TEST_INSTANCE_BLOCK::getDefaultInstance)
                .title(Component.literal("Item Models"))
                .build()
        );

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "blocks"), CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, -1)
                .icon(Items.TEST_INSTANCE_BLOCK::getDefaultInstance)
                .title(Component.literal("Blocks"))
                .displayItems(((displayContext, entries) -> {
                    ArrayList<Block> blocksWithoutBlockItem = new ArrayList<>();

                    for(Block block : BuiltInRegistries.BLOCK) {
                        blocksWithoutBlockItem.add(block);
                    }

                    for (Item item : BuiltInRegistries.ITEM) {
                        if (item instanceof BlockItem blockItem) {
                            blocksWithoutBlockItem.remove(blockItem.getBlock());
                        }
                    }

                    for(Block block : blocksWithoutBlockItem.stream().sorted(Comparator.comparing(block -> block.properties().id.identifier().toString())).toList()) {
                        if(!block.defaultBlockState().isAir()) {
                            ItemStack stack = Items.REPEATING_COMMAND_BLOCK.getDefaultInstance();
                            stack.set(DataComponents.ITEM_NAME, block.getName());
                            CommandBlockEntity be = new CommandBlockEntity(BlockPos.ZERO, Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState());
                            be.setAutomatic(true);
                            be.setPowered(true);
                            be.conditionMet = true;
                            be.getCommandBlock().setCommand("setblock ~ ~ ~ " + block.properties().id.identifier());
                            TagValueOutput view = TagValueOutput.createWithoutContext(null);
                            be.saveWithFullMetadata(view);
                            stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(BlockEntityType.COMMAND_BLOCK, CustomData.of(view.buildResult()).copyTag()));
                            entries.accept(stack);
                        }
                    }
                }))
                .build()
        );

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "entities"), CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, -1)
                .icon(Items.SKELETON_HORSE_SPAWN_EGG::getDefaultInstance)
                .title(Component.literal("Entities"))
                .displayItems(((displayContext, entries) -> {
                    for(EntityType<?> e : BuiltInRegistries.ENTITY_TYPE) {
                        if(SpawnEggItem.byId(e) == null) {
                            ItemStack i = Items.CHICKEN_SPAWN_EGG.getDefaultInstance();
                            i.set(DataComponents.ENTITY_DATA, TypedEntityData.of(e, CustomData.EMPTY.copyTag()));
                            i.set(DataComponents.ITEM_NAME, e.getDescription().copy().append(" Spawn Egg"));
                            entries.accept(i, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        }
                    }
                }))
                .build()
        );

        ItemGroupEvents.modifyEntriesEvent(ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "models"))).register(entries -> {
            Minecraft.getInstance().getModelManager().bakedItemStackModels.keySet().stream().sorted(Comparator.comparing(Identifier::toString)).toList().forEach((id) -> {
                ItemStack i = ItemUtils.getBasicModelItemStack();
                i.set(DataComponents.ITEM_MODEL, id);
                i.set(DataComponents.ITEM_NAME, Component.literal(id.toString()).withStyle(ChatFormatting.YELLOW));
                entries.accept(i, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            });
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if(client.getWindow().isFullscreen() && FabricLoader.getInstance().isDevelopmentEnvironment()) {
                client.getWindow().toggleFullScreen();
                int x = client.getWindow().findBestMonitor().getX();
                int y = client.getWindow().findBestMonitor().getY();
                if(x <= 0 || y <= 0) return;
                client.getWindow().setWindowed(x, y);
                client.getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.nullToEmpty("Fullscreen disabled"), Component.nullToEmpty("Fullscreen will cause freezes at breakpoints")));
            }
        });
    }
}
