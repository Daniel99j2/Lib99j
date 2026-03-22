package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.ponder.api.PonderManager;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.booklet.impl.BookletPage;
import eu.pb4.booklet.impl.PageParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PageParser.class)
public class BookletPageMixin {
    @Inject(method = "readPage", at = @At(value = "INVOKE", target = "Leu/pb4/booklet/impl/PageParser$BodyBuilder;add(Ljava/lang/StringBuilder;IILeu/pb4/booklet/api/body/AlignedMessage$Align;)V", ordinal = 7))
    private void addPonderEntry(Identifier identifier, String pag1e, CallbackInfoReturnable<BookletPage> cir, @Local(name = "contents") StringBuilder contents, @Local(name = "title") String title) {
        if(title.contains("<item '")) {
            Identifier id = Identifier.parse(title.replace("<item '", "").replace("'>", ""));
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if(PonderManager.itemToBuilders.containsKey(item)) {
                contents.append("<nl><u><blue><polydex '" + "ponder_internal:" +id.toString().replace(":", "_lib99jcolon_") + "'>Ponder about "+title+"</>");
            }
        }
    }
}
