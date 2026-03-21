package com.daniel99j.lib99j.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RunCodeClickEventHolder {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final ArrayList<UUID> storage = new ArrayList<>();

    protected void add(RunCodeClickEvent event) {
        storage.add(event.getUuid());
        RunCodeClickEvent.eventMap.put(event.getUuid(), event);
    }

    protected void remove(RunCodeClickEvent event) {
        storage.remove(event.getUuid());
        RunCodeClickEvent.eventMap.remove(event.getUuid());
    }

    public void close() {
        List<UUID> ids = new ArrayList<>(storage);
        for (UUID uuid : ids) {
            RunCodeClickEvent.eventMap.get(uuid).disable();
        }
        storage.clear();
    }
}
