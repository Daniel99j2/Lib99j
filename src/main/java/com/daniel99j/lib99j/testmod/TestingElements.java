package com.daniel99j.lib99j.testmod;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.GenericScreenEffect;
import com.daniel99j.lib99j.api.RegUtil;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.ponder.api.*;
import com.daniel99j.lib99j.ponder.api.instruction.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector2i;

import java.util.List;

@ApiStatus.Internal
public class TestingElements {
    public static final Item TESTITEM = RegUtil.registerItem(
            "test_item"
    );

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

    public static final ItemStackTemplate TEST_UI_ITEM = GuiUtils.generateTexture(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "ui/testing_element_item"));
    public static final ItemStackTemplate TEST_VANILLA_GUI_ITEM = GuiUtils.generateTexture(Identifier.withDefaultNamespace("gui/sprites/container/slot/sword"));

    public static final Item TEST_BLOCKITEM = RegUtil.registerBlockItem(TEST);

    public static void init() {
        PonderManager.REGISTER.register((r) -> {
            var TEST_PONDER = r.registerBuilder(
                    PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j_test", "testponder1"), new ItemStackTemplate(Items.PISTON), Component.literal("Pistons push mechanics"), Component.literal("When powered, pistons push up to 12 blocks in front of them. Slime and honey stick to blocks, allowing you to push things horizontal from the piston. This is a test of if text length affects the GUI.")).size(20, 20, 20).defaultBiome(Biomes.FOREST)
                            .instruction(new ExecuteCodeInstruction((scene) -> {
                                scene.getLevel().setBlockAndUpdate(new BlockPos(4, 0, 4), Blocks.PISTON.defaultBlockState());
                            }))
                            .waitFor(1)
                            .instruction(new ExecuteCodeInstruction((scene) -> {
                                scene.getLevel().setBlockAndUpdate(new BlockPos(4, 10, 4), Blocks.ANVIL.defaultBlockState());

                                scene.getLevel().setBlockAndUpdate(new BlockPos(4, 1, 4), Blocks.REDSTONE_BLOCK.defaultBlockState());
                                Creeper creeper = new Creeper(EntityType.CREEPER, scene.getLevel()) {
                                    @Override
                                    public void tick() {
                                        super.tick();
                                        if (scene.player.getMainHandItem().is(Items.ARROW))
                                            throw new IllegalStateException("Test exception!");
                                    }
                                };
                                creeper.setPosRaw(5, 8, 5);
                                creeper.setPersistenceRequired();
                                creeper.ignite();
                                scene.getLevel().addFreshEntity(creeper);
                                //scene.fastForwardUntil(2);\
                                scene.getLevel().setBlockAndUpdate(new BlockPos(6, 1, 4), Blocks.COPPER_GOLEM_STATUE.defaultBlockState().setValue(CopperGolemStatueBlock.POSE, CopperGolemStatueBlock.Pose.STAR));
                                scene.getLevel().setBlockAndUpdate(new BlockPos(6, 1, 6), Blocks.BLACK_BANNER.defaultBlockState());
                                scene.getLevel().setBlockEntity(new BannerBlockEntity(new BlockPos(6, 1, 6).offset(scene.getOrigin()), Blocks.BLACK_BANNER.defaultBlockState(), DyeColor.BROWN));
                            }))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.PISTON)), new Vector2i(790, 367), PonderLine.RIGHT))
                            .instruction(new ShowTextInstruction(3, Component.translatable("lib99j.test"), new Vector2i(640, 236), PonderLine.LEFT))
                            .waitFor(2)
                            .finishStep()
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.STICKY_PISTON), new ItemStackTemplate(Items.SLIME_BLOCK)), new Vector2i(798, 367), PonderLine.RIGHT))
                            .instruction(new ExecuteCodeInstruction((scene) -> {
                                scene.getLevel().removeBlockWithParticles(new BlockPos(4, 0, 4));

                                Cow creeper = new Cow(EntityType.COW, scene.getLevel());
                                //scene.getLevel().makeEntityDumb(creeper);
                                creeper.setPosRaw(5, 10, 5);
                                creeper.setPersistenceRequired();
                                scene.getLevel().addFreshEntity(creeper);

                                scene.getLevel().setBlockAndUpdate(new BlockPos(10, 6, 10), TestingElements.TEST_POLYMER_ADVANCED_BVE.defaultBlockState());

                                scene.getLevel().setBlockAndUpdate(new BlockPos(15, 6, 10), TestingElements.TEST_POLYMER_ADVANCED_BVE.defaultBlockState().setValue(PolymerTestPlanetsBlock.CAN_FALL, true));
                            }))
                            .waitFor(2)
                            .finishStep()
                            .waitFor(1)
                            .instruction(new ExecuteCodeInstruction((scene) -> {
                                Chicken creeper = new Chicken(EntityType.CHICKEN, scene.getLevel());
                                creeper.setPosRaw(10, 5, 10);
                                creeper.setPersistenceRequired();
                                scene.getLevel().addFreshEntity(creeper);
                                scene.getLevel().makeEntityPathfindTo(creeper, BlockPos.ZERO.offset(0, 0, 1));

                                scene.getLevel().setBlockAndUpdate(new BlockPos(5, 6, 5), Blocks.LAVA.defaultBlockState());
                            }))
                            .waitFor(2)
                            .instruction(new ShowTextInstruction(10, Component.literal("TNT is also cool, and\nalso changes blocks"), new Vector2i(798, 367), PonderLine.LEFT))
                            .waitFor(8)
                            .finishStep()
                            .waitFor(30)
                            .finishStep()
                            .build()
            );

            r.registerBuilder(PonderBuilder.create(Identifier.withDefaultNamespace("icecream"), new ItemStackTemplate(Items.EXPOSED_CHISELED_COPPER), Component.literal("Vanilla"), Component.empty()).waitFor(10).finishStep().build());

            r.registerItemToBuilder(Items.STICK,
                    PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j_test", "animated_rebuild"), new ItemStackTemplate(Items.STICK), Component.literal("Animated rebuild"), Component.literal("")).size(5, 5, 5)
                            .instruction(new BeginAnimatedRebuildInstruction())
                            .instruction(new ExecuteCodeInstruction((scene) -> {
                                scene.getLevel().setBlockAndUpdate(new BlockPos(2, 0, 2), Blocks.TNT.defaultBlockState());
                                scene.getLevel().setBlockAndUpdate(new BlockPos(1, 3, 1), Blocks.FARMLAND.defaultBlockState());
                                scene.getLevel().setBlockAndUpdate(new BlockPos(0, 0, 0), Blocks.VAULT.defaultBlockState());
                                scene.getLevel().fillBlocks(BlockPos.ZERO, new BlockPos(4, 4, 4), Blocks.GLASS.defaultBlockState(), false);
                            }))
                            .instruction(new AnimatedRebuildInstruction(5, 126))
                            .instruction(new ExecuteCodeInstruction((scene) -> {
                                scene.getLevel().setBlockAndUpdate(new BlockPos(3, 0, 2), Blocks.REDSTONE_TORCH.defaultBlockState());
                            }))
                            .waitFor(6)
                            .finishStep()
                            .build()
            );

            var TEST_ITEM_PONDER2 = r.registerItemToBuilder(Items.TNT,
                    PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j_test", "tnt_explodes"), new ItemStackTemplate(Items.TNT), Component.literal("TNT explosions"), Component.literal("When TNT is lit and not in water, after a short delay it explodes!")).size(5, 5, 5).defaultBiome(Biomes.BADLANDS).floorBlocks(Blocks.TERRACOTTA.defaultBlockState(), Blocks.TERRACOTTA.defaultBlockState())
                            .instruction(new ExecuteCodeInstruction((scene) -> {
                                scene.setCanEscapeBounds(true);
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

            r.registerItemToBuilder(Items.TNT,
                    PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j_test", "tnt_doesnt_explode_underwater"), new ItemStackTemplate(Items.WATER_BUCKET), Component.literal("TNT water interactions"), Component.literal("When a TNT is lit in the water, it does not break blocks")).size(5, 5, 5).defaultBiome(Biomes.BADLANDS).floorBlocks(Blocks.CLAY.defaultBlockState(), Blocks.CLAY.defaultBlockState())
                            .instruction(new AddVfxInstruction(GenericScreenEffect.CONDUIT_POWER, 100000))
                            .instruction(new ExecuteCodeInstruction((scene) -> {
                                scene.getLevel().fillBlocksAndUpdate(new BlockPos(-5, -5, -5), new BlockPos(10, 10, 10), Blocks.WATER.defaultBlockState());
                                scene.setCanEscapeBounds(true);
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

            var END_CRYSTAL_PONDER2 = r.registerItemToBuilder(Items.END_CRYSTAL,
                    PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j_test", "end_crystals_explode"), new ItemStackTemplate(Items.END_CRYSTAL), Component.literal("End Crystals"), Component.literal("End crystals explode when punched or hit with a projectile")).defaultBiome(Biomes.END_BARRENS)
                            .instruction(new ExecuteCodeInstruction((scene) -> {
                                EndCrystal crystal = EntityType.END_CRYSTAL.create(scene.getLevel(), EntitySpawnReason.SPAWN_ITEM_USE);
                                crystal.setPos(new Vec3(2, 2, 2));
                                scene.getLevel().addFreshEntity(crystal);
                            }))
                            .waitFor(3)
                            .instruction(new ExecuteCodeInstruction((scene) -> {
                                Arrow crystal = EntityType.ARROW.create(scene.getLevel(), EntitySpawnReason.SPAWN_ITEM_USE);
                                crystal.setPos(new Vec3(2, 10, 2));
                                scene.getLevel().addFreshEntity(crystal);
                            }))
                            .waitFor(6)
                            .finishStep()
                            .build()
            );

            var TEST_ITEM_PONDER = r.registerItemToBuilder(Items.DAYLIGHT_DETECTOR,
                    PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j_test", "light_level"), new ItemStackTemplate(Items.GLOWSTONE), Component.literal("Daylight detectors"), Component.literal("When in sunlight, daylight detectors emit a signal")).size(50, 50, 50)
                            .instruction(new ExecuteCodeInstruction((scene) -> {
                                scene.getLevel().fillBlocksAndUpdate(BlockPos.ZERO, new BlockPos(50, 0, 50), Blocks.DAYLIGHT_DETECTOR.defaultBlockState());
                                scene.getLevel().fillBlocksAndUpdate(BlockPos.ZERO.above(), new BlockPos(50, 1, 50), Blocks.ACACIA_FENCE_GATE.defaultBlockState());
                            }))
                            .waitFor(6)
                            .finishStep()
                            .build()
            );

            var LINES = r.registerBuilder(
                    PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j_test", "lines"), new ItemStackTemplate(Items.WHITE_DYE), Component.literal("Lines"), Component.literal("UI Testing"))
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT)), new Vector2i(10), PonderLine.RIGHT))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT)), new Vector2i(20), PonderLine.RIGHT))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT)), new Vector2i(30), PonderLine.RIGHT))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT)), new Vector2i(40), PonderLine.RIGHT))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT)), new Vector2i(50), PonderLine.RIGHT))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT)), new Vector2i(60), PonderLine.RIGHT))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT)), new Vector2i(70), PonderLine.RIGHT))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT)), new Vector2i(80), PonderLine.RIGHT))
                            .waitFor(1)
                            .finishStep()
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT), new ItemStackTemplate(Items.STONE)), new Vector2i(10), PonderLine.RIGHT))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT), new ItemStackTemplate(Items.STONE)), new Vector2i(20), PonderLine.RIGHT))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT), new ItemStackTemplate(Items.STONE)), new Vector2i(30), PonderLine.RIGHT))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT), new ItemStackTemplate(Items.STONE)), new Vector2i(40), PonderLine.RIGHT))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT), new ItemStackTemplate(Items.STONE)), new Vector2i(50), PonderLine.RIGHT))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT), new ItemStackTemplate(Items.STONE)), new Vector2i(60), PonderLine.RIGHT))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT), new ItemStackTemplate(Items.STONE)), new Vector2i(70), PonderLine.RIGHT))
                            .waitFor(1)
                            .instruction(new ShowItemInstruction(1, List.of(new ItemStackTemplate(Items.DIRT), new ItemStackTemplate(Items.STONE)), new Vector2i(80), PonderLine.RIGHT))
                            .waitFor(1)
                            .finishStep()
                            .build()
            );

            var TEXT = r.registerBuilder(
                    PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j_test", "text"), new ItemStackTemplate(Items.LIME_DYE), Component.literal("Text"), Component.literal("UI Testing"))
                            .instruction(new ShowTextInstruction(5, Component.literal("hello left"), new Vector2i(400, 0), PonderLine.LEFT))
                            .instruction(new ShowTextInstruction(5, Component.literal("hello hi left\nmultiline"), new Vector2i(400, 30), PonderLine.LEFT))

                            .instruction(new ShowTextInstruction(5, Component.literal("hello none"), new Vector2i(400, 45), PonderLine.NONE))
                            .instruction(new ShowTextInstruction(5, Component.literal("hello hi none\nmultiline"), new Vector2i(400, 60), PonderLine.NONE))

                            .instruction(new ShowTextInstruction(5, Component.literal("hello right"), new Vector2i(400, 5), PonderLine.RIGHT))
                            .instruction(new ShowTextInstruction(5, Component.literal("hello hi right\nmultiline"), new Vector2i(400, 35), PonderLine.RIGHT))

                            .waitFor(5)
                            .instruction(new ShowTextInstruction(1, Component.literal("hello"), new Vector2i(400, 0), PonderLine.LEFT))
                            .instruction(new ShowTextInstruction(1, Component.literal("hello hi\nmultiline"), new Vector2i(150, 30), PonderLine.LEFT))
                            .waitFor(1)
                            .instruction(new ShowTextInstruction(1, Component.literal("hello"), new Vector2i(150, 0), PonderLine.LEFT))
                            .instruction(new ShowTextInstruction(1, Component.literal("hello hi\nmultiline"), new Vector2i(150, 30), PonderLine.LEFT))
                            .waitFor(1)
                            .instruction(new ShowTextInstruction(1, Component.literal("hello"), new Vector2i(150, 0), PonderLine.LEFT))
                            .instruction(new ShowTextInstruction(1, Component.literal("hello hi\nmultiline"), new Vector2i(150, 30), PonderLine.LEFT))
                            .waitFor(1)
                            .instruction(new ShowTextInstruction(1, Component.literal("hello"), new Vector2i(150, 0), PonderLine.LEFT))
                            .instruction(new ShowTextInstruction(1, Component.literal("hello hi\nmultiline"), new Vector2i(150, 30), PonderLine.LEFT))
                            .waitFor(1)
                            .instruction(new ShowTextInstruction(1, Component.literal("hello"), new Vector2i(150, 0), PonderLine.LEFT))
                            .instruction(new ShowTextInstruction(1, Component.literal("hello hi\nmultiline"), new Vector2i(150, 30), PonderLine.LEFT))
                            .waitFor(1)
                            .instruction(new ShowTextInstruction(1, Component.literal("hello"), new Vector2i(150, 0), PonderLine.LEFT))
                            .instruction(new ShowTextInstruction(1, Component.literal("hello hi\nmultiline"), new Vector2i(150, 30), PonderLine.LEFT))
                            .waitFor(1)
                            .instruction(new ShowTextInstruction(1, Component.literal("hello"), new Vector2i(150, 0), PonderLine.LEFT))
                            .instruction(new ShowTextInstruction(1, Component.literal("hello hi\nmultiline"), new Vector2i(150, 30), PonderLine.LEFT))
                            .waitFor(1)
                            .finishStep()
                            .build()
            );

            var RECURSIVE_LINE = r.registerBuilder(
                    PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j_test", "recursive_line"), new ItemStackTemplate(Items.LIME_DYE), Component.literal("Recursive line"), Component.literal("UI Testing"))
                            .instruction(new ExecuteCodeInstruction((scene -> {
                                scene.getElementHolder().addElement(new PonderLineDisplay(2, new Vector2i(0), Vec2.ONE, scene, 0, 0));
                            })))
                            .waitFor(100000)
                            .finishStep()
                            .build()
            );

            var CAMERA_TEST = r.registerBuilder(
                    PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j_test", "camera"), new ItemStackTemplate(Items.SPYGLASS), Component.literal("Camera"), Component.literal("CAMERAS WHEE"))
                            .instruction(new SetCameraInstruction(Vec3.ZERO, new Vec2(0, 90), 1))
                            .waitFor(1)
                            .instruction(new SetCameraInstruction(new Vec3(0, 10, 0), new Vec2(90, 180), 2))
                            .waitFor(1)
                            .instruction(new SetCameraInstruction(new Vec3(5, 7, 6), new Vec2(70, 30), 3))
                            .waitFor(1)
                            .instruction(new SetCameraInstruction(Vec3.ZERO, new Vec2(0, 90), 0.5f))
                            .waitFor(1)
                            .finishStep()
                            .build()
            );


            r.registerGroup(new PonderGroup(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "large_group"), new ItemStackTemplate(Items.BUNDLE), List.of(TEST_PONDER, TEST_ITEM_PONDER2, TEST_ITEM_PONDER, CAMERA_TEST, RECURSIVE_LINE, LINES, TEXT)));

            r.registerGroup(new PonderGroup(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "explosives"), new ItemStackTemplate(Items.GUNPOWDER), List.of(TEST_ITEM_PONDER2, TEST_ITEM_PONDER, END_CRYSTAL_PONDER2))).addRelatedGroup(PonderManager.getGroupForItem(Items.TNT));
            r.registerItemToBuilder(TESTITEM, TEST_PONDER);
        });
    }
}
