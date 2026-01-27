package com.daniel99j.lib99j.impl.mixin;

import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerList.class)
public interface PlayerListAccessor {
    @Mutable
    @Accessor("players")
    List<ServerPlayer> getPlayers();

    @Mutable
    @Accessor("playersByUUID")
    Map<UUID, ServerPlayer> getPlayersByUUID();

    @Mutable
    @Accessor("stats")
    Map<UUID, ServerStatsCounter> getStats();

    @Mutable
    @Accessor("advancements")
    Map<UUID, PlayerAdvancements> getAdvancements();
}