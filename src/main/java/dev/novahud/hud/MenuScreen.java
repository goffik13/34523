package dev.novahud.hud;

import dev.novahud.NovaHudClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Меню настроек (Right Shift). Строки: переключатели и значения, листаемые ЛКМ/ПКМ. */
public class MenuScreen extends Screen {
    private static final int W = 200, ROW = 18, PAD = 10, HEAD = 28;

    private enum Kind { TOGGLE, CYCLE }

    private record Row(String name, Kind kind) {}

    private static final Row[] ROWS = {
            new Row("Watermark", Kind.TOGGLE),   // 0
            new Row("Target HUD", Kind.TOGGLE),  // 1
            new Row("Potions", Kind.TOGGLE),     // 2
            new Row("Armor", Kind.TOGGLE),       // 3
            new Row("Info", Kind.TOGGLE),        // 4
            new Row("Theme", Kind.CYCLE),        // 5
            new Row("Trigger Bot", Kind.TOGGLE), // 6
            new Row("Players only", Kind.TOGGLE),// 7
            new Row("Aspect Ratio", Kind.TOGGLE),// 8
            new Row("Ratio", Kind.CYCLE),        // 9
    };

    public MenuScreen() {
        super(Text.literal("NovaHUD"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private boolean get(int i) {
        return switch (i) {
            case 0 -> NovaHudClient.showWatermark;
            case 1 -> NovaHudClient.showTarget;
            case 2 -> NovaHudClient.showPotions;
            case 3 -> NovaHudClient.showArmor;
            case 4 -> NovaHudClient.showInfo;
            case 6 -> NovaHudClient.triggerBot;
            case 7 -> NovaHudClient.triggerPlayersOnly;
            default -> NovaHudClient.aspectEnabled;
        };
    }

    private String value(int i) {
        return i == 5 ? NovaHudClient.theme.title : NovaHudClient.ASPECT_NAMES[NovaHudClient.aspectIndex];
    }

    private void change(int i, int dir) {
        switch (i) {
            case 0 -> NovaHudClient.showWatermark = !NovaHudClient.showWatermark;
            case 1 -> NovaHudClient.showTarget = !NovaHudClient.showTarget;
            case 2 -> NovaHudClient.showPotions = !NovaHudClient.showPotions;
            case 3 -> NovaHudClient.showArmor = !NovaHudClient.showArmor;
            case 4 -> NovaHudClient.showInfo = !NovaHudClient.showInfo;
            case 5 -> NovaHudClient.theme = NovaHudClient.theme.next();
            case 6 -> NovaHudClient.triggerBot = !NovaHudClient.triggerBot;
            case 7 -> NovaHudClient.triggerPlayersOnly = !NovaHudClient.triggerPlayersOnly;
            case 8 -> NovaHudClient.aspectEnabled = !NovaHudClient.aspectEnabled;
            default -> {
                int n = NovaHudClient.ASPECT_NAMES.length;
                NovaHudClient.aspectIndex = (NovaHudClient.aspectIndex + dir + n) % n;
                NovaHudClient.aspectEnabled = true;
            }
        }
    }

    private int panelX() { return (width - W) / 2; }
    private int panelH() { return HEAD + ROWS.length * ROW + PAD; }
    private int panelY() { return (height - panelH()) / 2; }

    private boolean over(double mx, double my, int i) {
        int x = panelX(), ry = panelY() + HEAD + i * ROW;
        return mx >= x + 4 && mx < x + W - 4 && my >= ry && my < ry + ROW - 2;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        renderBackground(c);
        Theme th = NovaHudClient.theme;
        int x = panelX(), y = panelY(), h = panelH();

        Draw.glow(c, x, y, W, h, 6, th.start(), 8, 1f);
        Draw.rrect(c, x, y, W, h, 6, Theme.BG);
        Draw.gradientText(c, textRenderer, "NOVAHUD", x + PAD, y + 9, th.at(0f), th.at(1f), 1f);
        Draw.hgrad(c, x + PAD, y + 22, W - PAD * 2, 1, th.at(0f), th.at(1f));

        for (int i = 0; i < ROWS.length; i++) {
            int ry = y + HEAD + i * ROW;
            if (over(mx, my, i)) Draw.rrect(c, x + 4, ry, W - 8, ROW - 2, 4, Theme.BG_LIGHT);
            Draw.str(c, textRenderer, ROWS[i].name(), x + PAD, ry + 5, Theme.TEXT, false);

            if (ROWS[i].kind() == Kind.TOGGLE) {
                boolean on = get(i);
                int sx = x + W - PAD - 24, sy = ry + 3;
                Draw.rrect(c, sx, sy, 24, 10, 5, on ? Draw.a(th.at(0.5f), 0.9f) : 0xFF2A2A3C);
                Draw.rrect(c, on ? sx + 14 : sx + 2, sy + 2, 8, 6, 3, 0xFFFFFFFF);
            } else {
                String t = value(i);
                Draw.str(c, textRenderer, t, x + W - PAD - Draw.w(textRenderer, t), ry + 5, th.at(0.5f), false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = 0; i < ROWS.length; i++) {
            if (over(mx, my, i)) {
                change(i, button == 1 ? -1 : 1);
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }
}
