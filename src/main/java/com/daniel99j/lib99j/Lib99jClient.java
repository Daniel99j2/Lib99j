package com.daniel99j.lib99j;

import com.daniel99j.lib99j.api.ItemUtils;
import com.daniel99j.lib99j.api.ServerConfigCopy;
import com.daniel99j.lib99j.impl.network.ClientboundLib99jPonderItemsPacket;
import com.mojang.blaze3d.platform.InputConstants;
import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.other.PacketTooltipContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.storage.TagValueOutput;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Lib99jClient implements ClientModInitializer {
    private static int ponderHoldAmount = 0;
    private static Identifier hoveredPonderItem = null;
    private static final List<Identifier> ponderItems = new ArrayList<>();
    public static final List<ItemStack> storedItems = new ArrayList<>();
    public static KeyMapping ponderKey;
    private static boolean ponderKeyWasHeld = false;

    @Override
    public void onInitializeClient() {
        ponderKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.lib99j.ponder",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_W,
                        KeyMapping.Category.INVENTORY
                ));

        if(Lib99j.CONFIG.getClient().extraItemGroups) {
            PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "models"), CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, -1)
                    .icon(Items.TEST_INSTANCE_BLOCK::getDefaultInstance)
                    .title(Component.literal("Item Models"))
                    .build()
            );

            PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "storage"), CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, -1)
                    .icon(Items.BUNDLE::getDefaultInstance)
                    .title(Component.literal("Stored items"))
                    .build()
            );


            PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "blocks"), CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, -1)
                    .icon(Items.TEST_INSTANCE_BLOCK::getDefaultInstance)
                    .title(Component.literal("Blocks"))
                    .displayItems(((displayContext, entries) -> {
                        ArrayList<Block> blocksWithoutBlockItem = new ArrayList<>();

                        for (Block block : BuiltInRegistries.BLOCK) {
                            blocksWithoutBlockItem.add(block);
                        }

                        for (Item item : BuiltInRegistries.ITEM) {
                            if (item instanceof BlockItem blockItem) {
                                blocksWithoutBlockItem.remove(blockItem.getBlock());
                            }
                        }

                        for (Block block : blocksWithoutBlockItem.stream().sorted(Comparator.comparing(block -> block.properties().id.identifier().toString())).toList()) {
                            if (!block.defaultBlockState().isAir()) {
                                ItemStack stack = Items.COMMAND_BLOCK.getDefaultInstance();
                                stack.set(DataComponents.ITEM_NAME, block.getName());
                                CommandBlockEntity be = new CommandBlockEntity(BlockPos.ZERO, Blocks.COMMAND_BLOCK.defaultBlockState());
                                be.setAutomatic(true);
                                be.setChanged();
                                be.getCommandBlock().setCommand("setblock ~ ~ ~ " + block.properties().id.identifier());
                                TagValueOutput view = TagValueOutput.createWithoutContext(null);
                                be.saveWithFullMetadata(view);
                                stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(BlockEntityType.COMMAND_BLOCK, CustomData.of(view.buildResult()).copyTag()));
                                stack.set(DataComponents.ITEM_MODEL, Identifier.withDefaultNamespace("test_instance_block"));
                                entries.accept(stack);
                            }
                        }
                    }))
                    .build()
            );

            PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "entities"), CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, -1)
                    .icon(Items.SKELETON_HORSE_SPAWN_EGG::getDefaultInstance)
                    .title(Component.literal("Entities"))
                    .displayItems(((displayContext, entries) -> {
                        for (EntityType<?> e : BuiltInRegistries.ENTITY_TYPE) {
                            if (SpawnEggItem.byId(e).isEmpty()) {
                                ItemStack i = Items.CHICKEN_SPAWN_EGG.getDefaultInstance();
                                i.set(DataComponents.ENTITY_DATA, TypedEntityData.of(e, CustomData.EMPTY.copyTag()));
                                i.set(DataComponents.ITEM_NAME, e.getDescription().copy().append(" Spawn Egg"));
                                entries.accept(i, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                            }
                        }
                    }))
                    .build()
            );

            CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "models"))).register(entries -> {
                Minecraft.getInstance().getModelManager().bakedItemStackModels.keySet().stream().sorted(Comparator.comparing(Identifier::toString)).toList().forEach((id) -> {
                    ItemStack i = ItemUtils.getBasicModelItemStack();
                    i.set(DataComponents.ITEM_MODEL, id);
                    i.set(DataComponents.ITEM_NAME, Component.literal(id.toString()).withStyle(ChatFormatting.YELLOW));
                    entries.accept(i, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                });
            });

            CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "storage"))).register(entries -> {
                ItemStack reload = Items.COMPASS.getDefaultInstance();
                reload.set(DataComponents.CREATIVE_SLOT_LOCK, Unit.INSTANCE);
                reload.set(DataComponents.ITEM_NAME, Component.literal("Reload list"));
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("lib99j$reloadStorage", true);
                reload.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                entries.accept(reload);
            });
        }

        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            //PacketTooltipContext is from Polymer but it adds extra lines I dont want
            if(!(context instanceof PacketTooltipContext) && Lib99j.CONFIG.getClient().ponderTooltip && ServerConfigCopy.getConfigOption(Lib99j.MOD_ID, "easy_ponder", Boolean.class, false) && Minecraft.getInstance().player != null) {
                hoveredPonderItem = PolymerItemUtils.getPolymerIdentifier(stack) == null ? BuiltInRegistries.ITEM.getKey(stack.getItem()) : PolymerItemUtils.getPolymerIdentifier(stack);

                if(ponderItems.contains(hoveredPonderItem)) {
                    MutableComponent bar = Component.literal(" ");
                    int max = 25;
                    if (ponderHoldAmount > 0)
                        bar.append(Component.literal("|".repeat(ponderHoldAmount)).withStyle(ChatFormatting.GRAY));
                    if (max - ponderHoldAmount > 0)
                        bar.append(Component.literal("|".repeat(max - ponderHoldAmount)).withStyle(ChatFormatting.DARK_GRAY));

                    MutableComponent text = Component.translatable("ponder.hold", Component.keybind("key.lib99j.ponder")).withStyle(ChatFormatting.GRAY).append(bar);

                    if(flag.isAdvanced()) {
                        int i = 0;
                        int idPos = 100;
                        for (Component line : lines) {
                            if(line.getString().equals(hoveredPonderItem.toString())) idPos = i;
                            i++;
                        }
                        if(idPos == 100) lines.add(Component.literal("Error adding ponder tooltip! Report to Daniel99j!").withStyle(ChatFormatting.RED));
                        else lines.add(idPos, text);
                    } else lines.add(text);
                } else {
                    ponderHoldAmount = 0;
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if(!ponderKeyWasHeld) {
                 if(ponderHoldAmount > 0) {
                    ponderHoldAmount-=1;
                }
            }
            if(ponderHoldAmount >= 25) {
                ponderHoldAmount = 0;
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.connection.sendCommand("ponder item "+hoveredPonderItem.toString());
                Minecraft.getInstance().player.closeContainer();
            }
            ponderKeyWasHeld = false;
        });

        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) ClientPlayNetworking.registerGlobalReceiver(ClientboundLib99jPonderItemsPacket.ID, (payload, context) -> {
            if(!Lib99j.CONFIG.getCommon().allowSyncing) return;
            ponderItems.clear();
            ponderItems.addAll(payload.items());
        });

        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> {
            ponderItems.clear();
        });
    }

    public static void handlePonderKey(boolean pressed) {
        if (pressed) {
            ponderKeyWasHeld = true;
            ponderHoldAmount++;
        }
    }

    public static void addItem(ItemStack stack) {
        if(stack.isEmpty() || storedItems.contains(stack) || stack.getComponentsPatch().entrySet().isEmpty()) return;
        if(storedItems.size() > 100) storedItems.removeFirst();
        storedItems.add(stack);
    }
}
