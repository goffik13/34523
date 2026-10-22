package dev.novahud.mixin;

import dev.novahud.NovaHudClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    /** Подменяем aspect ratio в матрице проекции (второй аргумент setPerspective). */
    @ModifyArg(
            method = "getBasicProjectionMatrix",
            at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;setPerspective(FFFF)Lorg/joml/Matrix4f;"),
            index = 1,
            require = 0
    )
    private float novahud$aspect(float original) {
        return NovaHudClient.aspectEnabled ? NovaHudClient.ASPECT_VALUES[NovaHudClient.aspectIndex] : original;
    }
}
