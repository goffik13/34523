package dev.novahud.mixin;

import dev.novahud.NovaHudClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    /** Прячем ванильные иконки эффектов, т.к. у нас свой список. */
    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void novahud$hideVanillaEffects(DrawContext context, CallbackInfo ci) {
        if (NovaHudClient.enabled) ci.cancel();
    }
}
