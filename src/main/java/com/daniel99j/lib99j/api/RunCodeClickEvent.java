package com.daniel99j.lib99j.api;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;


/**
 * A click event that runs arbitrary code when clicked
 * <p>If you want to use it you must store it somewhere, preferably in the context where it is being used (Example: A non-static list of RunCodeClickEvents should be put in your CustomGui class)</p>
 */
public class RunCodeClickEvent {
    @ApiStatus.Internal
    public static WeakHashMap<UUID, WeakReference<RunCodeClickEvent>> clickEvents = new WeakHashMap<>();

    private final Runnable code;
    private final Supplier<Boolean> allowedToRun;
    private final UUID uuid;
    private final boolean disableAfterUse;
    private boolean disabled = false;
    public final UUID allowedPlayerUUID;

    /**
     * This constructor does add the event to your list, so it will automatically be kept!
     */
    public RunCodeClickEvent(Runnable code, Supplier<Boolean> allowedToRun, boolean disableAfterUse, ServerPlayer player, ArrayList<RunCodeClickEvent> store) {
        this(code, allowedToRun, disableAfterUse, player);
        store.add(this);
    }

    /**
     * This constructor does NOT add the event to your list, so it will not automatically be kept!
     */
    public RunCodeClickEvent(Runnable code, Supplier<Boolean> allowedToRun, boolean disableAfterUse, ServerPlayer player) {
        this.code = code;
        this.allowedToRun = allowedToRun;
        this.uuid = UUID.randomUUID();
        this.disableAfterUse = disableAfterUse;
        this.allowedPlayerUUID = player.getUUID();
        clickEvents.put(this.uuid, new WeakReference<>(this));
    }

    public void run() {
        if(!disabled && allowedToRun.get()) {
            MiscUtils.runOnMainThread(() -> {
                if (allowedToRun.get()) {
                    this.code.run();
                }
            });
        }
        if (disableAfterUse) disable();
    }

    public ClickEvent.Custom clickEvent() {
        if(this.disabled) throw new IllegalStateException("A RunCodeClickEvent was re-used. You should created once for each usage or use .copy()");
        return new ClickEvent.Custom(Identifier.fromNamespaceAndPath("lib99j_run_code_click_event", this.uuid.toString()), Optional.empty());
    }

    public Optional<StaticAction> staticActionClickEvent() {
        return Optional.of(new StaticAction(clickEvent()));
    }

    public void disable() {
        this.disabled = true;
        clickEvents.remove(this.uuid);
    }

    @ApiStatus.Internal
    public static void removeGarbageCollectedEvents() {
        clickEvents.entrySet().removeIf(entry -> entry.getValue().get() == null);
    }
}
