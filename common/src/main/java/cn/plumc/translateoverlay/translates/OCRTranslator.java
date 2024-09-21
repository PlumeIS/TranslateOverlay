package cn.plumc.translateoverlay.translates;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import cn.plumc.translateoverlay.TranslateOverlay;
import cn.plumc.translateoverlay.config.Config;
import cn.plumc.translateoverlay.translates.ocr.AnimationOCRResult;
import cn.plumc.translateoverlay.translates.ocr.OCRHud;
import cn.plumc.translateoverlay.utils.TickUtil;

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
            TranslateOverlay.threadFactory.newThread(()->{
                List<AnimationOCRResult> ocr = Config.getOCR().OCR(nativeImage);
                OCRHud.instance.setCenter();
                OCRHud.instance.setContent(ocr);
                OCRHud.instance.clearInfo();
            }).start();
            minecraft.options.hudHidden = false;
        });
    }
}

