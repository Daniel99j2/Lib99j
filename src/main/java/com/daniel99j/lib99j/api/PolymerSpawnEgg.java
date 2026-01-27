package com.daniel99j.lib99j.api;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import xyz.nucleoid.packettweaker.PacketContext;

@SuppressWarnings({"unused"})
public class PolymerSpawnEgg extends SpawnEggItem implements PolymerItem {
    public PolymerSpawnEgg(EntityType<? extends Mob> type, Properties settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return ItemUtils.getBasicModelItem();
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        return super.interactLivingEntity(stack, user, entity, hand);
    }
}
