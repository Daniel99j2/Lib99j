package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.api.CommandSourceAccessor;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({CommandSourceStack.class})
public abstract class CommandSourceMixin implements CommandSourceAccessor {
    @Unique
    private boolean fromPacket = false;

    @Override
    public void lib99j$setFromPacket(boolean fromPacket) {
        this.fromPacket = fromPacket;
    }

    @Override
    public boolean lib99j$isFromPacket() {
        return fromPacket;
    }
}