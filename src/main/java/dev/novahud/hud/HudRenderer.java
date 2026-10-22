package dev.novahud.hud;

import dev.novahud.NovaHudClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HudRenderer {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final Map<StatusEffect, Integer> MAX_DURATION = new HashMap<>();
    private static final TargetHud TARGET = new TargetHud();
    private static long lastNanos = System.nanoTime();
    private static float bps = 0f;

    private HudRenderer() {}

    public static void render(DrawContext c, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.options.hudHidden || mc.options.debugEnabled) return;

        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastNanos) / 1_000_000_000f);
        lastNanos = now;

        TextRenderer tr = mc.textRenderer;
        Theme theme = NovaHudClient.theme;
        int sw = c.getScaledWindowWidth();
        int sh = c.getScaledWindowHeight();

        double dx = player.getX() - player.prevX;
        double dz = player.getZ() - player.prevZ;
        float curBps = (float) (Math.sqrt(dx * dx + dz * dz) * 20.0);
        bps = bps + (curBps - bps) * Math.min(1f, dt * 8f);

        if (NovaHudClient.showWatermark) watermark(c, mc, tr, theme, player);
        if (NovaHudClient.showPotions) potions(c, tr, theme, player, sw);
        int infoTop = NovaHudClient.showInfo ? info(c, tr, theme, player, sw, sh) : sh - 6;
        if (NovaHudClient.showArmor) armor(c, tr, theme, player, sw, infoTop);
        if (NovaHudClient.showTarget) TARGET.render(c, mc, tr, theme, sw, sh, dt);
    }

    // ---------------------------------------------------------------- watermark
    private static void watermark(DrawContext c, MinecraftClient mc, TextRenderer tr, Theme th, ClientPlayerEntity p) {
        int ping = 0;
        if (mc.getNetworkHandler() != null) {
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(p.getUuid());
            if (e != null) ping = e.getLatency();
        }
        String name = "NOVA";
        String info = mc.getCurrentFps() + " fps   " + ping + " ms   " + LocalTime.now().format(TIME);
        int x = 6, y = 6, h = 18;
        int w = 8 + Draw.w(tr, name) + 11 + Draw.w(tr, info) + 8;

        Draw.glow(c, x, y, w, h, 5, th.start(), 5, 1f);
        Draw.rrect(c, x, y, w, h, 5, Theme.BG);
        int nw = Draw.gradientText(c, tr, name, x + 8, y + 5, th.at(0f), th.at(1f), 1f);
        c.fill(x + 8 + nw + 5, y + 5, x + 8 + nw + 6, y + h - 5, 0x40FFFFFF);
        Draw.str(c, tr, info, x + 8 + nw + 11, y + 5, Theme.TEXT, false);
        Draw.hgrad(c, x + 5, y + h - 2, w - 10, 1, th.at(0f), th.at(1f));
    }

    // ------------------------------------------------------------------ potions
    private static void potions(DrawContext c, TextRenderer tr, Theme th, ClientPlayerEntity p, int sw) {
        List<StatusEffectInstance> list = new ArrayList<>(p.getStatusEffects());
        MAX_DURATION.keySet().removeIf(t -> list.stream().noneMatch(i -> i.getEffectType() == t));
        if (list.isEmpty()) return;
        list.sort(Comparator.comparingInt(StatusEffectInstance::getDuration).reversed());

        int rowH = 18, gap = 3, y = 6;
        for (StatusEffectInstance inst : list) {
            StatusEffect type = inst.getEffectType();
            int dur = inst.getDuration();
            boolean inf = dur < 0;
            MAX_DURATION.merge(type, dur, Math::max);

            String label = type.getName().getString() + " " + roman(inst.getAmplifier() + 1);
            String time = inf ? "inf" : fmtTime(dur);
            int w = 8 + 6 + 5 + Draw.w(tr, label) + 10 + Draw.w(tr, time) + 8;
            int x = sw - w - 6;
            int color = 0xFF000000 | type.getColor();

            Draw.glow(c, x, y, w, rowH, 4, color, 3, 0.8f);
            Draw.rrect(c, x, y, w, rowH, 4, Theme.BG);
            Draw.rrect(c, x + 6, y + 6, 6, 6, 3, color);
            Draw.str(c, tr, label, x + 6 + 6 + 5, y + 5, Theme.TEXT, false);
            Draw.str(c, tr, time, x + w - 8 - Draw.w(tr, time), y + 5, dur < 200 && !inf ? 0xFFFF6B6B : Theme.TEXT_DIM, false);

            float ratio = inf ? 1f : Math.min(1f, dur / (float) Math.max(1, MAX_DURATION.get(type)));
            int bw = Math.round((w - 12) * ratio);
            c.fill(x + 6, y + rowH - 3, x + w - 6, y + rowH - 2, 0x30FFFFFF);
            if (bw > 0) Draw.hgrad(c, x + 6, y + rowH - 3, bw, 1, th.at(0f), th.at(1f));
            y += rowH + gap;
        }
    }

    private static String fmtTime(int ticks) {
        int s = ticks / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private static String roman(int n) {
        String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return n >= 1 && n <= 10 ? r[n] : String.valueOf(n);
    }

    // --------------------------------------------------------------------- info
    /** Рисует панель координат справа снизу, возвращает её верхнюю границу по Y. */
    private static int info(DrawContext c, TextRenderer tr, Theme th, ClientPlayerEntity p, int sw, int sh) {
        String coords = String.format("%.1f  %.1f  %.1f", p.getX(), p.getY(), p.getZ());
        String sub = String.format("%.1f b/s   %s", bps, cap(p.getHorizontalFacing().asString()));
        int w = 10 + Math.max(Draw.w(tr, coords), Draw.w(tr, sub)) + 10;
        int h = 28;
        int x = sw - w - 6, y = sh - h - 6;

        Draw.glow(c, x, y, w, h, 5, th.end(), 5, 0.9f);
        Draw.rrect(c, x, y, w, h, 5, Theme.BG);
        c.fill(x, y + 6, x + 2, y + h - 6, th.start());
        Draw.str(c, tr, coords, x + 10, y + 6, Theme.TEXT, false);
        Draw.str(c, tr, sub, x + 10, y + 16, Theme.TEXT_DIM, false);
        return y;
    }

    private static String cap(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // -------------------------------------------------------------------- armor
    private static void armor(DrawContext c, TextRenderer tr, Theme th, ClientPlayerEntity p, int sw, int infoTop) {
        int slot = 22, w = 4 + slot * 4 + 4, h = 30;
        int x = sw - w - 6, y = infoTop - h - 5;

        Draw.glow(c, x, y, w, h, 5, th.start(), 4, 0.7f);
        Draw.rrect(c, x, y, w, h, 5, Theme.BG);

        for (int i = 0; i < 4; i++) {
            ItemStack st = p.getInventory().getArmorStack(3 - i);
            int sx = x + 4 + i * slot;
            Draw.rrect(c, sx + 1, y + 3, 20, 20, 4, Theme.BG_LIGHT);
            if (st.isEmpty()) continue;
            c.drawItem(st, sx + 3, y + 5);
            if (st.isDamageable()) {
                float r = 1f - st.getDamage() / (float) st.getMaxDamage();
                int bw = Math.max(1, Math.round(18 * r));
                c.fill(sx + 2, y + 24, sx + 20, y + 26, 0x60000000);
                c.fill(sx + 2, y + 24, sx + 2 + bw, y + 26, Draw.lerp(0xFFFF4D4D, 0xFF4DFF88, r));
            }
        }
    }
}
