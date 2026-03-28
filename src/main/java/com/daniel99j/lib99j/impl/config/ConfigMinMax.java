package com.daniel99j.lib99j.impl.config;

public class ConfigMinMax<T> {
    public final T min;
    public final T max;

    public ConfigMinMax(T min, T max) {
        this.min = min;
        this.max = max;
    }
}
