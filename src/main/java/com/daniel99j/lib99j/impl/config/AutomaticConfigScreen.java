package com.daniel99j.lib99j.impl.config;

import com.daniel99j.lib99j.api.MiscUtils;
import com.daniel99j.lib99j.api.config.*;
import com.daniel99j.lib99j.impl.ConfigEntryValueHolder;
import com.mojang.datafixers.util.Either;
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
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.input.BooleanInput;
import net.minecraft.server.dialog.input.NumberRangeInput;
import net.minecraft.server.dialog.input.SingleOptionInput;
import net.minecraft.server.dialog.input.TextInput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                .setTitle(Component.literal("test"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        builder.setSavingRunnable(this.modConfig::saveAvailable);

        for (ConfigHolder<?> holder : this.modConfig.getAvailableConfigs()) {
            Object defaultConfig = this.modConfig.get(holder.getContext()).createDefaultInstance();

            for (ConfigHolder.ConfigField field : holder.getFields()) {
                if (!field.isVisible() || !field.isSupportedOnCurrentSide()) {
                    continue;
                }

                Either<AbstractConfigListEntry<?>, Input> entry = buildEntry(Either.left(entryBuilder), holder, field, field.getValue(defaultConfig));
                if (entry != null && entry.left().isPresent()) {
                    //getCategoryName(holder.getContext(), field.annotation().category())
                    ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.lib99j.context.category", Component.translatable("config.lib99j.context."+holder.getContext().getType()), Component.translatable("config."+this.modConfig.getModId()+".category."+field.annotation().category())));
                    category.addEntry(entry.left().get());
                }
            }
        }

        return builder.build();
    }

    public List<Input> buildDialogInputs(ConfigContext context) {
        ArrayList<Input> inputs = new ArrayList<>();

        for (ConfigHolder<?> holder : this.modConfig.getAvailableConfigs()) {
            Object defaultConfig = this.modConfig.get(holder.getContext()).createDefaultInstance();

            for (ConfigHolder.ConfigField field : holder.getFields()) {
                if (!field.isVisible() || !field.isSupportedOnCurrentSide() || field.holder().getContext() != context) {
                    continue;
                }

                Either<AbstractConfigListEntry<?>, Input> entry = buildEntry(Either.right(inputs), holder, field, field.getValue(defaultConfig));
                if (entry != null && entry.right().isPresent()) {
                    inputs.add(entry.right().get());
                }
            }
        }

        return inputs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Either<AbstractConfigListEntry<?>, Input> buildEntry(Either<ConfigEntryBuilder, ArrayList<Input>> entryBuilder, ConfigHolder<?> holder, ConfigHolder.ConfigField configField, Object defaultValue) {
        Object config = holder.get();
        Field field = configField.field();
        Object currentValue = configField.getValue(config);
        Component name = configField.getDisplayName();

        boolean clothConfig = entryBuilder.left().isPresent();

        if (configField.annotation().displayType() == ConfigDisplayType.COLOUR_PICKER && configField.getType() == ConfigEntryType.INT) {
            if(clothConfig) {
                var builder = entryBuilder.left().get().startColorField(name, (Integer) currentValue)
                        .setSaveConsumer(value -> configField.setValue(config, value));
                applyCommonOptions(builder, configField, ((Integer) defaultValue));
                return Either.left(builder.build());
            } else {
                String var10000 = Integer.toHexString((Integer) currentValue);
                int var10001 = 8;
                String v = "#" + StringUtils.leftPad(var10000, var10001, '0');
                return Either.right(new Input(configField.getSerializedName(), new TextInput(200, configField.getDisplayName(), true, v, 1000, Optional.empty())));
            }
        }
        if (configField.annotation().displayType() == ConfigDisplayType.COLOUR_PICKER_ALPHA && configField.getType() == ConfigEntryType.INT) {
            if(clothConfig) {
                var builder = entryBuilder.left().get().startAlphaColorField(name, (Integer) currentValue)
                        .setSaveConsumer(value -> configField.setValue(config, value));
                applyCommonOptions(builder, configField, ((Integer) defaultValue));
                return Either.left(builder.build());
            } else {
                String var10000 = Integer.toHexString((Integer) currentValue);
                int var10001 = 6;
                String v = "#" + StringUtils.leftPad(var10000, var10001, '0');
                return Either.right(new Input(configField.getSerializedName(), new TextInput(200, configField.getDisplayName(), true, v, 1000, Optional.empty())));
            }
        }

        boolean slider = configField.annotation().displayType() == ConfigDisplayType.SLIDER;

        return switch (configField.getType()) {
            case BOOLEAN -> {
                if(clothConfig) {
                    var builder = entryBuilder.left().get().startBooleanToggle(name, (Boolean) currentValue);
                    builder.setSaveConsumer(value -> configField.setValue(config, value));
                    applyCommonOptions(builder, configField, ((Boolean) defaultValue));
                    yield Either.left(builder.build());
                } else {
                    yield Either.right(new Input(configField.getSerializedName(), new BooleanInput(configField.getDisplayName(), (Boolean) currentValue, "True", "False")));
                }
            }
            case INT -> {
                if (clothConfig) {
                    var builder = slider ? entryBuilder.left().get().startIntSlider(name, (Integer) currentValue, (Integer) configField.minMax().min, (Integer) configField.minMax().max) : entryBuilder.left().get().startIntField(name, (Integer) currentValue);
                    builder.setSaveConsumer(value -> configField.setValue(config, value));
                    applyCommonOptions(builder, configField, ((Integer) defaultValue));
                    if(!slider) applyRange((IntFieldBuilder) builder, configField);
                    yield Either.left(builder.build());
                } else {
                    yield Either.right(slider ? createNumberInput(configField, ((Integer) currentValue).floatValue(), (Integer) configField.minMax().min, (Integer) configField.minMax().max, Optional.of(1.0f)) : createTextInput(configField, String.valueOf(currentValue)));
                }
            }
            case FLOAT -> {
                if (clothConfig) {
                    var builder = entryBuilder.left().get().startFloatField(name, (Float) currentValue);
                    builder.setSaveConsumer(value -> configField.setValue(config, value));
                    applyCommonOptions(builder, configField, ((Float) defaultValue));
                    applyRange(builder, configField);
                    yield Either.left(builder.build());
                } else {
                    yield Either.right(slider ? createNumberInput(configField, (Float) currentValue, (Float) configField.minMax().min, (Float) configField.minMax().max, Optional.of(0.01f)) : createTextInput(configField, String.valueOf(currentValue)));
                }
            }
            case DOUBLE -> {
                if (clothConfig) {
                    var builder = entryBuilder.left().get().startDoubleField(name, (Double) currentValue);
                    builder.setSaveConsumer(value -> configField.setValue(config, value));
                    applyCommonOptions(builder, configField, ((Double) defaultValue));
                    applyRange(builder, configField);
                    yield Either.left(builder.build());
                } else {
                    yield Either.right(slider ? createNumberInput(configField, ((Double) currentValue).floatValue(), (Double) configField.minMax().min, (Double) configField.minMax().max, Optional.of(0.01f)) : createTextInput(configField, String.valueOf(currentValue)));
                }
            }
            case SHORT -> {
                if (clothConfig) {
                    var builder = slider ? entryBuilder.left().get().startIntSlider(name, (Short) currentValue, (Short) configField.minMax().min, (Short) configField.minMax().max) : entryBuilder.left().get().startIntField(name, (Short) currentValue);
                    builder.setSaveConsumer(value -> configField.setValue(config, value.shortValue()));
                    applyCommonOptions(builder, configField, ((Short) defaultValue).intValue());
                    if(!slider) applyRangeShort((IntFieldBuilder) builder, configField);
                    yield Either.left(builder.build());
                } else {
                    yield Either.right(slider ? createNumberInput(configField, ((Short) currentValue).floatValue(), (Short) configField.minMax().min, (Short) configField.minMax().max, Optional.of(1.0f)) : createTextInput(configField, String.valueOf(currentValue)));
                }
            }
            case LONG -> {
                if (clothConfig) {
                    var builder = slider ? entryBuilder.left().get().startLongSlider(name, (Long) currentValue, (Long) configField.minMax().min, (Long) configField.minMax().max) : entryBuilder.left().get().startLongField(name, (Long) currentValue);
                    builder.setSaveConsumer(value -> configField.setValue(config, value));
                    applyCommonOptions(builder, configField, ((Long) defaultValue));
                    if(!slider) applyRange((LongFieldBuilder) builder, configField);
                    yield Either.left(builder.build());
                } else {
                    yield Either.right(slider ? createNumberInput(configField, ((Long) currentValue).floatValue(), (Long) configField.minMax().min, (Long) configField.minMax().max, Optional.of(1.0f)) : createTextInput(configField, String.valueOf(currentValue)));
                }
            }
            case STRING -> {
                if (clothConfig) {
                    var builder = entryBuilder.left().get().startStrField(name, (String) currentValue);
                    builder.setSaveConsumer(value -> configField.setValue(config, value));
                    applyCommonOptions(builder, configField, ((String) defaultValue));
                    yield Either.left(builder.build());
                } else {
                    yield Either.right(createTextInput(configField, (String) currentValue));
                }
            }
            case ENUM -> {
                if (clothConfig) {
                    var builder = entryBuilder.left().get().startEnumSelector(name, (Class<Enum>) field.getType(), (Enum) currentValue);
                    builder.setSaveConsumer(value -> configField.setValue(config, value));
                    applyCommonOptions(builder, configField, ((Enum) defaultValue));
                    yield Either.left(builder.build());
                } else {
                    List<SingleOptionInput.Entry> list = new ArrayList<>();
                    for (Enum enumConstant : ((Class<Enum>) field.getType()).getEnumConstants()) {
                        list.add(new SingleOptionInput.Entry(enumConstant.name(), Optional.empty(), currentValue == enumConstant));
                    }
                    yield Either.right(new Input(configField.getSerializedName(), new SingleOptionInput(200, list, configField.getDisplayName(), true)));
                }
            }
            case VEC3 -> {
                if (clothConfig) {
                    var builder = entryBuilder.left().get().startStrField(name, vecToString((Vec3) currentValue))
                            .setSaveConsumer(value -> configField.setValue(config, parseVec3(value, (Vec3) currentValue)));
                    applyCommonOptions(builder, configField, vecToString((Vec3) defaultValue));
                    yield Either.left(builder.build());
                } else {
                    String vec2 = ((Vec3) currentValue).x+","+((Vec3) currentValue).y+","+((Vec3) currentValue).z;
                    yield Either.right(createTextInput(configField, vec2));
                }
            }
            case VEC2 -> {
                if (clothConfig) {
                    var builder = entryBuilder.left().get().startStrField(name, vecToString((Vec2) currentValue))
                            .setSaveConsumer(value -> configField.setValue(config, parseVec2(value, (Vec2) currentValue)));
                    applyCommonOptions(builder, configField, vecToString((Vec2) defaultValue));
                    yield Either.left(builder.build());
                } else {
                    String vec2 = ((Vec2) currentValue).x+","+((Vec2) currentValue).y;
                    yield Either.right(createTextInput(configField, vec2));
                }
            }
            case IDENTIFIER -> {
                if (clothConfig) {
                    var builder = entryBuilder.left().get().startStrField(name, ((Identifier) currentValue).toString())
                            .setSaveConsumer(value -> configField.setValue(config, MiscUtils.fallback(Identifier.tryParse(value), ((Identifier) currentValue))));
                    applyCommonOptions(builder, configField, ((Identifier) defaultValue).toString());
                    yield Either.left(builder.build());
                } else {
                    yield Either.right(createTextInput(configField, currentValue.toString()));
                }
            }
            case CUSTOM, AUTO -> null;
        };
    }

    private static Input createTextInput(ConfigHolder.ConfigField configField, String currentValue) {
        return new Input(configField.getSerializedName(), new TextInput(200, configField.getDisplayName(), true, currentValue, 1000, Optional.empty()));
    }

    private static Input createNumberInput(ConfigHolder.ConfigField configField, float currentValue, double minValue, double maxValue, Optional<Float> step) {
        return new Input(
                configField.getSerializedName(),
                new NumberRangeInput(
                        200,
                        configField.getDisplayName(),
                        "options.generic_value",
                        new NumberRangeInput.RangeInfo((float) minValue, (float) maxValue, Optional.of(currentValue), step)
                )
        );
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
