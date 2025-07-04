package com.daniel99j.lib99j.api;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import xyz.nucleoid.packettweaker.PacketContext;

@SuppressWarnings({"unused"})
public class PolymerSpawnEgg extends SpawnEggItem implements PolymerItem {
    public PolymerSpawnEgg(EntityType<? extends MobEntity> type, Settings settings) {
        super(type, settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return ItemUtils.getBasicModelItem();
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return super.useOnEntity(stack, user, entity, hand);
    }
}
