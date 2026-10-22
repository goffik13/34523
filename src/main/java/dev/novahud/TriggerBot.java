package dev.novahud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

/** Простой trigger bot: бьёт цель под прицелом, когда кулдаун атаки восстановился. */
public final class TriggerBot {
    private TriggerBot() {}

    public static void tick(MinecraftClient mc) {
        if (!NovaHudClient.triggerBot) return;
        if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.currentScreen != null) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult hit)) return;

        Entity e = hit.getEntity();
        if (!(e instanceof LivingEntity le) || !le.isAlive()) return;
        if (NovaHudClient.triggerPlayersOnly && !(e instanceof PlayerEntity)) return;
        if (mc.player.isUsingItem()) return;
        if (mc.player.getAttackCooldownProgress(0.5f) < 0.95f) return;

        mc.interactionManager.attackEntity(mc.player, e);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
