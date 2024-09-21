package cn.plumc.translateoverlay.utils;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import cn.plumc.translateoverlay.TranslateOverlay;
import cn.plumc.translateoverlay.command.TranslateCommand;
import cn.plumc.translateoverlay.translates.OCRTranslator;
import cn.plumc.translateoverlay.translates.ocr.OCRHud;

public class KeyUtil {
    private static boolean OCRKeyPress = false;
    private static boolean CtrlPress = false;

    public static void onKeyPress() {
        if (TranslateOverlay.translateKey.wasPressed()) {
            TranslateCommand.translate();
        }
        if (TranslateOverlay.OCRKey.wasPressed()) {
            OCRTranslator.translate(false);
        }
    }

    public static void onScreenKeyPress(int keyCode, int scancode){
        if (MinecraftClient.getInstance().currentScreen != null && GLFW.GLFW_KEY_LEFT_CONTROL == keyCode){
            CtrlPress = true;
        }
        else if (MinecraftClient.getInstance().currentScreen != null && TranslateOverlay.OCRKey.matchesKey(keyCode, scancode)){
            if (!OCRKeyPress && CtrlPress) OCRTranslator.translate(false);
            OCRKeyPress = true;
        }
        else {
            OCRHud.instance.clearWithFadeOut();
        }
    }

    public static void onScreenKeyReleased(int keyCode, int scancode){
        if (GLFW.GLFW_KEY_LEFT_CONTROL == keyCode){
            CtrlPress = false;
        }
        if (TranslateOverlay.OCRKey.matchesKey(keyCode, scancode)){
            OCRKeyPress = false;
        }
    }
}
