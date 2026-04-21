package com.daniel99j.lib99j.impl;

import java.util.function.Consumer;

public record DebugArg(String name, Consumer<Boolean> setter) {
}
