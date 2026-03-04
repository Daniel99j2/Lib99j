package com.daniel99j.lib99j.ponder.impl;

import net.minecraft.world.level.GameType;

public enum PonderSceneMode {
    PAUSED(true, true, true, true, GameType.SPECTATOR),
    PLAYING(false, true, true, true, GameType.SPECTATOR),
    DEV_EDITING(true, false, false, false, GameType.CREATIVE),
    IDENTIFYING(true, true, false, false, GameType.SPECTATOR),
    IN_MENU(true, true, true, true, GameType.SPECTATOR);

    private final boolean pausesScene;
    private final boolean locksHotbar;
    private final boolean locksCamera;
    private final GameType gameType;
    private final boolean movementControls;

    PonderSceneMode(boolean pausesScene, boolean locksHotbar, boolean locksCamera, boolean movementControls, GameType gameType) {
        this.pausesScene = pausesScene;
        this.locksHotbar = locksHotbar;
        this.locksCamera = locksCamera;
        this.gameType = gameType;
        this.movementControls = movementControls;
    }

    public boolean isPaused() {
        return pausesScene;
    }

    public boolean locksCamera() {
        return locksCamera;
    }

    public GameType getGameType() {
        return gameType;
    }

    public boolean hasMovementControls() {
        return movementControls;
    }

    public boolean locksHotbar() {
        return locksHotbar;
    }
}
