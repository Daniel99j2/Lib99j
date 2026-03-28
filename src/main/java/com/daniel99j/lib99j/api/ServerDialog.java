package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.Lib99j;
import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.core.Holder;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ServerDialog implements GuiInterface {
    private ServerDialogScreenHandler screenHandler;
    private final ServerPlayer player;
    private Component title = Component.empty();
    private RunCodeClickEventHolder runCodeClickEventHolder;
    private final ServerDialogType type;

    public ServerDialog(ServerPlayer player, ServerDialogType type) {
        this.player = player;
        this.type = type;
    }

    @Override
    public void setTitle(Component title) {
        this.title = title;
    }

    @Override
    public @Nullable Component getTitle() {
        return this.title;
    }

    @Override
    public MenuType<?> getType() {
        throw new IllegalArgumentException();
    }

    @Override
    public ServerPlayer getPlayer() {
        return player;
    }

    @Override
    public int getSyncId() {
        return 0;
    }

    @Override
    public boolean isOpen() {
        return this.screenHandler != null && this.screenHandler == this.player.containerMenu;
    }

    @Override
    public boolean open() {
        this.runCodeClickEventHolder = new RunCodeClickEventHolder();
        this.screenHandler = new ServerDialogScreenHandler(this);
        reSend();
        return true;
    }

    @Override
    public boolean getAutoUpdate() {
        return false;
    }

    @Override
    public void setAutoUpdate(boolean value) {

    }

    @Override
    public void close(boolean alreadyClosed) {
        this.runCodeClickEventHolder.close();
        this.runCodeClickEventHolder = null;
        if(this.player.containerMenu == this.screenHandler) this.player.containerMenu = this.player.inventoryMenu;
        this.screenHandler = null;
        this.player.connection.send(new ClientboundContainerClosePacket(0));
    }

    public void reSend() {
        reSend(true);
    }

    public void reSend(boolean removeOldClickEventsOnlyUseIfYouKnowWhatYouAreDoing) {
        if(removeOldClickEventsOnlyUseIfYouKnowWhatYouAreDoing) runCodeClickEventHolder.close();
        RunCodeClickEvent onClose = new RunCodeClickEvent((tag) -> {
            try {
                if (onNoOrExit()) close();
            } catch (Exception e) {
                Lib99j.LOGGER.error("Error running action", e);
                player.disconnect();
            }
        }, () -> this.type == ServerDialogType.CONFIRM || this.type == ServerDialogType.MULTI_ACTION, true, player, runCodeClickEventHolder);
        RunCodeClickEvent onDone = new RunCodeClickEvent((tag) -> {
            try {
                if (onYesOrDone(tag)) close();
            } catch (Exception e) {
                Lib99j.LOGGER.error("Error running action", e);
                player.disconnect();
            }
        }, () -> this.type == ServerDialogType.CONFIRM || this.type == ServerDialogType.NOTICE, true, player, runCodeClickEventHolder);
        assert this.getTitle() != null;
        Dialog dialog = null;
        CommonDialogData data = new CommonDialogData(this.getTitle(), Optional.empty(), canCloseWithEscape(), false, DialogAction.WAIT_FOR_RESPONSE, this.getBody(), this.getInputs());

        ActionButton yes = new ActionButton(new CommonButtonData(getDoneActionName(), 150), Optional.of(onDone.dialogActionClickEvent()));
        ActionButton no = new ActionButton(new CommonButtonData(getCancelActionName(), 150), Optional.of(onClose.dialogActionClickEvent()));

        if(this.type == ServerDialogType.CONFIRM) dialog = new ConfirmationDialog(data, yes, no);
        if(this.type == ServerDialogType.NOTICE) dialog = new NoticeDialog(data, yes);
        if(this.type == ServerDialogType.MULTI_ACTION) dialog = new MultiActionDialog(data, this.getActions(), Optional.of(no), getColumns());
        assert dialog != null;
        player.openDialog(Holder.direct(dialog));
    }

    public boolean canCloseWithEscape() {
        return true;
    }

    public List<DialogBody> getBody() {
        return List.of();
    }

    public List<Input> getInputs() {
        return List.of();
    }

    public List<ActionButton> getActions() {
        return List.of();
    }

    public Component getDoneActionName() {
        return Component.translatable("gui.done");
    }
    public Component getCancelActionName() {
        return Component.translatable("gui.cancel");
    }

    public boolean onYesOrDone(Optional<Tag> tag) {
        return true;
    }

    public boolean onNoOrExit() {
        return true;
    }

    public int getColumns() {
        return 1;
    }

    public RunCodeClickEventHolder getRunCodeClickEventHolder() {
        return runCodeClickEventHolder;
    }
}
