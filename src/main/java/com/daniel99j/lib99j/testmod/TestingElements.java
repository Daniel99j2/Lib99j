package com.daniel99j.lib99j.testmod;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.RegUtil;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.ponder.api.PonderBuilder;
import com.daniel99j.lib99j.ponder.api.PonderGroup;
import com.daniel99j.lib99j.ponder.api.PonderManager;
import com.daniel99j.lib99j.ponder.api.instruction.ExecuteCodeInstruction;
import com.daniel99j.lib99j.ponder.api.instruction.ShowItemInstruction;
import com.daniel99j.lib99j.ponder.api.instruction.ShowTextInstruction;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public class TestingElements {
    public static final Block TEST = RegUtil.registerBlock(
            "test_block",
            TestBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).pushReaction(PushReaction.DESTROY).instabreak().noCollision().mapColor(MapColor.SNOW).sound(SoundType.WOOL)
    );

    public static final Block TEST_POLYMER_ADVANCED_BVE = RegUtil.registerBlock(
            "blockbound_virtual_entity_test",
            PolymerTestPlanetsBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).pushReaction(PushReaction.DESTROY).instabreak().noCollision().mapColor(MapColor.SNOW).sound(SoundType.WOOL)
    );

    public static final PonderBuilder TEST_PONDER = PonderManager.registerBuilder(
            PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j_test", "testponder1"), Items.PISTON.getDefaultInstance(), Component.literal("Pistons push mechanics"), Component.literal("When powered, pistons push up to 12 blocks in front of them. Slime and honey stick to blocks, allowing you to push things horizontal from the piston. This is a test of if text length affects the GUI.")).size(20, 20, 20).defaultBiome(Biomes.FOREST)
                    .instruction(new ExecuteCodeInstruction((scene) -> {
                        scene.getLevel().setBlockAndUpdate(new BlockPos(4, 0, 4), Blocks.PISTON.defaultBlockState());
                    }))
                    .waitFor(1)
                    .instruction(new ExecuteCodeInstruction((scene) -> {
                        scene.getLevel().setBlockAndUpdate(new BlockPos(4, 1, 4), Blocks.REDSTONE_BLOCK.defaultBlockState());
                        Creeper creeper = new Creeper(EntityType.CREEPER, scene.getLevel());
                        creeper.setPosRaw(5, 10, 5);
                        creeper.setPersistenceRequired();
                        creeper.ignite();
                        scene.getLevel().addFreshEntity(creeper);
                        //scene.fastForwardUntil(2);
                    }))
                    .waitFor(1)
                    .instruction(new ShowItemInstruction(1, List.of(Items.PISTON.getDefaultInstance())))
                    .instruction(new ShowTextInstruction(3, Component.literal("hello")))
                    .waitFor(2)
                    .finishStep()
                    .waitFor(1)
                    .instruction(new ShowItemInstruction(1, List.of(Items.STICKY_PISTON.getDefaultInstance())))
                    .instruction(new ExecuteCodeInstruction((scene) -> {
                        Cow creeper = new Cow(EntityType.COW, scene.getLevel());
                        creeper.setPosRaw(5, 10, 5);
                        creeper.setPersistenceRequired();
                        scene.getLevel().addFreshEntity(creeper);

                        scene.getLevel().setBlockAndUpdate(new BlockPos(10, 6, 10), TestingElements.TEST_POLYMER_ADVANCED_BVE.defaultBlockState());

                        scene.getLevel().setBlockAndUpdate(new BlockPos(15, 6, 10), TestingElements.TEST_POLYMER_ADVANCED_BVE.defaultBlockState().setValue(PolymerTestPlanetsBlock.CAN_FALL, true));
                    }))
                    .waitFor(2)
                    .finishStep()
                    .waitFor(1)
                    .instruction(new ShowItemInstruction(1, List.of(Items.TNT.getDefaultInstance())))
                    .instruction(new ExecuteCodeInstruction((scene) -> {
                        Chicken creeper = new Chicken(EntityType.CHICKEN, scene.getLevel());
                        creeper.setPosRaw(5, 10, 5);
                        creeper.setPersistenceRequired();
                        scene.getLevel().addFreshEntity(creeper);

                        scene.getLevel().setBlockAndUpdate(new BlockPos(5, 6, 5), Blocks.LAVA.defaultBlockState());
                    }))
                    .waitFor(10)
                    .finishStep()
                    .waitFor(30)
                    .finishStep()
                    .build()
    );

    public static final PonderBuilder TEST_ITEM_PONDER = PonderManager.registerItemToBuilder(Items.TNT,
            PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j_test", "tnt_explodes"), Items.TNT.getDefaultInstance(), Component.literal("TNT explosions"), Component.literal("When TNT is lit and not in water, after a short delay it explodes!")).size(5, 5, 5).defaultBiome(Biomes.BADLANDS).floorBlocks(Blocks.TERRACOTTA.defaultBlockState(), Blocks.TERRACOTTA.defaultBlockState())
                    .instruction(new ExecuteCodeInstruction((scene) -> {
                        scene.setCanBreakFloor(true);
                        scene.getLevel().setBlockAndUpdate(new BlockPos(2, 0, 2), Blocks.TNT.defaultBlockState());
                    }))
                    .waitFor(3)
                    .instruction(new ExecuteCodeInstruction((scene) -> {
                        scene.getLevel().setBlockAndUpdate(new BlockPos(3, 0, 2), Blocks.REDSTONE_TORCH.defaultBlockState());
                    }))
                    .waitFor(6)
                    .finishStep()
                    .build()
    );

    public static final PonderBuilder TEST_ITEM_PONDER2 = PonderManager.registerItemToBuilder(Items.TNT,
            PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j_test", "tnt_doesnt_explode_underwater"), Items.WATER_BUCKET.getDefaultInstance(), Component.literal("TNT water interactions"), Component.literal("When a TNT is lit in the water, it does not break blocks")).size(5, 5, 5).defaultBiome(Biomes.BADLANDS).floorBlocks(Blocks.CLAY.defaultBlockState(), Blocks.CLAY.defaultBlockState())
                    .instruction(new ExecuteCodeInstruction((scene) -> {
                        scene.getLevel().fillBlocksAndUpdate(new BlockPos(-5, -5, -5), new BlockPos(10, 10, 10), Blocks.WATER.defaultBlockState());
                        scene.setCanBreakFloor(true);
                        scene.getLevel().setBlockAndUpdate(new BlockPos(2, 0, 2), Blocks.TNT.defaultBlockState());
                    }))
                    .waitFor(3)
                    .instruction(new ExecuteCodeInstruction((scene) -> {
                        scene.getLevel().setBlockAndUpdate(new BlockPos(3, 0, 2), Blocks.REDSTONE_BLOCK.defaultBlockState());
                    }))
                    .waitFor(6)
                    .finishStep()
                    .build()
    );

    public static final PonderBuilder LIGHT_LEVEL_PONDER = PonderManager.registerItemToBuilder(Items.DAYLIGHT_DETECTOR,
            PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j_test", "light_level"), Items.GLOWSTONE.getDefaultInstance(), Component.literal("Daylight detectors"), Component.literal("When in sunlight, daylight detectors emit a signal")).size(50, 50, 50)
                    .instruction(new ExecuteCodeInstruction((scene) -> {
                        scene.getLevel().fillBlocksAndUpdate(BlockPos.ZERO, new BlockPos(50, 0, 50), Blocks.DAYLIGHT_DETECTOR.defaultBlockState());
                        scene.getLevel().fillBlocksAndUpdate(BlockPos.ZERO.above(), new BlockPos(50, 1, 50), Blocks.ACACIA_FENCE_GATE.defaultBlockState());
                    }))
                    .waitFor(6)
                    .finishStep()
                    .build()
    );

    public static final PonderGroup MANY_PONDERS_GROUP = PonderManager.registerGroup(new PonderGroup(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "large_group"), Items.BUNDLE.getDefaultInstance(), new ArrayList<>(List.of(TEST_PONDER, TEST_ITEM_PONDER2, TEST_ITEM_PONDER))));

    public static final PonderGroup TNT_PONDERS_GROUP = PonderManager.registerGroup(new PonderGroup(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "tnt_group"), Items.TNT.getDefaultInstance(), new ArrayList<>(List.of(TEST_ITEM_PONDER2, TEST_ITEM_PONDER))));

    public static final GuiElementBuilder TEST_UI_ITEM = GuiUtils.generateTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ui/testing_element_item"));
    public static final GuiElementBuilder TEST_VANILLA_GUI_ITEM = GuiUtils.generateTexture(Identifier.withDefaultNamespace("gui/sprites/container/slot/sword"));

    public static final Item TEST_BLOCKITEM = RegUtil.registerBlockItem(TEST);

    public static void init() {}
}
