package com.daniel99j.lib99j.impl.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Commands.class)
public abstract class CommandsMixin {
    @Inject(method = "argument", at = @At("TAIL"))
    private static void lib99j$makeDimensionsServerSide(String name, ArgumentType<?> type, CallbackInfoReturnable<RequiredArgumentBuilder<CommandSourceStack, ?>> cir) {
        if (type instanceof DimensionArgument) {
            cir.getReturnValue().suggests(type::listSuggestions);
        }
    }
}