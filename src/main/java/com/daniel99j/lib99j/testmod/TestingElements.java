package com.daniel99j.lib99j.testmod;

import com.daniel99j.lib99j.api.RegUtil;
import com.daniel99j.lib99j.ponder.api.PonderBuilder;
import com.daniel99j.lib99j.ponder.api.PonderManager;
import com.daniel99j.lib99j.ponder.api.instruction.ExecuteCodeInstruction;
import com.daniel99j.lib99j.ponder.api.instruction.ShowItemInstruction;
import com.daniel99j.lib99j.ponder.api.instruction.ShowLineInstruction;
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

public class TestingElements {
    public static final Block TEST = RegUtil.registerBlock(
            "test_block",
            TestBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).pushReaction(PushReaction.DESTROY).instabreak().noCollision().mapColor(MapColor.SNOW).sound(SoundType.WOOL)
    );

    public static final PonderBuilder TEST_PONDER = PonderManager.registerIdToBuilder(Identifier.fromNamespaceAndPath("lib99j_test", "testponder0"),
            PonderBuilder.create().title("Dev ponder menu").size(20, 20, 20).defaultBiome(Biomes.BASALT_DELTAS)
            .waitFor(1)
            .instruction(new ExecuteCodeInstruction((scene) -> {
                scene.getWorld().setBlockAndUpdate(scene.getOrigin().offset(4, 0, 4), Blocks.PISTON.defaultBlockState());
            }))
            .waitFor(1)
            .instruction(new ExecuteCodeInstruction((scene) -> {
                scene.getWorld().setBlockAndUpdate(scene.getOrigin().offset(4, 1, 4), Blocks.REDSTONE_BLOCK.defaultBlockState());
                Creeper creeper = new Creeper(EntityType.CREEPER, scene.getWorld());
                creeper.setPosRaw(scene.getOrigin().getX() + 5, scene.getOrigin().getY() + 10, scene.getOrigin().getZ() + 5);
                creeper.setPersistenceRequired();
                creeper.ignite();
                scene.getWorld().addFreshEntity(creeper);
                //scene.fastForwardUntil(2);
            }))
            .waitFor(1)
            .instruction(new ShowItemInstruction(1, Items.PISTON.getDefaultInstance()))
            .instruction(new ShowLineInstruction(1, 0xFF0000, Vec2.ZERO, new Vec2(10, 10), 10))
            .waitFor(2)
            .finishStep("creeper_boom")
            .waitFor(1)
            .instruction(new ShowItemInstruction(1, Items.STICKY_PISTON.getDefaultInstance()))
            .instruction(new ShowLineInstruction(1, 0x00FF00, Vec2.ZERO, new Vec2(20, 20), 5))
            .instruction(new ExecuteCodeInstruction((scene) -> {
                Cow creeper = new Cow(EntityType.COW, scene.getWorld());
                creeper.setPosRaw(scene.getOrigin().getX() + 5, scene.getOrigin().getY() + 10, scene.getOrigin().getZ() + 5);
                creeper.setPersistenceRequired();
                scene.getWorld().addFreshEntity(creeper);
            }))
            .waitFor(2)
            .finishStep("cow_moo")
            .waitFor(1)
            .instruction(new ShowItemInstruction(1, Items.TNT.getDefaultInstance()))
            .instruction(new ShowLineInstruction(1, 0x00FF00, Vec2.ZERO, new Vec2(20, 20), 5))
            .instruction(new ExecuteCodeInstruction((scene) -> {
                Chicken creeper = new Chicken(EntityType.CHICKEN, scene.getWorld());
                creeper.setPosRaw(scene.getOrigin().getX() + 5, scene.getOrigin().getY() + 10, scene.getOrigin().getZ() + 5);
                creeper.setPersistenceRequired();
                scene.getWorld().addFreshEntity(creeper);

                scene.getWorld().setBlockAndUpdate(new BlockPos(scene.getOrigin().getX() + 5, scene.getOrigin().getY() + 6, scene.getOrigin().getZ() + 5), Blocks.LAVA.defaultBlockState());
            }))
            .waitFor(10)
            .finishStep("chicken_yum")
            .waitFor(30)
            .finishStep("demo_complete")
            .build()
    );

    public static final PonderBuilder TEST_PONDER1 = PonderManager.registerIdToBuilder(Identifier.fromNamespaceAndPath("lib99j_test", "testponder1"),
            PonderBuilder.create().title("Dev ponder menu 1").size(20, 20, 20).defaultBiome(Biomes.BASALT_DELTAS)
                    .waitFor(1)
                    .instruction(new ExecuteCodeInstruction((scene) -> {
                        scene.getWorld().setBlockAndUpdate(new BlockPos(4, 0, 4), Blocks.PISTON.defaultBlockState());
                    }))
                    .waitFor(1)
                    .instruction(new ExecuteCodeInstruction((scene) -> {
                        scene.getWorld().setBlockAndUpdate(new BlockPos(4, 1, 4), Blocks.REDSTONE_BLOCK.defaultBlockState());
                        Creeper creeper = new Creeper(EntityType.CREEPER, scene.getWorld());
                        creeper.setPosRaw(5, 10, 5);
                        creeper.setPersistenceRequired();
                        creeper.ignite();
                        scene.getWorld().addFreshEntity(creeper);
                        //scene.fastForwardUntil(2);
                    }))
                    .waitFor(1)
                    .instruction(new ShowItemInstruction(1, Items.PISTON.getDefaultInstance()))
                    .instruction(new ShowLineInstruction(1, 0xFF0000, Vec2.ZERO, new Vec2(10, 10), 10))
                    .waitFor(2)
                    .finishStep("creeper_boom")
                    .waitFor(1)
                    .instruction(new ShowItemInstruction(1, Items.STICKY_PISTON.getDefaultInstance()))
                    .instruction(new ShowLineInstruction(1, 0x00FF00, Vec2.ZERO, new Vec2(20, 20), 5))
                    .instruction(new ExecuteCodeInstruction((scene) -> {
                        Cow creeper = new Cow(EntityType.COW, scene.getWorld());
                        creeper.setPosRaw(5, 10, 5);
                        creeper.setPersistenceRequired();
                        scene.getWorld().addFreshEntity(creeper);
                    }))
                    .waitFor(2)
                    .finishStep("cow_moo")
                    .waitFor(1)
                    .instruction(new ShowItemInstruction(1, Items.TNT.getDefaultInstance()))
                    .instruction(new ShowLineInstruction(1, 0x00FF00, Vec2.ZERO, new Vec2(20, 20), 5))
                    .instruction(new ExecuteCodeInstruction((scene) -> {
                        Chicken creeper = new Chicken(EntityType.CHICKEN, scene.getWorld());
                        creeper.setPosRaw(5, 10, 5);
                        creeper.setPersistenceRequired();
                        scene.getWorld().addFreshEntity(creeper);

                        scene.getWorld().setBlockAndUpdate(new BlockPos(5, 6, 5), Blocks.LAVA.defaultBlockState());
                    }))
                    .waitFor(10)
                    .finishStep("chicken_yum")
                    .waitFor(30)
                    .finishStep("demo_complete")
                    .build()
    );

    public static final Item TEST_BLOCKITEM = RegUtil.registerBlockItem(
            TEST
    );

    public static void init() {}
}
