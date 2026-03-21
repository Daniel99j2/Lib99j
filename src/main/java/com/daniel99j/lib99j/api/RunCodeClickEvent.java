package com.daniel99j.lib99j.api;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;


/**
 * A click event that runs arbitrary code when clicked
 * <p>If you want to use it you MUST store it somewhere, preferably in the context where it is being used (Example: A non-static list of RunCodeClickEvents should be put in your CustomGui class)</p>
 * <p>If you do not store it, then it will be periodically removed</p>
 */
public class RunCodeClickEvent {
    @ApiStatus.Internal
    public static Map<UUID, RunCodeClickEvent> eventMap = new HashMap<>();

    private final Runnable code;
    private final Supplier<Boolean> allowedToRun;
    private final UUID uuid;
    private final boolean disableAfterUse;
    private boolean disabled = false;
    public final UUID allowedPlayerUUID;
    private final RunCodeClickEventHolder holder;

    /**
     * This constructor does NOT add the event to your list, so it will not automatically be kept!
     */
    public RunCodeClickEvent(Runnable code, Supplier<Boolean> allowedToRun, boolean disableAfterUse, ServerPlayer player, RunCodeClickEventHolder holder) {
        this.code = code;
        this.allowedToRun = allowedToRun;
        this.uuid = UUID.randomUUID();
        this.disableAfterUse = disableAfterUse;
        this.allowedPlayerUUID = player.getUUID();
        this.holder = holder;
        holder.add(this);
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
        if(this.disabled) throw new IllegalStateException("This event is disabled");
        return new ClickEvent.Custom(Identifier.fromNamespaceAndPath("lib99j_run_code_click_event", this.uuid.toString()), Optional.empty());
    }

    public Optional<StaticAction> staticActionClickEvent() {
        return Optional.of(new StaticAction(clickEvent()));
    }

    public void disable() {
        this.disabled = true;
        this.holder.remove(this);
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isDisabled() {
        return disabled;
    }
}
