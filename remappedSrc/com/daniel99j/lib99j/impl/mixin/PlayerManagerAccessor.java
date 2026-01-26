package com.daniel99j.lib99j.impl.mixin;

import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerManager.class)
public interface PlayerManagerAccessor {
    @Mutable
    @Accessor("players")
    List<ServerPlayerEntity> getPlayers();

    @Mutable
    @Accessor("playerMap")
    Map<UUID, ServerPlayerEntity> getPlayerMap();

    @Mutable
    @Accessor("statisticsMap")
    Map<UUID, ServerStatHandler> getStatisticsMap();

    @Mutable
    @Accessor("advancementTrackers")
    Map<UUID, PlayerAdvancementTracker> getAdvancementTrackers();
}