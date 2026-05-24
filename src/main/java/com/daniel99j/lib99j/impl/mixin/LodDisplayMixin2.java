package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.ponder.impl.PonderLevel;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ElementHolder.class)
public abstract class LodDisplayMixin2 {
    @Shadow
    public abstract List<VirtualElement> getElements();

    @Inject(method = "setAttachment", at = @At("TAIL"))
    private void lib99j$makeLodsBetterQualityInPonders(HolderAttachment attachment, CallbackInfo ci) {
        if(attachment instanceof ChunkAttachment chunkAttachment && chunkAttachment.getWorld() instanceof PonderLevel) {
            for (VirtualElement element : this.getElements()) {
                if(element instanceof LodItemDisplayElement lod) {
                    ((LodDisplayAccessor) lod).setFar(10000*10000);
                    ((LodDisplayAccessor) lod).setNear(1000*1000);
                }
            }
        }
    }
}