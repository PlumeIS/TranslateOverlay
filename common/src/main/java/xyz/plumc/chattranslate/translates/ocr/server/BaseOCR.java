package xyz.plumc.chattranslate.translates.ocr.server;

import net.minecraft.client.texture.NativeImage;
import xyz.plumc.chattranslate.translate.Language;
import xyz.plumc.chattranslate.translates.ocr.AnimationOCRResult;

import java.util.List;

public interface BaseOCR {
    List<AnimationOCRResult> OCR(NativeImage screenshot);
    void init(Language language);
}
