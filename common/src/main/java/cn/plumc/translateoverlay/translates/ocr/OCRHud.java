package cn.plumc.translateoverlay.translates.ocr;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import cn.plumc.translateoverlay.components.animation.Animation;
import cn.plumc.translateoverlay.components.animation.animas.LinearFunction;

import java.util.ArrayList;
import java.util.List;

public class OCRHud {
    public static OCRHud instance = new OCRHud();
    public static final int BACKGROUND_COLOR = ((int)(255 * 0.8));
    public static final int BOX_COLOR = 0xFFFFFF;
    public static final int TEXT_COLOR = 0xFFFFFF;
    private float centerX;
    private float centerY;
    private Vec3d centerPosition;
    private List<AnimationOCRResult> results = new ArrayList<>();

    private String lastInfo;
    private String info;
    private Animation infoStartAnimation;
    private Animation infoEndAnimation;

    public void render(MatrixStack matrixStack, Integer mouseX, Integer mouseY, float deltaTime) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (lastInfo!=null) {
            if (infoEndAnimation.step>=infoEndAnimation.end-1){
                lastInfo = null;
                infoEndAnimation = null;
            } else {
                float opacity = infoEndAnimation.step();
                DrawableHelper.drawTextWithShadow(matrixStack, minecraft.textRenderer, Text.of(lastInfo), minecraft.getWindow().getScaledWidth() - minecraft.textRenderer.getWidth(lastInfo) - minecraft.textRenderer.fontHeight/2, minecraft.textRenderer.fontHeight / 2, (TEXT_COLOR | (int)(255 * opacity) << 24));
            }
        }
        if (info!=null) {
            float opacity = infoStartAnimation.step();
            DrawableHelper.drawTextWithShadow(matrixStack, minecraft.textRenderer, Text.of(info), minecraft.getWindow().getScaledWidth()  - minecraft.textRenderer.getWidth(info) - minecraft.textRenderer.fontHeight/2, minecraft.textRenderer.fontHeight/2, (TEXT_COLOR | (int)(255 * opacity) << 24));
        }

        if (results.isEmpty()) return;
        if (minecraft.player!=null&&movedCheck()){
            clearWithFadeOut();
        }

