package xyz.plumc.chattranslate.utils;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import xyz.plumc.chattranslate.ChatTranslate;
import xyz.plumc.chattranslate.command.TranslateCommand;
import xyz.plumc.chattranslate.translates.OCRTranslator;
import xyz.plumc.chattranslate.translates.ocr.OCRHud;

public class KeyUtil {
    private static boolean OCRKeyPress = false;
    private static boolean CtrlPress = false;

    public static void onKeyPress() {
        if (ChatTranslate.translateKey.wasPressed()) {
            TranslateCommand.translate();
        }
        if (ChatTranslate.OCRKey.wasPressed()) {
            OCRTranslator.translate(false);
        }
    }

    public static void onScreenKeyPress(int keyCode, int scancode){
        if (MinecraftClient.getInstance().currentScreen != null && GLFW.GLFW_KEY_LEFT_CONTROL == keyCode){
            CtrlPress = true;
        }
        else if (MinecraftClient.getInstance().currentScreen != null && ChatTranslate.OCRKey.matchesKey(keyCode, scancode)){
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
        if (ChatTranslate.OCRKey.matchesKey(keyCode, scancode)){
            OCRKeyPress = false;
        }
    }
}
