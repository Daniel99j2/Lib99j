package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.config.ConfigContext;
import com.daniel99j.lib99j.impl.Lib99jCommonConfig;
import eu.pb4.booklet.impl.textnode.PolydexNode;
import eu.pb4.placeholders.api.ParserContext;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PolydexNode.class)
public class BookletPageMixin2 {
    @Shadow
    @Final
    private Identifier entry;

    @Inject(method = "applyFormatting", at = @At("HEAD"), cancellable = true)
    private void ponderType(Style style, ParserContext context, CallbackInfoReturnable<Style> cir) {
        if(!((Lib99jCommonConfig) Lib99j.CONFIG.getOrThrow(ConfigContext.COMMON).getOrThrow()).bookletPonderAdditions) return;

        if(this.entry.getNamespace().equals("ponder_internal")) {
            Identifier realId = Identifier.parse(this.entry.getPath().replace("_lib99jcolon_", ":"));
            cir.setReturnValue(style.withClickEvent(new ClickEvent.RunCommand("/ponder item "+ realId)));
        }
    }
}
