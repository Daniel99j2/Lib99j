package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.Lib99j;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * A click event that runs arbitrary code when clicked
 * <p>Use a RunCodeClickEventHolder in your context to store all active events. When finished, run holder.close()</p>
 */
public class RunCodeClickEvent {
    @ApiStatus.Internal
    public static Map<UUID, RunCodeClickEvent> eventMap = new HashMap<>();

    private final Consumer<Optional<Tag>> code;
    private final Supplier<Boolean> allowedToRun;
    private final UUID uuid;
    private final boolean disableAfterUse;
    //volatile so if multiple threads try and check it then it will never run more than wanted
    private volatile boolean disabled = false;
    public final UUID allowedPlayerUUID;
    private final RunCodeClickEventHolder holder;

    public RunCodeClickEvent(Consumer<Optional<Tag>> code, Supplier<Boolean> allowedToRun, boolean disableAfterUse, ServerPlayer player, RunCodeClickEventHolder holder) {
        this.code = code;
        this.allowedToRun = allowedToRun;
        this.uuid = UUID.randomUUID();
        this.disableAfterUse = disableAfterUse;
        this.allowedPlayerUUID = player.getUUID();
        this.holder = holder;
        holder.add(this);
    }

    public void run(Optional<Tag> tag) {
        if(!disabled && allowedToRun.get()) {
            runOnMainThread(() -> {
                if (allowedToRun.get()) {
                    this.code.accept(tag);
                }
            });
        }
        if (disableAfterUse) disable();
    }

    public ClickEvent.Custom clickEvent() {
        if(this.disabled) throw new IllegalStateException("This event is disabled");
        return new ClickEvent.Custom(Identifier.fromNamespaceAndPath("lib99j_run_code_click_event", this.uuid.toString()), Optional.empty());
    }

    public CustomAll dialogActionClickEvent() {
        return new CustomAll(Identifier.fromNamespaceAndPath("lib99j_run_code_click_event", this.uuid.toString()), Optional.empty());
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

    private static void runOnMainThread(Runnable code) {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Minecraft.getInstance().execute(code);
        } else {
            Lib99j.getServerOrThrow().execute(code);
        }
    }
}
