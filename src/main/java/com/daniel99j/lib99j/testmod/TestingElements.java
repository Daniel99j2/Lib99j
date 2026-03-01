package com.daniel99j.lib99j.testmod;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.RegUtil;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.ponder.api.PonderBuilder;
import com.daniel99j.lib99j.ponder.api.PonderManager;
import com.daniel99j.lib99j.ponder.api.instruction.ExecuteCodeInstruction;
import com.daniel99j.lib99j.ponder.api.instruction.ShowItemInstruction;
import com.daniel99j.lib99j.ponder.api.instruction.ShowLineInstruction;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.ApiStatus;

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

    public static final PonderBuilder TEST_PONDER = PonderManager.registerIdToBuilder(Identifier.fromNamespaceAndPath("lib99j_test", "testponder1"),
            PonderBuilder.create().title("Dev ponder menu 1").size(20, 20, 20).defaultBiome(Biomes.FOREST)
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
                    .instruction(new ShowItemInstruction(1, Items.PISTON.getDefaultInstance()))
                    .instruction(new ShowLineInstruction(1, 0xFF0000, Vec2.ZERO, new Vec2(20, 10), 10))
                    .waitFor(2)
                    .finishStep()
                    .waitFor(1)
                    .instruction(new ShowItemInstruction(1, Items.STICKY_PISTON.getDefaultInstance()))
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
                    .instruction(new ShowItemInstruction(1, Items.TNT.getDefaultInstance()))
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

    public static final PonderBuilder TEST_ITEM_PONDER = PonderManager.registerItemToBuilder(Items.TNT, Identifier.fromNamespaceAndPath("lib99j_test", "tnt_explodes"),
            PonderBuilder.create().title("Tnt explodes!").size(5, 5, 5).defaultBiome(Biomes.BADLANDS).floorBlocks(Blocks.TERRACOTTA.defaultBlockState(), Blocks.TERRACOTTA.defaultBlockState())
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

    public static final PonderBuilder TEST_ITEM_PONDER2 = PonderManager.registerItemToBuilder(Items.TNT, Identifier.fromNamespaceAndPath("lib99j_test", "tnt_doesnt_explode_underwater"),
            PonderBuilder.create().title("Tnt doesn't explode underwater").size(5, 5, 5).defaultBiome(Biomes.BADLANDS).floorBlocks(Blocks.CLAY.defaultBlockState(), Blocks.CLAY.defaultBlockState())
                    .instruction(new ExecuteCodeInstruction((scene) -> {
                        scene.getLevel().fillBlocks(new BlockPos(-5, -5, -5), new BlockPos(10, 10, 10), Blocks.WATER.defaultBlockState());
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

    public static final GuiElementBuilder TEST_UI_ITEM = GuiUtils.generateTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ui/testing_element_item"));
    public static final GuiElementBuilder TEST_VANILLA_GUI_ITEM = GuiUtils.generateTexture(Identifier.withDefaultNamespace("gui/sprites/container/slot/sword"));

    public static final Item TEST_BLOCKITEM = RegUtil.registerBlockItem(TEST);

    public static void init() {}
}
