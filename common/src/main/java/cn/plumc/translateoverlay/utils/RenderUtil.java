package cn.plumc.translateoverlay.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.Objects;

public class RenderUtil {
    public static void drawTextWithBackground(MatrixStack matrices, TextRenderer textRenderer, Text text, int x, int y, int width, int color) {
        int i = MinecraftClient.getInstance().options.getTextBackgroundColor(0.0F);
        if (i != 0) {
            int var10001 = x - 2;
            int var10002 = y - 2;
            int var10003 = x + width + 2;
            Objects.requireNonNull(textRenderer);
            DrawableHelper.fill(matrices, var10001, var10002, var10003, y + 9 + 2, Argb.mixColor(i, color));
        }
        DrawableHelper.drawTextWithShadow(matrices, textRenderer, text, x, y, color);
    }

    public static int withAlpha(int alpha, int rgb) {
        return alpha << 24 | rgb & 16777215;
    }

    public static int getScaledWidth(){
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }

    public static int getScaledHeight(){
        return MinecraftClient.getInstance().getWindow().getScaledHeight();
    }

    public static class Argb {
        public static int getAlpha(int argb) {
            return argb >>> 24;
        }

        public static int getRed(int argb) {
            return argb >> 16 & 0xFF;
        }

        public static int getGreen(int argb) {
            return argb >> 8 & 0xFF;
        }

        public static int getBlue(int argb) {
            return argb & 0xFF;
        }

        public static int getArgb(int alpha, int red, int green, int blue) {
            return alpha << 24 | red << 16 | green << 8 | blue;
        }

        public static int mixColor(int first, int second) {
            return Argb.getArgb(Argb.getAlpha(first) * Argb.getAlpha(second) / 255, Argb.getRed(first) * Argb.getRed(second) / 255, Argb.getGreen(first) * Argb.getGreen(second) / 255, Argb.getBlue(first) * Argb.getBlue(second) / 255);
        }
    }
}
