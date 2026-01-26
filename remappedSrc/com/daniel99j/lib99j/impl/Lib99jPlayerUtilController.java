package com.daniel99j.lib99j.impl;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

@ApiStatus.Internal
public interface Lib99jPlayerUtilController {
    void lib99j$lockCamera();

    void lib99j$unlockCamera();

    void lib99j$runModCheckerOutput();

    void lib99j$addModTranslationCheckerTranslation(String entry);

    void lib99j$setNeededModCheckerTranslations(ArrayList<Map.Entry<String, String>> translations);

    void lib99j$setModCheckerOutput(Consumer<ArrayList<String>> output);
}
