package com.daniel99j.lib99j.impl.mixin;

import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SharedConstants.class)
public interface SharedConstantsAccessor {
    @Mutable
    @Accessor("IS_RUNNING_IN_IDE")
    static void setInIde(boolean inIde) {

    }
}