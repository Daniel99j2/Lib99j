package com.daniel99j.lib99j.impl.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.argument.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {
    @Inject(method = "argument", at = @At("TAIL"))
    private static void lib99j$makeDimensionsServerSide(String name, ArgumentType<?> type, CallbackInfoReturnable<RequiredArgumentBuilder<ServerCommandSource, ?>> cir) {
        if (type instanceof DimensionArgumentType) {
            cir.getReturnValue().suggests(type::listSuggestions);
        }
    }
}