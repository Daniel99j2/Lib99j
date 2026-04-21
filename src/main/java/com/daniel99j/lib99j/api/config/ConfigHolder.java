package com.daniel99j.lib99j.api.config;

import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.impl.config.ConfigMinMax;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigHolder<T> {
    private final ConfigContext context;
    private final String fileName;
    private final Class<T> configClass;
    private final List<ConfigField> fields;
    private @Nullable String modId;
    private @Nullable T instance;

    public ConfigHolder(ConfigContext context, Class<T> configClass) {
        this(context, context.getType(), configClass);
    }

    public ConfigHolder(ConfigContext context, String fileName, Class<T> configClass) {
        this.context = context;
        this.fileName = fileName;
        this.configClass = configClass;
        this.fields = discoverFields(configClass, this);
    }

    public Class<T> getConfigClass() {
        return configClass;
    }

    public ConfigContext getContext() {
        return this.context;
    }

    public boolean isAvailable() {
        return this.context.isAvailable();
    }

    public synchronized @NotNull T getOrThrow() {
        T out = get();
        if(out == null) throw new IllegalStateException("Could not get config");
        return out;
    }


    public synchronized T get() {
        if(!this.isAvailable()) return null;
        if (this.instance == null) {
            this.instance = load();
        }
        return this.instance;
    }

    public synchronized void reload() {
        this.instance = load();
    }

    public synchronized void unload() {
        this.instance = null;
    }

    public void markDirty() {
        ConfigManager.configs.get(this.modId).markDirty();
    }

    public synchronized void save() {
        T value = get();
        Path folder = getFolderOrThrow();

        try {
            Files.createDirectories(folder);

            ConfigUtils.saveConfig(this.fileName, folder, value);
        } catch (Exception e) {
            Lib99j.LOGGER.error("Error saving config from mod {}, type: {}", this.modId, this.context, e);
        }
    }

    public List<ConfigField> getFields() {
        return this.fields;
    }

    public @Nullable Path getResolvedPath() {
        if (this.modId == null || !this.context.isAvailable()) {
            return null;
        }
        return this.context.resolveFolder(this.modId).resolve(this.fileName + ".json");
    }

    @ApiStatus.Internal
    void setModId(String modId) {
        if (this.modId != null && !this.modId.equals(modId)) {
            throw new IllegalStateException("Config holder is already bound to mod id " + this.modId);
        }
        this.modId = modId;
    }

    private T load() {
        Path folder = getFolderOrThrow();
        return ConfigUtils.loadConfig(this.fileName, folder, this.configClass);
    }

    private Path getFolderOrThrow() {
        if (this.modId == null) {
            throw new IllegalStateException("Config holder has not been registered yet");
        }
        return this.context.resolveFolder(this.modId);
    }

    public T createDefaultInstance() {
        try {
            return this.configClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Invalid config class " + this.configClass, e);
        }
    }

    public static List<ConfigField> discoverFields(Class<?> configClass, ConfigHolder<?> holder) {
        List<ConfigField> out = new ArrayList<>();
        Class<?> currentClass = configClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
                if (entry == null) {
                    continue;
                }

                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers()) || Modifier.isNative(field.getModifiers())) {
                    throw new IllegalStateException("Config entries must not be static, transient, final, private, or native");
                }

                field.setAccessible(true);

                ConfigMinMax<?> minMax = new ConfigMinMax<>(null, null);

                Class<?> realClass = field.getType();

                boolean isNumber = false;
                boolean isColourPicker = entry.displayType() == ConfigDisplayType.COLOUR_PICKER || entry.displayType() == ConfigDisplayType.COLOUR_PICKER_ALPHA;

                if (realClass.equals(Integer.class) || realClass.getName().equals("int")) {
                    realClass = Integer.class;
                    isNumber = true;
                } else if (realClass.equals(Double.class) || realClass.getName().equals("double")) {
                    realClass = Double.class;
                    isNumber = true;
                } else if (realClass.equals(Short.class) || realClass.getName().equals("short")) {
                    realClass = Short.class;
                    isNumber = true;
                } else if (realClass.equals(Long.class) || realClass.getName().equals("long")) {
                    realClass = Long.class;
                    isNumber = true;
                } else if (realClass.equals(Boolean.class) || realClass.getName().equals("boolean"))
                    realClass = Boolean.class;
                else if (realClass.equals(Character.class) || realClass.getName().equals("char"))
                    realClass = Character.class;

                if(!((Double) entry.max()).isNaN() && !((Double) entry.min()).isNaN()) {
                    if(realClass == Integer.class) minMax = new ConfigMinMax<>(((Double) entry.min()).intValue(), ((Double) entry.max()).intValue());
                    if(realClass == Double.class) minMax = new ConfigMinMax<>(entry.min(), entry.max());
                    if(realClass == Short.class) minMax = new ConfigMinMax<>(((Double) entry.min()).shortValue(), ((Double) entry.max()).shortValue());
                    if(realClass == Long.class) minMax = new ConfigMinMax<>(((Double) entry.min()).longValue(), ((Double) entry.max()).longValue());
                } else if(isNumber && !isColourPicker) throw new IllegalArgumentException("Number inputs must have minimum and maximum values (reading '1')".replace("1", field.toString()));

                if(isColourPicker && !realClass.equals(Integer.class)) throw new IllegalArgumentException("Only integers can be a colour picker (reading '1')".replace("1", field.toString()));
                if(entry.displayType() == ConfigDisplayType.SLIDER && !isNumber) throw new IllegalArgumentException("Only numbers can be a slider (reading '1')".replace("1", field.toString()));

                out.add(new ConfigField(field, entry, holder, realClass, minMax));
            }
            currentClass = currentClass.getSuperclass();
        }
        return List.copyOf(out);
    }

    public void reset() {
        if(this.instance != null) {
            this.instance = this.createDefaultInstance();
            this.markDirty();
        };
    }

    public record ConfigField(Field field, ConfigEntry annotation, ConfigHolder<?> holder, Class<?> realClass, ConfigMinMax<?> minMax) {
        public ConfigEntryType getType() {
            return this.annotation.type() == ConfigEntryType.AUTO ? ConfigEntryType.autoType(this.realClass) : this.annotation.type();
        }

        public String getSerializedName() {
            SerializedName serializedName = this.field.getAnnotation(SerializedName.class);
            return serializedName != null ? serializedName.value() : this.field.getName();
        }

        public boolean isVisible() {
            return this.annotation.show();
        }

        public boolean isSupportedOnCurrentSide() {
            Environment environment = this.field.getAnnotation(Environment.class);
            if (environment == null) {
                return true;
            }
            return environment.value() == FabricLoader.getInstance().getEnvironmentType();
        }

        public Component getDisplayName() {
            return Component.translatable("config."+holder.modId+"."+this.getSerializedName());
        }

        public @Nullable Object getValue(Object instance) {
            try {
                return this.field.get(instance);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to read config field " + this.field.getName(), e);
            }
        }

        public void setValueAutoCorrect(Object instance, Object value) {
            setValue(instance, autoCorrect(value));
        }

        public Object autoCorrect(Object value) {

            try {
                if ((this.realClass.equals(Integer.class)) && value instanceof String str && (this.annotation.displayType() == ConfigDisplayType.COLOUR_PICKER_ALPHA || this.annotation.displayType() == ConfigDisplayType.COLOUR_PICKER)) {
                    if (str.startsWith("#") && (str.length() == 7 || str.length() == 9)) {
                        return (int) Long.parseLong(str.substring(1), 16);
                    } else {
                        return 0;
                    }
                } else if (this.realClass.equals(Vec2.class) && value instanceof String str) {
                    float x = Float.parseFloat(MiscUtils.getTextBetween(str, "", ","));
                    str = MiscUtils.replaceTextBetween(str, "", ",", "");
                    float y = Float.parseFloat(str);
                    return new Vec2(x, y);
                } else if (this.realClass.equals(Vec3.class) && value instanceof String str) {
                    float x = Float.parseFloat(MiscUtils.getTextBetween(str, "", ","));
                    str = MiscUtils.replaceTextBetween(str, "", ",", "");
                    float y = Float.parseFloat(MiscUtils.getTextBetween(str, "", ","));
                    str = MiscUtils.replaceTextBetween(str, "", ",", "");
                    float z = Float.parseFloat(str);
                    return new Vec3(x, y, z);
                } else if (this.realClass.equals(Identifier.class) && value instanceof String str) {
                    return Identifier.tryParse(str);
                } else if (this.realClass.isEnum() && value instanceof String str) {
                    //noinspection unchecked,rawtypes
                    return Enum.valueOf((Class<? extends Enum>) this.realClass, str);
                }

                if (value instanceof String str) {
                    if(this.minMax.max instanceof Number) value = Double.parseDouble(str);
                }

                if (this.realClass.equals(Integer.class))
                    return ((Number) value).intValue();
                if (this.realClass.equals(Double.class))
                    return ((Number) value).doubleValue();
                else if (this.realClass.equals(Short.class))
                    return ((Number) value).shortValue();
                else if (this.realClass.equals(Long.class))
                    return ((Number) value).longValue();
            } catch (Exception e) {
                if (Lib99j.isDevelopmentEnvironment) Lib99j.LOGGER.error("Error auto-correcting value", e);
                return value;
            }

            return value;
        }

        public void setValue(Object instance, Object value) {
            if(this.minMax.max instanceof Number && value instanceof Number number) {
                if (number.doubleValue() > ((Number) this.minMax.max).doubleValue())
                    value = this.minMax.max;
                if (number.doubleValue() < ((Number) this.minMax.min).doubleValue())
                    value = this.minMax.min;
            }

            try {
                Object old = this.field.get(instance);

                this.field.set(instance, value);

                if(value != old) {
                    this.holder.markDirty();
                };
            } catch (Exception e) {
                throw new IllegalStateException("Failed to write config field " + this.field.getName(), e);
            }
        }
    }
}
