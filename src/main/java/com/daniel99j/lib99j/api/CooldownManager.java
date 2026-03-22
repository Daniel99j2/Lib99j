package com.daniel99j.lib99j.api;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.UseCooldown;

import java.util.Optional;

public class CooldownManager {
    public static void setCooldown(ServerPlayer player, Identifier id, int time) {
        player.getCooldowns().addCooldown(getStackFromId(id), time);
    }

    public static boolean isOnCooldown(ServerPlayer player, Identifier id) {
        return player.getCooldowns().isOnCooldown(getStackFromId(id));
    }

    private static ItemStack getStackFromId(Identifier id) {
        ItemStack stack = Items.STONE.getDefaultInstance();
        stack.set(DataComponents.USE_COOLDOWN, new UseCooldown(0.0001f, Optional.of(Identifier.fromNamespaceAndPath("lib99j_cooldown_manager", id.toString().replace(":", "_lib99jcolon_")))));
        return stack;
    }
}
