package com.daniel99j.lib99j.api.config;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.ModInstallManager;
import com.daniel99j.lib99j.impl.config.AutomaticDialogInputs;
import com.daniel99j.lib99j.impl.network.ClientboundLib99jSyncConfigOptionPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ModConfig {
    private final AutomaticDialogInputs dialogInputs = new AutomaticDialogInputs(this);
    private final EnumMap<ConfigContext, ConfigHolder<?>> configs = new EnumMap<>(ConfigContext.class);
    private String modId;
    private boolean dirty = false;

    public <A, B, C, D> ModConfig(@Nullable Class<A> clientConfig, @Nullable Class<B> serverConfig, @Nullable Class<C> saveConfig, @Nullable Class<D> commonConfig) {
        this(
                clientConfig == null ? null : new ConfigHolder<A>(ConfigContext.CLIENT, clientConfig),
                serverConfig == null ? null : new ConfigHolder<B>(ConfigContext.DEDICATED_SERVER, serverConfig),
                saveConfig == null ? null : new ConfigHolder<C>(ConfigContext.LEVEL, saveConfig),
                commonConfig == null ? null : new ConfigHolder<D>(ConfigContext.COMMON, commonConfig)
        );
    }

    public <A, B, C, D> ModConfig(@Nullable ConfigHolder<A> clientConfig, @Nullable ConfigHolder<B> serverConfig, @Nullable ConfigHolder<C> saveConfig, @Nullable ConfigHolder<D> commonConfig) {
        if(clientConfig != null && clientConfig.getContext() != ConfigContext.CLIENT) throw new IllegalStateException("Client configs must have a CLIENT context");
        if(serverConfig != null && serverConfig.getContext() != ConfigContext.DEDICATED_SERVER) throw new IllegalStateException("Server configs must have a DEDICATED_SERVER context");
        if(saveConfig != null && saveConfig.getContext() != ConfigContext.LEVEL) throw new IllegalStateException("World configs must have a LEVEL context");
        if(commonConfig != null && commonConfig.getContext() != ConfigContext.COMMON) throw new IllegalStateException("Common configs must have a COMMON context");
        add(clientConfig);
        add(serverConfig);
        add(saveConfig);
        add(commonConfig);

        ArrayList<String> fieldNames = new ArrayList<>();
        for (ConfigHolder<?> config : this.getConfigs()) {
            for (ConfigHolder.ConfigField field : config.getFields()) {
                if(fieldNames.contains(field.getSerializedName())) throw new IllegalStateException("Field {} already exists in another context".replace("{}", field.getSerializedName()));
                fieldNames.add(field.getSerializedName());
            }
        }
    }

    public AutomaticDialogInputs getDialogInputs() {
        return dialogInputs;
    }

    public String getModId() {
        return this.modId;
    }

    public Collection<ConfigHolder<?>> getConfigs() {
        return this.configs.values();
    }

    public Collection<ConfigHolder<?>> getAvailableConfigs() {
        return this.configs.values().stream().filter(ConfigHolder::isAvailable).toList();
    }

    public @Nullable ConfigHolder<?> get(ConfigContext context) {
        return this.configs.get(context);
    }

    public @NotNull ConfigHolder<?> getOrThrow(ConfigContext context) {
        if(!this.configs.containsKey(context)) throw new NullPointerException("Cannot get mod config for 1 in context 2".replace("1", this.modId).replace("2", context.getDisplayName()));
        return Objects.requireNonNull(get(context));
    }

    public boolean has(ConfigContext context) {
        return this.configs.containsKey(context);
    }

    public ModConfig add(@Nullable ConfigHolder<?> holder) {
        if (holder == null) {
            return this;
        }

        ConfigHolder<?> existing = this.configs.putIfAbsent(holder.getContext(), holder);
        if (existing != null) {
            throw new IllegalStateException("Duplicate config context " + holder.getContext());
        }

        if (this.modId != null) {
            holder.setModId(this.modId);
        }
        return this;
    }

    public void reloadAvailable() {
        this.configs.values().stream().filter(ConfigHolder::isAvailable).forEach(ConfigHolder::reload);
    }

    public void saveAvailable() {
        this.configs.values().stream().filter(ConfigHolder::isAvailable).forEach(ConfigHolder::save);
    }

    public void unload(ConfigContext context) {
        checkDirty();

        ConfigHolder<?> holder = this.configs.get(context);
        if (holder != null) {
            holder.unload();
        }
    }

    @ApiStatus.Internal
    void setModId(String modId) {
        if (this.modId != null && !this.modId.equals(modId)) {
            throw new IllegalStateException("Config is already bound to mod id " + this.modId);
        }
        this.modId = modId;
        for (Map.Entry<ConfigContext, ConfigHolder<?>> entry : this.configs.entrySet()) {
            entry.getValue().setModId(modId);
        }
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void checkDirty() {
        if(this.dirty) {
            for (ConfigHolder<?> availableConfig : this.getAvailableConfigs()) {
                for (ConfigHolder.ConfigField field : availableConfig.getFields()) {
                    if(availableConfig.getContext() != ConfigContext.CLIENT && field.annotation().sync() && Lib99j.getServer() != null) {
                        for (ServerPlayer player : Lib99j.getServerOrThrow().getPlayerList().players) {
                            if(ModInstallManager.isInstalled(Lib99j.MOD_ID, player)) {
                                ServerPlayNetworking.send(player, new ClientboundLib99jSyncConfigOptionPacket(this.modId, field.getSerializedName(), ConfigUtils.GSON.toJson(field.getValue(availableConfig.get()), field.getValue(availableConfig.get()).getClass())));
                            }
                        }
                    }
                }
            }
            this.saveAvailable();
            this.dirty = false;
        }
    }
}
