package com.daniel99j.lib99j.impl.config;

import com.daniel99j.lib99j.api.config.*;
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

public class AutomaticDialogInputs {
    private final ModConfig modConfig;

    public AutomaticDialogInputs(ModConfig modConfig) {
        this.modConfig = modConfig;
    }

    public List<Input> buildDialogInputs(ConfigContext context) {
        ArrayList<Input> inputs = new ArrayList<>();

        for (ConfigHolder<?> holder : this.modConfig.getAvailableConfigs()) {
            Object defaultConfig = this.modConfig.get(holder.getContext()).createDefaultInstance();

            for (ConfigHolder.ConfigField field : holder.getFields()) {
                if (!field.isVisible() || !field.isSupportedOnCurrentSide() || field.holder().getContext() != context) {
                    continue;
                }

                Input entry = buildEntry(holder, field, field.getValue(defaultConfig));
                if (entry != null) {
                    inputs.add(entry);
                }
            }
        }

        return inputs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Input buildEntry(ConfigHolder<?> holder, ConfigHolder.ConfigField configField, Object defaultValue) {
        Object config = holder.get();
        Field field = configField.field();
        Object currentValue = configField.getValue(config);

        if (configField.annotation().displayType() == ConfigDisplayType.COLOUR_PICKER && configField.getType() == ConfigEntryType.INT) {
            String value = "#" + StringUtils.leftPad(Integer.toHexString((Integer) currentValue), 8, '0');
            return new Input(configField.getSerializedName(), new TextInput(200, configField.getDisplayName(), true, value, 1000, Optional.empty()));
        }
        if (configField.annotation().displayType() == ConfigDisplayType.COLOUR_PICKER_ALPHA && configField.getType() == ConfigEntryType.INT) {
            String value = "#" + StringUtils.leftPad(Integer.toHexString((Integer) currentValue), 6, '0');
            return new Input(configField.getSerializedName(), new TextInput(200, configField.getDisplayName(), true, value, 1000, Optional.empty()));
        }

        boolean slider = configField.annotation().displayType() == ConfigDisplayType.SLIDER;

        return switch (configField.getType()) {
            case BOOLEAN -> new Input(configField.getSerializedName(), new BooleanInput(configField.getDisplayName(), (Boolean) currentValue, "True", "False"));
            case INT -> slider ? createNumberInput(configField, ((Integer) currentValue).floatValue(), (Integer) configField.minMax().min, (Integer) configField.minMax().max, Optional.of(1.0f)) : createTextInput(configField, String.valueOf(currentValue));
            case FLOAT -> slider ? createNumberInput(configField, (Float) currentValue, (Float) configField.minMax().min, (Float) configField.minMax().max, Optional.of(0.01f)) : createTextInput(configField, String.valueOf(currentValue));
            case DOUBLE -> slider ? createNumberInput(configField, ((Double) currentValue).floatValue(), (Double) configField.minMax().min, (Double) configField.minMax().max, Optional.of(0.01f)) : createTextInput(configField, String.valueOf(currentValue));
            case SHORT -> slider ? createNumberInput(configField, ((Short) currentValue).floatValue(), (Short) configField.minMax().min, (Short) configField.minMax().max, Optional.of(1.0f)) : createTextInput(configField, String.valueOf(currentValue));
            case LONG -> slider ? createNumberInput(configField, ((Long) currentValue).floatValue(), (Long) configField.minMax().min, (Long) configField.minMax().max, Optional.of(1.0f)) : createTextInput(configField, String.valueOf(currentValue));
            case STRING -> createTextInput(configField, (String) currentValue);
            case ENUM -> {
                List<SingleOptionInput.Entry> list = new ArrayList<>();
                for (Enum enumConstant : ((Class<Enum>) field.getType()).getEnumConstants()) {
                    list.add(new SingleOptionInput.Entry(enumConstant.name(), Optional.empty(), currentValue == enumConstant));
                }
                yield new Input(configField.getSerializedName(), new SingleOptionInput(200, list, configField.getDisplayName(), true));
            }
            case VEC3 -> createTextInput(configField, vecToString((Vec3) currentValue));
            case VEC2 -> createTextInput(configField, vecToString((Vec2) currentValue));
            case IDENTIFIER -> createTextInput(configField, ((Identifier) currentValue).toString());
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

    private static String vecToString(Vec3 vec3) {
        return vec3.x + "," + vec3.y + "," + vec3.z;
    }

    private static String vecToString(Vec2 vec2) {
        return vec2.x + "," + vec2.y;
    }
}
