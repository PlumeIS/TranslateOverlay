package cn.plumc.translateoverlay.translates.ocr;

import cn.plumc.translateoverlay.components.animation.Animation;
import org.jetbrains.annotations.Nullable;

public class AnimationOCRResult {
    public Animation start;
    public Animation end;
    public Animation focus;
    public final OCRResult ocrResult;

    public AnimationOCRResult(@Nullable Animation start, @Nullable Animation end, OCRResult ocrResult){
        this.start = start;
        this.end = end;
        this.ocrResult = ocrResult;
    }
}
