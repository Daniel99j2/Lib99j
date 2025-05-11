package com.daniel99j.lib99j.api;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.packettweaker.PacketContext;

public class FixedPolymerBlockItem extends PolymerBlockItem {
    public FixedPolymerBlockItem(Block block, Settings settings) {
        this(block, settings, ItemUtils.getBasicModelItem(), true);
    }

    public FixedPolymerBlockItem(Block block, Settings settings, Item polymerItem) {
        super(block, settings, polymerItem);
    }

    public FixedPolymerBlockItem(Block block, Settings settings, Item polymerItem, boolean useModel) {
        super(block, settings, polymerItem, useModel);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return ItemUtils.getBasicModelItem();
    }
    
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var x = super.useOnBlock(context);
        if (x == ActionResult.SUCCESS) {
            if (context.getPlayer() instanceof ServerPlayerEntity player) {
                var blockSoundGroup = this.getBlock().getDefaultState().getSoundGroup();
                SoundUtils.playSoundAtPosition(((ServerWorld) player.getWorld()), Vec3d.of(context.getBlockPos()), this.getPlaceSound(this.getBlock().getDefaultState()), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
            }
            return ActionResult.SUCCESS_SERVER;
        }
        return x;
    }
}