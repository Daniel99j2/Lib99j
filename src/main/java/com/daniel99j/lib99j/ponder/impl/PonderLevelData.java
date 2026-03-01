package com.daniel99j.lib99j.ponder.impl;

import com.mojang.serialization.Lifecycle;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.*;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PonderLevelData implements ServerLevelData, WorldData {
    private final WorldOptions worldOptions;
    private long gameTime;
    private long dayTime;
    private int clearWeatherTime;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;
    private boolean initialized;
    private final TimerQueue<MinecraftServer> timer;
    private GameRules gameRules;
    private Difficulty difficulty;

    public PonderLevelData() {
        this.gameTime = 0;
        this.dayTime = 0;
        this.clearWeatherTime = 0;
        this.rainTime = 0;
        this.raining = false;
        this.thunderTime = 0;
        this.thundering = false;
        this.initialized = true;
        this.worldOptions = new WorldOptions(0, false, false);
        this.timer = new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS);
        this.gameRules = new GameRules(FeatureFlagSet.of());
    }

    @Override
    public LevelData.RespawnData getRespawnData() {
        return new RespawnData(GlobalPos.of(Level.OVERWORLD, new BlockPos(0, 0, 0)), 0, 0);
    }

    @Override
    public long getGameTime() {
        return this.gameTime;
    }

    @Override
    public long getDayTime() {
        return this.dayTime;
    }

    @Nullable
    @Override
    public CompoundTag getLoadedPlayerTag() {
        return null;
    }

    @Override
    public void setGameTime(long l) {
        this.gameTime = l;
    }

    @Override
    public void setDayTime(long l) {
        this.dayTime = l;
    }

    @Override
    public String getLevelName() {
        return "Ponder (Temporary world)";
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public int getClearWeatherTime() {
        return this.clearWeatherTime;
    }

    @Override
    public void setClearWeatherTime(int i) {
        this.clearWeatherTime = i;
    }

    @Override
    public boolean isThundering() {
        return this.thundering;
    }

    @Override
    public void setThundering(boolean bl) {
        this.thundering = bl;
    }

    @Override
    public int getThunderTime() {
        return this.thunderTime;
    }

    @Override
    public void setThunderTime(int i) {
        this.thunderTime = i;
    }

    @Override
    public boolean isRaining() {
        return this.raining;
    }

    @Override
    public void setRaining(boolean bl) {
        this.raining = bl;
    }

    @Override
    public int getRainTime() {
        return this.rainTime;
    }

    @Override
    public void setRainTime(int i) {
        this.rainTime = i;
    }

    @Override
    public GameType getGameType() {
        return GameType.SPECTATOR;
    }

    @Override
    public void setGameType(GameType gameType) {
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public boolean isAllowCommands() {
        return true;
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public void setInitialized(boolean bl) {
        this.initialized = bl;
    }

    @Override
    public GameRules getGameRules() {
        return this.gameRules;
    }

    @Override
    public Optional<WorldBorder.Settings> getLegacyWorldBorderSettings() {
        return Optional.empty();
    }

    @Override
    public void setLegacyWorldBorderSettings(Optional<WorldBorder.Settings> optional) {
    }

    @Override
    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public boolean isDifficultyLocked() {
        return false;
    }

    @Override
    public void setDifficultyLocked(boolean bl) {
    }

    @Override
    public TimerQueue<MinecraftServer> getScheduledEvents() {
        return this.timer;
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory crashReportCategory, LevelHeightAccessor levelHeightAccessor) {
        ServerLevelData.super.fillCrashReportCategory(crashReportCategory, levelHeightAccessor);
        WorldData.super.fillCrashReportCategory(crashReportCategory);
    }

    @Override
    public WorldOptions worldGenOptions() {
        return this.worldOptions;
    }

    @Override
    public boolean isFlatWorld() {
        return true;
    }

    @Override
    public boolean isDebugWorld() {
        return false;
    }

    @Override
    public Lifecycle worldGenSettingsLifecycle() {
        return Lifecycle.experimental();
    }

    @Override
    public EndDragonFight.Data endDragonFightData() {
        return new EndDragonFight.Data(false, false, false, false, Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Override
    public void setEndDragonFightData(EndDragonFight.Data data) {
    }

    @Override
    public WorldDataConfiguration getDataConfiguration() {
        return WorldDataConfiguration.DEFAULT;
    }

    @Override
    public void setDataConfiguration(WorldDataConfiguration worldDataConfiguration) {
    }

    @Nullable
    @Override
    public CompoundTag getCustomBossEvents() {
        return null;
    }

    @Override
    public void setCustomBossEvents(@Nullable CompoundTag compoundTag) {
    }

    @Override
    public int getWanderingTraderSpawnDelay() {
        return 9999999;
    }

    @Override
    public void setWanderingTraderSpawnDelay(int i) {

    }

    @Override
    public int getWanderingTraderSpawnChance() {
        return 0;
    }

    @Override
    public void setWanderingTraderSpawnChance(int i) {
    }

    @Nullable
    @Override
    public UUID getWanderingTraderId() {
        return null;
    }

    @Override
    public void setWanderingTraderId(UUID uUID) {
    }

    @Override
    public void setModdedInfo(String string, boolean bl) {

    }

    @Override
    public boolean wasModded() {
        return true;
    }

    @Override
    public Set<String> getKnownServerBrands() {
        return Set.of("ponder_scene");
    }

    @Override
    public Set<String> getRemovedFeatureFlags() {
        return Set.of();
    }

    @Override
    public ServerLevelData overworldData() {
        throw new IllegalStateException();
    }

    @Override
    public LevelSettings getLevelSettings() {
        return new LevelSettings(this.getLevelName(), this.getGameType(), this.isHardcore(), this.getDifficulty(), this.isAllowCommands(), this.getGameRules(), this.getDataConfiguration());
    }

    @Override
    public CompoundTag createTag(RegistryAccess registryAccess, @Nullable CompoundTag compoundTag) {
        throw new IllegalCallerException("Cannot create tag");
    }

    @Override
    public void setSpawn(RespawnData respawnData) {

    }
}
