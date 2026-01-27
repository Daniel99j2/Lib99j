package com.daniel99j.lib99j.api;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.packettweaker.PacketContext;

@SuppressWarnings({"unused"})
public class FixedPolymerBlockItem extends PolymerBlockItem {
    public FixedPolymerBlockItem(Block block, Properties settings) {
        this(block, settings, ItemUtils.getBasicModelItem(), true);
    }

    public FixedPolymerBlockItem(Block block, Properties settings, Item polymerItem) {
        super(block, settings, polymerItem);
    }

    public FixedPolymerBlockItem(Block block, Properties settings, Item polymerItem, boolean useModel) {
        super(block, settings, polymerItem, useModel);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return ItemUtils.getBasicModelItem();
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        var x = super.useOn(context);
        if (x == InteractionResult.SUCCESS) {
            if (context.getPlayer() instanceof ServerPlayer player) {
                var blockSoundGroup = this.getBlock().defaultBlockState().getSoundType();
                SoundUtils.playSoundAtPosition(((ServerLevel) player.level()), Vec3.atLowerCornerOf(context.getClickedPos()), this.getPlaceSound(this.getBlock().defaultBlockState()), SoundSource.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return x;
    }
}