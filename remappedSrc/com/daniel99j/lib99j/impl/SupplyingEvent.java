package com.daniel99j.lib99j.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class SupplyingEvent<T> {
    @FunctionalInterface
    public interface Listener<T> {
        void run(T arg);
    }

    private final List<Listener<T>> handlers = new ArrayList<>();

    public SupplyingEvent() {
    }

    public void register(Listener<T> listener) {
        this.handlers.add(listener);
    }

    public Listener<T> registerRet(Listener<T> listener) {
        this.handlers.add(listener);
        return listener;
    }

    public void unregister(Listener<T> listener) {
        this.handlers.remove(listener);
    }

    public void invoke(T argument) {
        for (Listener<T> handler : this.handlers) {
            handler.run(argument);
        }
    }

    public boolean isEmpty() {
        return this.handlers.isEmpty();
    }

    public Collection<Listener<T>> invokers() {
        return Collections.unmodifiableCollection(this.handlers);
    }
}