        boolean mouseHovered = false;
        int hoveredIndex = -1;
        for (int i = 0; i < results.size(); i++){
            AnimationOCRResult result = results.get(i);
            OCRResult ocr = result.ocrResult;
            int x = (int) (ocr.xRate() * minecraft.getWindow().getScaledWidth());
            int y = (int) (ocr.yRate() * minecraft.getWindow().getScaledHeight());
            int w = (int) (ocr.wRate() * minecraft.getWindow().getScaledWidth());
            int h = (int) (ocr.hRate() * minecraft.getWindow().getScaledHeight());
            if (mouseX != null && x - 1 <= mouseX && mouseX <= Math.max(x + w + 1, x + minecraft.textRenderer.getWidth(ocr.result())) && y - h - minecraft.textRenderer.fontHeight - 2 <= mouseY && mouseY <= y + 1) {
                mouseHovered = true;
                hoveredIndex = i;
                break;
            }
        }
        for (int i = 0; i < results.size(); i++){
            AnimationOCRResult result = results.get(i);
            float opacity;
            if (mouseHovered){
                if (i != hoveredIndex){
                    if (result.focus==null) result.focus = new Animation(1.0f, 0.3f, new LinearFunction(), new Animation.Time(0.85f, deltaTime));
                    System.out.println(result.focus.step());
                    opacity = result.start.step() * result.focus.step();
                } else opacity = result.start.step();
            } else {
                result.focus = null;
                opacity = result.start.step();
            }

            if (result.end != null) opacity = result.end.step();
            OCRResult ocr = result.ocrResult;
            int x = (int) (ocr.xRate() * minecraft.getWindow().getScaledWidth());
            int y = (int) (ocr.yRate() * minecraft.getWindow().getScaledHeight());
            int w = (int) (ocr.wRate() * minecraft.getWindow().getScaledWidth());
            int h = (int) (ocr.hRate() * minecraft.getWindow().getScaledHeight());
            renderResult(matrixStack, result, x, y, w, h, 1, opacity);
        }
        clearCheck();
    }
    private void renderResult(MatrixStack matrixStack, AnimationOCRResult result, int x, int y, int w, int h, int s, float opacity) {
        OCRResult ocr = result.ocrResult;
        renderOutline(matrixStack, x, y, w, h, s, (BOX_COLOR | (int) (255 * opacity) << 24));
        renderText(matrixStack, ocr.result(), x, y, s, (TEXT_COLOR | (int) (255 * opacity) << 24));
        renderTextBox(matrixStack, ocr.result(), x, y, s, (int) (BACKGROUND_COLOR * opacity) << 24);
        renderTextOutline(matrixStack, ocr.result(), x, y, s, (BOX_COLOR | (int) (255 * opacity) << 24));
    }

    private void renderOutline(MatrixStack matrixStack, int x, int y, int w, int h, int s, int color){
        DrawableHelper.fill(matrixStack, x-s, y-s, x+w+s, y, color);
        DrawableHelper.fill(matrixStack, x-s, y+h, x+w+s, y+h+s, color);
        DrawableHelper.fill(matrixStack, x-s, y, x, y+h, color);
        DrawableHelper.fill(matrixStack, x+w, y, x+w+s, y+h, color);
    }
    private void renderText(MatrixStack matrixStack, String text, int x, int y, int s, int color){
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        DrawableHelper.drawTextWithShadow(matrixStack, font, Text.of(text), x+2, y-s-font.fontHeight-2, color);
    }
    private void renderTextBox(MatrixStack matrixStack, String text, int x, int y, int s, int color){
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        DrawableHelper.fill(matrixStack, x, y-s-font.fontHeight-2-2, x+font.getWidth(text)+2+2, y-s, color);
    }

    private void renderTextOutline(MatrixStack matrixStack, String text, int x, int y, int s, int color){
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        int font_with = font.getWidth(text);
        DrawableHelper.fill(matrixStack, x-s, y-s-font.fontHeight-2-2-s, x+font_with+s+2+2,y-s-font.fontHeight-2-2, color);
        DrawableHelper.fill(matrixStack, x-s, y, x+font_with+s+2+2,y-s, color);
        DrawableHelper.fill(matrixStack, x-s,y-s-font.fontHeight-2-2, x, y-s, color);
        DrawableHelper.fill(matrixStack, x+font_with+2+2, y-font.fontHeight-2-2-s, x+font_with+2+2+s, y-s, color);
    }

    public void setCenter(){
        MinecraftClient minecraft = MinecraftClient.getInstance();
        centerX = minecraft.player!=null ? minecraft.player.getPitch() : 0;
        centerY = minecraft.player!=null ? minecraft.player.getYaw() : 0;
        centerPosition = minecraft.player!=null ? minecraft.player.getEyePos() : new Vec3d(0, 0, 0);
    }

    public void setInfo(String info){
        this.lastInfo = this.info;
        this.info = info;
        this.infoStartAnimation = new Animation(0, 1, new LinearFunction(), Animation.getTime(0.2f));
        this.infoEndAnimation = new Animation(1, 0, new LinearFunction(), Animation.getTime(0.3f));
    }

    public void clearInfo(){
        this.lastInfo = this.info;
        this.info = null;
        this.infoEndAnimation = new Animation(1, 0, new LinearFunction(), Animation.getTime(0.2f));
    }

    public boolean movedCheck(){
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.currentScreen != null) return false;
        float xRot = minecraft.player.getPitch();
        float yRot = minecraft.player.getYaw();

        Vec3d position = minecraft.player.getEyePos();
        return (Math.abs(xRot - centerX)>10||
                Math.abs(yRot - centerY)>10||
                centerPosition.distanceTo(position)>0.1f)&&!minecraft.player.isSneaking();
    }

    public void setContent(List<AnimationOCRResult> results){
        this.results = results;
    }
    public void clear(){
        this.results = new ArrayList<>();
    }
    public void clearWithFadeOut(){
        for (AnimationOCRResult result: results){
            if (result.end==null) result.end = new Animation(1, 0, new LinearFunction(), Animation.getTime(0.35f));
        }
    }

    public void clearCheck(){
        for (AnimationOCRResult result : ImmutableList.copyOf(results)){
            if (result.end != null && result.end.current >= result.end.step-1) results.remove(result);
        }
    }
}