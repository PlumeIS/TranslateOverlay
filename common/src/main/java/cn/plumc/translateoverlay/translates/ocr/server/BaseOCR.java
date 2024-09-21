package cn.plumc.translateoverlay.translates.ocr.server;

import cn.plumc.translateoverlay.translate.Language;
import cn.plumc.translateoverlay.translates.ocr.AnimationOCRResult;
import net.minecraft.client.texture.NativeImage;

import java.util.List;

public interface BaseOCR {
    List<AnimationOCRResult> OCR(NativeImage screenshot);
    void init(Language language);
}
