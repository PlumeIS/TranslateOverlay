package xyz.plumc.chattranslate.translates.ocr;

import org.jetbrains.annotations.Nullable;
import xyz.plumc.chattranslate.components.animation.Animation;

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
