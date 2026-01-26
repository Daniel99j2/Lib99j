package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.api.gui.GuiUtils;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

@ApiStatus.Internal
public interface Lib99jPlayerUtilController {
    void lib99j$lockCamera();

    void lib99j$unlockCamera();

    void lib99j$setCameraPos(Vec3d pos);

    void lib99j$setCameraPitch(float pitch);

    void lib99j$setCameraYaw(float yaw);

    void lib99j$finishCurrentModChecker();

    void lib99j$addTranslationChecker(Map<String, String> modTranslations, Consumer<GuiUtils.PlayerTranslationsResponse> output);

    GuiUtils.PlayerTranslationCheckerData lib99j$getActiveTranslationChecker();

    boolean lib99j$isModCheckerRunning();

    Vec3d lib99j$getCameraWorldPos();
}
