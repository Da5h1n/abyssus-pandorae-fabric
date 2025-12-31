package abyssus.pandorae.mixin;

import abyssus.pandorae.AbyssusPandorae;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerEntity.class)
public abstract class ChatMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void abyssus$ensureCustomFontInChat(CallbackInfoReturnable<Text> cir) {
        Text original = cir.getReturnValue();

        var fontSource = new net.minecraft.text.StyleSpriteSource.Font(
                Identifier.of(AbyssusPandorae.MOD_ID, "soul_font")
        );

        // .withParent merges your font into whatever style the name already has
        // (like the Kingdom color), which is much more compatible.
        MutableText fixedText = original.copy().setStyle(
                original.getStyle().withParent(Style.EMPTY.withFont(fontSource))
        );

        cir.setReturnValue(fixedText);
    }
}