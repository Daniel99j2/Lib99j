package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.RunCodeClickEvent;
import com.daniel99j.lib99j.api.ServerDialog;
import com.daniel99j.lib99j.api.ServerDialogType;
import com.daniel99j.lib99j.api.config.ConfigContext;
import com.daniel99j.lib99j.api.config.ConfigHolder;
import com.daniel99j.lib99j.api.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModConfigDialog extends ServerDialog {
    private final List<Input> inputs;
    private final List<DialogBody> body;
    private final ModConfig config;

    private ModConfigDialog(ServerPlayer player, List<Input> inputs, List<DialogBody> body, ModConfig config) {
        super(player, ServerDialogType.CONFIRM);
        this.inputs = inputs;
        this.body = body;
        this.config = config;
    }

    @Override
    public List<Input> getInputs() {
        return inputs;
    }

    @Override
    public List<DialogBody> getBody() {
        return body;
    }

    @Override
    public Component getDoneActionName() {
        return Component.translatable("selectWorld.edit.save");
    }

    @Override
    public boolean onYesOrDone(Optional<Tag> tag) {
        if(!this.getPlayer().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            this.getPlayer().sendSystemMessage(Component.translatable("commands.lib99j.no_permission").withStyle(ChatFormatting.RED));
            return true;
        }
        if(tag.isPresent() && tag.get() instanceof CompoundTag compoundTag) {
            for (Map.Entry<String, Tag> entry : compoundTag.entrySet()) {
                switch (entry.getValue()) {
                    case IntTag value -> setValue(config, entry.getKey(), value.value());
                    case FloatTag value -> setValue(config, entry.getKey(), value.value());
                    case DoubleTag value -> setValue(config, entry.getKey(), value.value());
                    case LongTag value -> setValue(config, entry.getKey(), value.value());
                    case ByteTag value -> setValue(config, entry.getKey(), value.value() != 0);
                    case ShortTag value -> setValue(config, entry.getKey(), value.value());
                    case StringTag value -> setValue(config, entry.getKey(), value.value());
                    case null, default -> throw new IllegalStateException("Invalid tag type");
                }
            }
        }
        for (ServerPlayer player1 : Lib99j.getServerOrThrow().getPlayerList().players) {
            if(player1.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) && player1 != this.getPlayer()) {
                player1.sendSystemMessage(Component.translatable("chat.type.admin", this.getPlayer().getDisplayName(), Component.translatable("commands.lib99j.config.saved", this.getPlayer().getDisplayName())).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
        this.getPlayer().sendSystemMessage(Component.translatable("commands.lib99j.config.saved", this.getPlayer().getDisplayName()));
        return true;
    }

    public static void openDialog(ModConfig config, ServerPlayer player, ConfigContext context) {
        if(config.getAvailableConfigs().isEmpty()) return;

        if(!config.has(context)) context = config.getAvailableConfigs().iterator().next().getContext();

        List<DialogBody> body = new ArrayList<>();

        MutableComponent contextPicker = Component.empty();

        List<Input> inputs = config.getScreen().buildDialogInputs(context);

        ModConfigDialog dialog = new ModConfigDialog(player, inputs, new ArrayList<>(), config);

        dialog.open();

        boolean first = true;
        for (ConfigHolder<?> availableConfig : config.getAvailableConfigs()) {
            RunCodeClickEvent selectCategory = new RunCodeClickEvent((tag) -> {
                openDialog(config, player, availableConfig.getContext());
            }, () -> true, true, player, dialog.getRunCodeClickEventHolder());

            if(!first) contextPicker.append(" ");
            if(availableConfig.getContext().equals(context)) {
                contextPicker.append(Component.translatable("lib99j.square_brackets", Component.translatable("config.lib99j.context."+availableConfig.getContext().getType()).setStyle(Style.EMPTY.withBold(true))));
            } else {
                contextPicker.append(Component.translatable("lib99j.square_brackets", Component.translatable("config.lib99j.context."+availableConfig.getContext().getType()).setStyle(Style.EMPTY.withClickEvent(selectCategory.clickEvent()))));
            }
            first = false;
        }

        body.add(new PlainMessage(contextPicker, 200));

        dialog.body.addAll(body);

        dialog.reSend(false);
    }

    private static void setValue(ModConfig config, String name, Object value) {
        for (ConfigHolder<?> availableConfig : config.getAvailableConfigs()) {
            for (ConfigHolder.ConfigField field : availableConfig.getFields()) {
                if(field.getSerializedName().equals(name)) {
                    field.setValueAutoCorrect(availableConfig.get(), value);
                    return;
                }
            }
        }
    }
}
