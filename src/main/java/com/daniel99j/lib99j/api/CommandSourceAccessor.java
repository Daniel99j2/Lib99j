package com.daniel99j.lib99j.api;

import org.jetbrains.annotations.ApiStatus;

public interface CommandSourceAccessor {
    boolean lib99j$isFromPacket();

    @ApiStatus.Internal
    void lib99j$setFromPacket(boolean fromPacket);
}
