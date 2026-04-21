package com.daniel99j.lib99j.impl.config;

import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.lib99j.api.config.ConfigDisplayType;
import com.daniel99j.lib99j.api.config.ConfigEntryType;
import com.daniel99j.lib99j.api.config.ConfigHolder;
import com.daniel99j.lib99j.api.config.ModConfig;
import com.daniel99j.lib99j.impl.ConfigEntryValueHolder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.util.NullScreenFactory;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;

public class AutomaticConfigScreen {
    private final ModConfig modConfig;

    public AutomaticConfigScreen(ModConfig modConfig) {
        this.modConfig = modConfig;
    }

    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (this.modConfig.getAvailableConfigs().isEmpty()) {
            return new NullScreenFactory<>();
        }
        return this::buildScreen;
    }

    private Screen buildScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config."+modConfig.getModId()));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        builder.setSavingRunnable(this.modConfig::saveAvailable);

        for (ConfigHolder<?> holder : this.modConfig.getAvailableConfigs()) {
            Object defaultConfig = this.modConfig.get(holder.getContext()).createDefaultInstance();

            for (ConfigHolder.ConfigField field : holder.getFields()) {
                if (!field.isVisible() || !field.isSupportedOnCurrentSide()) {
                    continue;
                }

                AbstractConfigListEntry<?> entry = buildEntry(entryBuilder, holder, field, field.getValue(defaultConfig));
                //getCategoryName(holder.getContext(), field.annotation().category())
                ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.lib99j.context.category", Component.translatable("config.lib99j.context." + holder.getContext().getType()), Component.translatable("config." + this.modConfig.getModId() + ".category." + field.annotation().category())));
                category.addEntry(entry);
            }
        }

        return builder.build();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private AbstractConfigListEntry<?> buildEntry(ConfigEntryBuilder entryBuilder, ConfigHolder<?> holder, ConfigHolder.ConfigField configField, Object defaultValue) {
        Object config = holder.get();
        Field field = configField.field();
        Object currentValue = configField.getValue(config);
        Component name = configField.getDisplayName();


        if (configField.annotation().displayType() == ConfigDisplayType.COLOUR_PICKER && configField.getType() == ConfigEntryType.INT) {
                var builder = entryBuilder.startColorField(name, (Integer) currentValue)
                        .setSaveConsumer(value -> configField.setValue(config, value));
                applyCommonOptions(builder, configField, ((Integer) defaultValue));
                return builder.build();
        }
        if (configField.annotation().displayType() == ConfigDisplayType.COLOUR_PICKER_ALPHA && configField.getType() == ConfigEntryType.INT) {
                var builder = entryBuilder.startAlphaColorField(name, (Integer) currentValue)
                        .setSaveConsumer(value -> configField.setValue(config, value));
                applyCommonOptions(builder, configField, ((Integer) defaultValue));
                return builder.build();
        }

        boolean slider = configField.annotation().displayType() == ConfigDisplayType.SLIDER;

        return switch (configField.getType()) {
            case BOOLEAN -> {
                    var builder = entryBuilder.startBooleanToggle(name, (Boolean) currentValue);
                    builder.setSaveConsumer(value -> configField.setValue(config, value));
                    applyCommonOptions(builder, configField, ((Boolean) defaultValue));
                    yield builder.build();
            }
            case INT -> {
                    var builder = slider ? entryBuilder.startIntSlider(name, (Integer) currentValue, (Integer) configField.minMax().min, (Integer) configField.minMax().max) : entryBuilder.startIntField(name, (Integer) currentValue);
                    builder.setSaveConsumer(value -> configField.setValue(config, value));
                    applyCommonOptions(builder, configField, ((Integer) defaultValue));
                    if(!slider) applyRange((IntFieldBuilder) builder, configField);
                    yield builder.build();
            }
            case FLOAT -> {
                var builder = entryBuilder.startFloatField(name, (Float) currentValue);
                builder.setSaveConsumer(value -> configField.setValue(config, value));
                applyCommonOptions(builder, configField, ((Float) defaultValue));
                applyRange(builder, configField);
                yield builder.build();
            }
            case DOUBLE -> {
                var builder = entryBuilder.startDoubleField(name, (Double) currentValue);
                builder.setSaveConsumer(value -> configField.setValue(config, value));
                applyCommonOptions(builder, configField, ((Double) defaultValue));
                applyRange(builder, configField);
                yield builder.build();
            }
            case SHORT -> {
                var builder = slider ? entryBuilder.startIntSlider(name, (Short) currentValue, (Short) configField.minMax().min, (Short) configField.minMax().max) : entryBuilder.startIntField(name, (Short) currentValue);
                builder.setSaveConsumer(value -> configField.setValue(config, value.shortValue()));
                applyCommonOptions(builder, configField, ((Short) defaultValue).intValue());
                if(!slider) applyRangeShort((IntFieldBuilder) builder, configField);
                yield builder.build();
            }
            case LONG -> {
                var builder = slider ? entryBuilder.startLongSlider(name, (Long) currentValue, (Long) configField.minMax().min, (Long) configField.minMax().max) : entryBuilder.startLongField(name, (Long) currentValue);
                builder.setSaveConsumer(value -> configField.setValue(config, value));
                applyCommonOptions(builder, configField, ((Long) defaultValue));
                if(!slider) applyRange((LongFieldBuilder) builder, configField);
                yield builder.build();
            }
            case STRING -> {
                var builder = entryBuilder.startStrField(name, (String) currentValue);
                builder.setSaveConsumer(value -> configField.setValue(config, value));
                applyCommonOptions(builder, configField, ((String) defaultValue));
                yield builder.build();
            }
            case ENUM -> {
                var builder = entryBuilder.startEnumSelector(name, (Class<Enum>) field.getType(), (Enum) currentValue);
                builder.setSaveConsumer(value -> configField.setValue(config, value));
                applyCommonOptions(builder, configField, ((Enum) defaultValue));
                yield builder.build();
            }
            case VEC3 -> {
                var builder = entryBuilder.startStrField(name, vecToString((Vec3) currentValue))
                        .setSaveConsumer(value -> configField.setValue(config, parseVec3(value, (Vec3) currentValue)));
                applyCommonOptions(builder, configField, vecToString((Vec3) defaultValue));
                yield builder.build();
            }
            case VEC2 -> {
                var builder = entryBuilder.startStrField(name, vecToString((Vec2) currentValue))
                        .setSaveConsumer(value -> configField.setValue(config, parseVec2(value, (Vec2) currentValue)));
                applyCommonOptions(builder, configField, vecToString((Vec2) defaultValue));
                yield builder.build();
            }
            case IDENTIFIER -> {
                var builder = entryBuilder.startStrField(name, ((Identifier) currentValue).toString())
                        .setSaveConsumer(value -> configField.setValue(config, MiscUtils.fallback(Identifier.tryParse(value), ((Identifier) currentValue))));
                applyCommonOptions(builder, configField, ((Identifier) defaultValue).toString());
                yield builder.build();
            }
            case CUSTOM, AUTO -> null;
        };
    }

    private static <T> void applyCommonOptions(AbstractFieldBuilder<T, ?, ?> builder, ConfigHolder.ConfigField configField, T defaultValue) {
        if (configField.annotation().requiresRestart()) {
            builder.requireRestart();
        }
        builder.setDefaultValue(defaultValue);
        ((ConfigEntryValueHolder) builder).lib99j$setValue(configField);
    }

    private static void applyRangeShort(IntFieldBuilder builder, ConfigHolder.ConfigField configField) {
        builder.setMax((short) configField.minMax().max);
        builder.setMin((short) configField.minMax().min);
    }

    private static void applyRange(IntFieldBuilder builder, ConfigHolder.ConfigField configField) {
        builder.setMax((int) configField.minMax().max);
        builder.setMin((int) configField.minMax().min);
    }

    private static void applyRange(FloatFieldBuilder builder, ConfigHolder.ConfigField configField) {
        builder.setMax((float) configField.minMax().max);
        builder.setMin((float) configField.minMax().min);
    }

    private static void applyRange(DoubleFieldBuilder builder, ConfigHolder.ConfigField configField) {
        builder.setMax((double) configField.minMax().max);
        builder.setMin((double) configField.minMax().min);
    }

    private static void applyRange(LongFieldBuilder builder, ConfigHolder.ConfigField configField) {
        builder.setMax((long) configField.minMax().max);
        builder.setMin((long) configField.minMax().min);
    }

    private static String vecToString(Vec3 vec3) {
        return vec3.x + ", " + vec3.y + ", " + vec3.z;
    }

    private static String vecToString(Vec2 vec3) {
        return vec3.x + ", " + vec3.y;
    }

    private static Vec3 parseVec3(String value, Vec3 fallback) {
        String[] split = value.split(",");
        if (split.length != 3) {
            return fallback;
        }
        try {
            return new Vec3(
                    Double.parseDouble(split[0].trim()),
                    Double.parseDouble(split[1].trim()),
                    Double.parseDouble(split[2].trim())
            );
        } catch (Exception e) {
            return fallback;
        }
    }

    private static Vec2 parseVec2(String value, Vec2 fallback) {
        String[] split = value.split(",");
        if (split.length != 2) {
            return fallback;
        }
        try {
            return new Vec2(
                    (float) Double.parseDouble(split[0].trim()),
                    (float) Double.parseDouble(split[1].trim())
            );
        } catch (Exception e) {
            return fallback;
        }
    }
}
