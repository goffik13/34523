package dev.novahud.hud;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Небольшой набор 2D-примитивов: скруглённые панели, свечение, градиенты, текст с масштабом. */
public final class Draw {
    private Draw() {}

    public static final Identifier FONT = new Identifier("novahud", "inter");
    private static final Style STYLE = Style.EMPTY.withFont(FONT);

    /** Строка в шрифте Inter. */
    public static Text f(String s) {
        return Text.literal(s).setStyle(STYLE);
    }

    public static int str(DrawContext c, TextRenderer tr, String s, int x, int y, int color, boolean shadow) {
        return c.drawText(tr, f(s), x, y, color, shadow);
    }

    public static int w(TextRenderer tr, String s) {
        return tr.getWidth(f(s));
    }

    public static String trim(TextRenderer tr, String s, int maxW) {
        if (w(tr, s) <= maxW) return s;
        while (s.length() > 1 && w(tr, s + "..") > maxW) s = s.substring(0, s.length() - 1);
        return s + "..";
    }

    public static int a(int color, float alpha) {
        int al = Math.round(((color >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, alpha)));
        return (al << 24) | (color & 0xFFFFFF);
    }

    public static int lerp(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int al = (int) (((c1 >>> 24) & 0xFF) + ((((c2 >>> 24) & 0xFF) - ((c1 >>> 24) & 0xFF)) * t));
        int r = (int) (((c1 >> 16) & 0xFF) + ((((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * t));
        int g = (int) (((c1 >> 8) & 0xFF) + ((((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)) * t));
        int b = (int) ((c1 & 0xFF) + (((c2 & 0xFF) - (c1 & 0xFF)) * t));
        return (al << 24) | (r << 16) | (g << 8) | b;
    }

    public static float approach(float cur, float target, float step) {
        if (cur < target) return Math.min(target, cur + step);
        return Math.max(target, cur - step);
    }

    /** Скруглённый прямоугольник (без сглаживания, но на пиксельном HUD выглядит чисто). */
    public static void rrect(DrawContext c, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int dx = r - (int) Math.round(Math.sqrt(r * r - dy * dy));
            c.fill(x + dx, y + i, x + w - dx, y + i + 1, color);
            c.fill(x + dx, y + h - i - 1, x + w - dx, y + h - i, color);
        }
        c.fill(x, y + r, x + w, y + h - r, color);
    }

    /** Мягкое свечение вокруг панели: несколько расширяющихся полупрозрачных слоёв. */
    public static void glow(DrawContext c, int x, int y, int w, int h, int r, int color, int size, float strength) {
        for (int i = size; i >= 1; i--) {
            float k = 1f - (i - 1f) / size;
            rrect(c, x - i, y - i, w + i * 2, h + i * 2, r + i, a(color, strength * k * k * 0.35f));
        }
    }

    /** Горизонтальный градиент (колонками, подходит для тонких линий и баров). */
    public static void hgrad(DrawContext c, int x, int y, int w, int h, int c1, int c2) {
        for (int i = 0; i < w; i++) {
            c.fill(x + i, y, x + i + 1, y + h, lerp(c1, c2, w <= 1 ? 0 : i / (float) (w - 1)));
        }
    }

    /** Полоса со скруглением и горизонтальным градиентом. */
    public static void bar(DrawContext c, int x, int y, int w, int h, int c1, int c2) {
        if (w <= 0) return;
        int r = Math.min(h / 2, w / 2);
        for (int i = 0; i < w; i++) {
            int inset = 0;
            if (i < r) inset = r - (int) Math.round(Math.sqrt(r * r - Math.pow(r - i - 0.5, 2)));
            else if (i >= w - r) inset = r - (int) Math.round(Math.sqrt(r * r - Math.pow(i - (w - r) + 0.5, 2)));
            c.fill(x + i, y + inset, x + i + 1, y + h - inset, lerp(c1, c2, w <= 1 ? 0 : i / (float) (w - 1)));
        }
    }

    public static void text(DrawContext c, TextRenderer tr, String s, float x, float y, float scale, int color, boolean shadow) {
        MatrixStack m = c.getMatrices();
        m.push();
        m.translate(x, y, 0);
        m.scale(scale, scale, 1f);
        c.drawText(tr, f(s), 0, 0, color, shadow);
        m.pop();
    }

    public static int width(TextRenderer tr, String s, float scale) {
        return Math.round(w(tr, s) * scale);
    }

    /** Текст с градиентом по символам. Возвращает ширину. */
    public static int gradientText(DrawContext c, TextRenderer tr, String s, int x, int y, int c1, int c2, float alpha) {
        int cx = x;
        int n = Math.max(1, s.length() - 1);
        for (int i = 0; i < s.length(); i++) {
            String ch = String.valueOf(s.charAt(i));
            str(c, tr, ch, cx, y, a(lerp(c1, c2, i / (float) n), alpha), true);
            cx += w(tr, ch);
        }
        return cx - x;
    }
}
