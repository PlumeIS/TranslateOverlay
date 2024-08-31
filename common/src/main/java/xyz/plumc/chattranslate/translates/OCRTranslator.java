package xyz.plumc.chattranslate.translates;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import xyz.plumc.chattranslate.ChatTranslate;
import xyz.plumc.chattranslate.config.Config;
import xyz.plumc.chattranslate.translates.ocr.AnimationOCRResult;
import xyz.plumc.chattranslate.translates.ocr.OCRHud;
import xyz.plumc.chattranslate.utils.TickUtil;

import java.util.List;

public class OCRTranslator {
    public static void translate(boolean withGUI){
        MinecraftClient minecraft = MinecraftClient.getInstance();
        minecraft.options.hudHidden = !withGUI;
        OCRHud.instance.clear();
        TickUtil.tickRun(()->{
            NativeImage nativeImage = ScreenshotRecorder.takeScreenshot(MinecraftClient.getInstance().getFramebuffer());
            OCRHud.instance.clearInfo();
            TickUtil.runAfterTick(() -> OCRHud.instance.clearInfo(), 5.0f);
            ChatTranslate.threadFactory.newThread(()->{
                List<AnimationOCRResult> ocr = Config.getOCR().OCR(nativeImage);
                OCRHud.instance.setCenter();
                OCRHud.instance.setContent(ocr);
                OCRHud.instance.clearInfo();
            }).start();
            minecraft.options.hudHidden = false;
        });
    }
}

