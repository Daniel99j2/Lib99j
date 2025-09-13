package com.daniel99j.lib99j;

import com.daniel99j.lib99j.api.EntityUtils;
import com.daniel99j.lib99j.api.ItemUtils;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;

public class Lib99jClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Registry.register(Registries.ITEM_GROUP, Identifier.of(Lib99j.MOD_ID, "models"), ItemGroup.create(ItemGroup.Row.BOTTOM, -1)
                .icon(Items.TEST_INSTANCE_BLOCK::getDefaultStack)
                .displayName(Text.literal("Item Models"))
                .build()
        );

        Registry.register(Registries.ITEM_GROUP, Identifier.of(Lib99j.MOD_ID, "blocks"), ItemGroup.create(ItemGroup.Row.BOTTOM, -1)
                .icon(Items.TEST_INSTANCE_BLOCK::getDefaultStack)
                .displayName(Text.literal("Blocks"))
                .entries(((displayContext, entries) -> {
                    ArrayList<Block> blocksWithoutBlockItem = new ArrayList<>();

                    for(Block block : Registries.BLOCK) {
                        blocksWithoutBlockItem.add(block);
                    }

                    for (Item item : Registries.ITEM) {
                        if (item instanceof BlockItem blockItem) {
                            blocksWithoutBlockItem.remove(blockItem.getBlock());
                        }
                    }

                    for(Block block : blocksWithoutBlockItem.stream().sorted(Comparator.comparing(block -> block.getSettings().registryKey.getValue().toString())).toList()) {
                        if(!block.getDefaultState().isAir()) {
                            ItemStack stack = Items.REPEATING_COMMAND_BLOCK.getDefaultStack();
                            stack.set(DataComponentTypes.ITEM_NAME, block.getName());
                            CommandBlockBlockEntity be = new CommandBlockBlockEntity(BlockPos.ORIGIN, Blocks.REPEATING_COMMAND_BLOCK.getDefaultState());
                            be.setAuto(true);
                            be.setPowered(true);
                            be.conditionMet = true;
                            be.getCommandExecutor().setCommand("setblock ~ ~ ~ " + block.getSettings().registryKey.getValue());
                            NbtWriteView view = NbtWriteView.create(null);
                            be.writeFullData(view);
                            stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(view.getNbt()));
                            entries.add(stack);
                        }
                    }
                }))
                .build()
        );

        Registry.register(Registries.ITEM_GROUP, Identifier.of(Lib99j.MOD_ID, "entities"), ItemGroup.create(ItemGroup.Row.BOTTOM, -1)
                .icon(Items.SKELETON_HORSE_SPAWN_EGG::getDefaultStack)
                .displayName(Text.literal("Entities"))
                .entries(((displayContext, entries) -> {
                    for(EntityType<?> e : Registries.ENTITY_TYPE) {
                        Entity entity = EntityUtils.getEntityFromType(e);
                        if(!(entity instanceof MobEntity) || (entity instanceof MobEntity && SpawnEggItem.forEntity(e) == null)) {
                            ItemStack i = Items.CHICKEN_SPAWN_EGG.getDefaultStack();
                            NbtCompound nbt = new NbtCompound();
                            nbt.putString("id", EntityType.getId(e).toString());
                            i.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(nbt));
                            i.set(DataComponentTypes.ITEM_NAME, e.getName().copy().append(" Spawn Egg"));
                            entries.add(i, ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
                        }
                    }
                }))
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
