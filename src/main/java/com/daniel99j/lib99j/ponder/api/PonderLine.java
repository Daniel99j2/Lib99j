package com.daniel99j.lib99j.ponder.api;

public record PonderLine(int textStartPos, LineSide lineSide) {
    public static final PonderLine LEFT = new PonderLine(100, LineSide.LEFT);
    public static final PonderLine RIGHT = new PonderLine(100, LineSide.RIGHT);
    public static final PonderLine NONE = new PonderLine(0, LineSide.RIGHT);

    public boolean shouldShow() {
        return textStartPos != 0;
    }

    public enum LineSide {
        RIGHT,
        LEFT
    }
}
