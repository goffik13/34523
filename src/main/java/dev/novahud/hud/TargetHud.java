package dev.novahud.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/** Панель цели: голова, имя, анимированная полоса здоровья с «хвостом» урона. */
final class TargetHud {
    private LivingEntity target;
    private long lastSeen;
    private float alpha;
    private float shown = 1f;
    private float trail = 1f;

    void render(DrawContext c, MinecraftClient mc, TextRenderer tr, Theme th, int sw, int sh, float dt) {
        long now = System.currentTimeMillis();
        Entity e = mc.targetedEntity;
        if (e instanceof LivingEntity le && le != mc.player && le.isAlive()) {
            if (le != target) {
                shown = trail = le.getHealth() / Math.max(1f, le.getMaxHealth());
            }
            target = le;
            lastSeen = now;
        }
        boolean show = target != null && target.isAlive() && now - lastSeen < 2000;
        alpha = Draw.approach(alpha, show ? 1f : 0f, dt * 6f);
        if (alpha < 0.06f) {
            if (!show) target = null;
            return;
        }
        if (target == null) return;

        float max = Math.max(1f, target.getMaxHealth());
        float ratio = Math.max(0f, Math.min(1f, target.getHealth() / max));
        shown = Draw.approach(shown, ratio, dt * 3f);
        trail = trail > shown ? Draw.approach(trail, shown, dt * 0.6f) : shown;

        int w = 146, h = 46;
        int x = sw / 2 + 26, y = sh / 2 + 16;
        float al = alpha;

        Draw.glow(c, x, y, w, h, 6, th.start(), 6, al);
        Draw.rrect(c, x, y, w, h, 6, Draw.a(Theme.BG, al));

        // голова / иконка
        int hx = x + 6, hy = y + 6, hs = 34;
        Draw.rrect(c, hx - 1, hy - 1, hs + 2, hs + 2, 4, Draw.a(th.at(0.5f), al * 0.9f));
        boolean skinDrawn = false;
        if (target instanceof PlayerEntity pe && mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(pe.getUuid());
            if (entry != null) {
                c.setShaderColor(1f, 1f, 1f, al);
                c.drawTexture(entry.getSkinTexture(), hx, hy, hs, hs, 8f, 8f, 8, 8, 64, 64);
                c.drawTexture(entry.getSkinTexture(), hx, hy, hs, hs, 40f, 8f, 8, 8, 64, 64);
                c.setShaderColor(1f, 1f, 1f, 1f);
                skinDrawn = true;
            }
        }
        if (!skinDrawn) {
            Draw.rrect(c, hx, hy, hs, hs, 3, Draw.a(Theme.BG_LIGHT, al));
            String letter = target.getName().getString().substring(0, 1).toUpperCase();
            float ls = 2.5f;
            Draw.text(c, tr, letter, hx + (hs - Draw.w(tr, letter) * ls) / 2f, hy + (hs - 9f * ls) / 2f, ls, Draw.a(Theme.TEXT, al), true);
        }

        // имя и дистанция
        int tx = x + 46;
        String name = Draw.trim(tr, target.getName().getString(), 92);
        Draw.str(c, tr, name, tx, y + 7, Draw.a(Theme.TEXT, al), true);
        String dist = String.format("%.1fm", mc.player.distanceTo(target));
        Draw.text(c, tr, dist, x + w - 6 - Draw.width(tr, dist, 0.8f), y + 8, 0.8f, Draw.a(Theme.TEXT_DIM, al), false);

        // здоровье
        int bx = tx, by = y + 21, bw = w - 46 - 8, bh = 6;
        Draw.rrect(c, bx, by, bw, bh, 3, Draw.a(0xFF000000, al * 0.45f));
        int trailW = Math.round(bw * trail);
        if (trailW > 0) Draw.bar(c, bx, by, trailW, bh, Draw.a(0xFFFFFFFF, al * 0.35f), Draw.a(0xFFFFFFFF, al * 0.35f));
        int hpW = Math.round(bw * shown);
        if (hpW > 0) {
            int cEnd = Draw.lerp(0xFFFF4D4D, th.end(), shown);
            Draw.bar(c, bx, by, hpW, bh, Draw.a(th.start(), al), Draw.a(cEnd, al));
        }

        String hp = String.format("%.1f / %.1f", target.getHealth(), max);
        Draw.text(c, tr, hp, tx, y + 31, 0.8f, Draw.a(Theme.TEXT_DIM, al), false);
        float abs = target.getAbsorptionAmount();
        if (abs > 0) {
            String ab = String.format("+%.1f", abs);
            Draw.text(c, tr, ab, x + w - 8 - Draw.width(tr, ab, 0.8f), y + 31, 0.8f, Draw.a(0xFFFFD84D, al), false);
        }
    }
}
