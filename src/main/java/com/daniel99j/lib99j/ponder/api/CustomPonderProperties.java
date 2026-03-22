package com.daniel99j.lib99j.ponder.api;

public final class CustomPonderProperties {
    public Object customData;
    public boolean canSkipAndRewind;
    public boolean canUseMenu;
    /**
     * If pressing Q quits the ponder. Taking damage or disconnecting still closes it
     */
    public boolean canLeave;
    public boolean showHud;
    public boolean canIdentify;
    public boolean canPause;
    public Runnable onClose;

    public CustomPonderProperties(Object customData, boolean canSkipAndRewind, boolean canUseMenu, boolean canLeave, boolean showHud, boolean canIdentify, boolean canPause, Runnable onClose) {
        this.customData = customData;
        this.canSkipAndRewind = canSkipAndRewind;
        this.canUseMenu = canUseMenu;
        this.canLeave = canLeave;
        this.showHud = showHud;
        this.canIdentify = canIdentify;
        this.canPause = canPause;
        this.onClose = onClose;
    }
}
