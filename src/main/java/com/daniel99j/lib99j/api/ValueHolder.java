package com.daniel99j.lib99j.api;

public class ValueHolder<T> {
    public T object = null;

    public ValueHolder() {
    }

    public ValueHolder(T o) {
        this.object = o;
    }
}
