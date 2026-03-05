package com.daniel99j.lib99j.ponder.impl;

import net.minecraft.world.level.GameType;

public enum PonderSceneMode {
    PAUSED(true, true, true, true, GameType.SPECTATOR, true),
    PLAYING(false, true, true, true, GameType.SPECTATOR, true),
    DEV_EDITING(true, false, false, false, GameType.CREATIVE, false),
    IDENTIFYING(true, true, false, false, GameType.SPECTATOR, false),
    IN_MENU(true, true, true, true, GameType.SPECTATOR, true);

    private final boolean pausesScene;
    private final boolean locksHotbar;
    private final boolean locksCamera;
    private final GameType gameType;
    private final boolean movementControls;
    private final boolean inGameGui;

    PonderSceneMode(boolean pausesScene, boolean locksHotbar, boolean locksCamera, boolean movementControls, GameType gameType, boolean inGameGui) {
        this.pausesScene = pausesScene;
        this.locksHotbar = locksHotbar;
        this.locksCamera = locksCamera;
        this.gameType = gameType;
        this.movementControls = movementControls;
        this.inGameGui = inGameGui;
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

    public boolean hasInGameGui() {
        return inGameGui;
    }
}
