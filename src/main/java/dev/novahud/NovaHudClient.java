package dev.novahud;

import dev.novahud.hud.HudRenderer;
import dev.novahud.hud.MenuScreen;
import dev.novahud.hud.Theme;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class NovaHudClient implements ClientModInitializer {
    public static boolean enabled = true;
    public static Theme theme = Theme.VIOLET;
    public static boolean showWatermark = true, showTarget = true, showPotions = true, showArmor = true, showInfo = true;

    // Trigger bot
    public static boolean triggerBot = false, triggerPlayersOnly = false;

    // Aspect ratio
    public static boolean aspectEnabled = false;
    public static final String[] ASPECT_NAMES = {"4:3", "5:4", "1:1", "3:2", "16:10", "21:9", "32:9"};
    public static final float[] ASPECT_VALUES = {4f / 3f, 5f / 4f, 1f, 3f / 2f, 16f / 10f, 21f / 9f, 32f / 9f};
    public static int aspectIndex = 0;

    private static KeyBinding menuKey;
    private static KeyBinding toggleKey;
    private static KeyBinding themeKey;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.novahud.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.categories.novahud"));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.novahud.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.categories.novahud"));
        themeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.novahud.theme", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.novahud"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            TriggerBot.tick(client);
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new MenuScreen());
            }
            while (toggleKey.wasPressed()) enabled = !enabled;
            while (themeKey.wasPressed()) {
                theme = theme.next();
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("NovaHUD theme: " + theme.title), true);
                }
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (enabled) HudRenderer.render(drawContext, tickDelta);
        });
    }
}
