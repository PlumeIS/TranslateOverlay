package cn.plumc.translateoverlay.translates;

import cn.plumc.translateoverlay.components.animation.Animation;
import cn.plumc.translateoverlay.components.animation.animas.LinearFunction;
import cn.plumc.translateoverlay.components.animation.animas.NoneFunction;
import cn.plumc.translateoverlay.config.Config;
import cn.plumc.translateoverlay.translate.translator.Translator;
import cn.plumc.translateoverlay.utils.RenderUtil;
import cn.plumc.translateoverlay.utils.TranslateUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TitleTranslator {
    private static Text title;
    private static Text subtitle;
    private static Animation titleAnimation = new Animation(0, 1, new NoneFunction(), 1);
    private static Animation titleKeepAnimation = null;
    private static Animation titleEndAnimation = null;

    private static Text overlayMessage;
    private static Animation overlayMessageAnimation = new Animation(0, 1, new NoneFunction(), 1);
    private static Animation overlayMessageKeepAnimation = null;
    private static Animation overlayMessageEndAnimation = null;

    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    public static void translateTitle(Text title){
        executor.submit(() -> {
            Translator translator = Config.getTranslator();
            TitleTranslator.title = TranslateUtil.translateText(translator, title);
            titleAnimation = new Animation(0, 1, new LinearFunction(), Animation.getTime(0.2F));
            titleKeepAnimation = null;
            titleEndAnimation = null;
        });
    }

    public static void translateSubtitle(Text subtitle){
        executor.submit(() -> {
            Translator translator = Config.getTranslator();
            TitleTranslator.subtitle = TranslateUtil.translateText(translator, subtitle);
        });
    }

    public static void translateOverlayMessage(Text overlayMessage){
        executor.submit(() -> {
            Translator translator = Config.getTranslator();
            TitleTranslator.overlayMessage = TranslateUtil.translateText(translator, overlayMessage);
            overlayMessageAnimation = new Animation(0, 1, new LinearFunction(), Animation.getTime(0.2F));
            overlayMessageKeepAnimation = null;
            overlayMessageEndAnimation = null;
        });
    }

    public static void render(DrawContext context, float tickDelta) {
        renderTitle(context);
        renderActionbar(context);
    }

    private static void renderActionbar(DrawContext context){
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (overlayMessage != null) {
            context.getMatrices().push();
            context.getMatrices().translate((float)(context.getScaledWindowWidth() / 2), (float)(context.getScaledWindowHeight() - 68) + 10, 0.0F);
            int alpha = RenderUtil.withAlpha((int) (overlayMessageAnimation.step() * 255), -1);
            if (overlayMessageAnimation.isEnd()){
                if (overlayMessageKeepAnimation == null){
                    overlayMessageKeepAnimation = new Animation(0, 1, new LinearFunction(), Animation.getTime(3F));
                }
                overlayMessageKeepAnimation.step();
                if (overlayMessageKeepAnimation.isEnd()){
                    if (overlayMessageEndAnimation == null){
                        overlayMessageEndAnimation = new Animation(1, 0, new LinearFunction(), Animation.getTime(0.2F));
                    }
                    alpha = RenderUtil.withAlpha((int) (Math.min(overlayMessageEndAnimation.step(), 1) * 255), -1);
                    if (overlayMessageEndAnimation.isEnd()){
                        overlayMessage = null;
                        overlayMessageKeepAnimation = null;
                        overlayMessageEndAnimation = null;
                        return;
                    }
                }
            }
            int width = textRenderer.getWidth(overlayMessage);
            RenderUtil.drawTextWithBackground(context, textRenderer, overlayMessage, -width / 2, -4, width, alpha);
            context.getMatrices().pop();
        }
    }


    private static void renderTitle(DrawContext context){
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (title != null) {
            context.getMatrices().push();
            context.getMatrices().translate((float) (context.getScaledWindowWidth() / 2), (float) (context.getScaledWindowHeight() / 2) + 12, 0.0F);
            context.getMatrices().push();
            context.getMatrices().scale(2.0F, 2.0F, 2.0F);
            int width = textRenderer.getWidth(title);
            int alpha = RenderUtil.withAlpha((int) (titleAnimation.step() * 255), -1);
            if (titleAnimation.isEnd()){
                if (titleKeepAnimation == null){
                    titleKeepAnimation = new Animation(0, 1, new LinearFunction(), Animation.getTime(5F));
                }
                titleKeepAnimation.step();
                if (titleKeepAnimation.isEnd()){
                    if (titleEndAnimation == null){
                        titleEndAnimation = new Animation(1, 0, new LinearFunction(), Animation.getTime(0.2F));
                    }
                    alpha = RenderUtil.withAlpha((int) (Math.min(titleEndAnimation.step(), 1) * 255), -1);
                    if (titleEndAnimation.isEnd()){
                        title = null;
                        titleKeepAnimation = null;
                        titleEndAnimation = null;
                        return;
                    }
                }
            }
            RenderUtil.drawTextWithBackground(context, textRenderer, title, -width / 2, -10, width, alpha);
            context.getMatrices().pop();
            if (subtitle != null) {
                context.getMatrices().push();
                context.getMatrices().scale(1.2F, 1.2F, 1.2F);
                int l = textRenderer.getWidth(subtitle);
                RenderUtil.drawTextWithBackground(context, textRenderer, subtitle, -l / 2, 13, l, alpha);
                context.getMatrices().pop();
            }
            context.getMatrices().pop();

        }
    }

    public static void clear(){
        title = null;
        subtitle = null;
        titleAnimation = new Animation(0, 1, new NoneFunction(), 1);
        titleKeepAnimation = null;
        titleEndAnimation = null;
        overlayMessage = null;
        overlayMessageAnimation = new Animation(0, 1, new NoneFunction(), 1);
        overlayMessageKeepAnimation = null;
        overlayMessageEndAnimation = null;
    }
}
