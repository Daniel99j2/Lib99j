package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.ponder.impl.PonderLevel;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.AbstractElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractElement.class)
public abstract class LodDisplayMixin {
    @Shadow
    private ElementHolder holder;

    @Inject(method = "setHolder", at = @At("TAIL"))
    private void lib99j$makeLodBetterQualityInPonders(ElementHolder par1, CallbackInfo ci) {
        if(((AbstractElement) (Object) this) instanceof LodItemDisplayElement lod && this.holder.getAttachment() instanceof ChunkAttachment chunkAttachment && chunkAttachment.getWorld() instanceof PonderLevel) {
            ((LodDisplayAccessor) lod).setFar(10000*10000);
            ((LodDisplayAccessor) lod).setNear(1000*1000);
        }
    }
}