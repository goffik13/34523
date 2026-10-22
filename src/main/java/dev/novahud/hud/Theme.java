package dev.novahud.hud;

import net.minecraft.util.math.MathHelper;

public enum Theme {
    VIOLET("Violet", 0xFF8B5CF6, 0xFFEC4899),
    OCEAN("Ocean", 0xFF38BDF8, 0xFF6366F1),
    MINT("Mint", 0xFF34D399, 0xFF22D3EE),
    EMBER("Ember", 0xFFF97316, 0xFFEF4444),
    RAINBOW("Rainbow", 0, 0);

    public static final int BG = 0xD00B0B14;
    public static final int BG_LIGHT = 0xB0181826;
    public static final int TEXT = 0xFFE8E8F2;
    public static final int TEXT_DIM = 0xFF8C8CA6;

    public final String title;
    private final int c1, c2;

    Theme(String title, int c1, int c2) {
        this.title = title;
        this.c1 = c1;
        this.c2 = c2;
    }

    public Theme next() {
        Theme[] v = values();
        return v[(ordinal() + 1) % v.length];
    }

    /** Цвет акцента для позиции t (0..1) вдоль градиента. */
    public int at(float t) {
        if (this == RAINBOW) {
            float hue = ((System.currentTimeMillis() % 6000L) / 6000f + t * 0.25f) % 1f;
            return 0xFF000000 | MathHelper.hsvToRgb(hue, 0.65f, 1f);
        }
        return Draw.lerp(c1, c2, t);
    }

    public int start() { return at(0f); }
    public int end() { return at(1f); }
}
