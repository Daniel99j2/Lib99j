package com.daniel99j.lib99j.ponder.impl;

import com.mojang.serialization.Lifecycle;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class PonderLevelData implements ServerLevelData, WorldData {
    private long gameTime;
    private Difficulty difficulty = Difficulty.NORMAL;

    public PonderLevelData() {
        this.gameTime = 0;
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
    public void setGameTime(long l) {
        this.gameTime = l;
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
        return true;
    }

    @Override
    public void setInitialized(boolean bl) {

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
    public @Nullable UUID getSinglePlayerUUID() {
        return null;
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory crashReportCategory, LevelHeightAccessor levelHeightAccessor) {
        ServerLevelData.super.fillCrashReportCategory(crashReportCategory, levelHeightAccessor);
        WorldData.super.fillCrashReportCategory(crashReportCategory);
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
    public WorldDataConfiguration getDataConfiguration() {
        return WorldDataConfiguration.DEFAULT;
    }

    @Override
    public void setDataConfiguration(WorldDataConfiguration worldDataConfiguration) {
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
        return new LevelSettings(this.getLevelName(), this.getGameType(), LevelSettings.DifficultySettings.DEFAULT, this.isAllowCommands(), this.getDataConfiguration());
    }

    @Override
    public CompoundTag createTag(@Nullable UUID singlePlayerUUID) {
        throw new IllegalStateException("Cannot create tag");
    }

    @Override
    public void setSpawn(RespawnData respawnData) {

    }
}
