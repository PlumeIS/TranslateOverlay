package cn.plumc.translateoverlay.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.Objects;

public class RenderUtil {
    public static int drawTextWithBackground(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int width, int color) {
        int i = MinecraftClient.getInstance().options.getTextBackgroundColor(0.0F);
        if (i != 0) {
            int var10001 = x - 2;
            int var10002 = y - 2;
            int var10003 = x + width + 2;
            Objects.requireNonNull(textRenderer);
            context.fill(var10001, var10002, var10003, y + 9 + 2, ColorHelper.Argb.mixColor(i, color));
        }

        return context.drawText(textRenderer, text, x, y, color, true);
    }

    public static int withAlpha(int alpha, int rgb) {
        return alpha << 24 | rgb & 16777215;
    }
}
