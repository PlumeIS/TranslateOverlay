package cn.plumc.translateoverlay.translates;

import cn.plumc.translateoverlay.config.Config;
import cn.plumc.translateoverlay.translate.translator.Translator;
import cn.plumc.translateoverlay.utils.RenderUtil;
import cn.plumc.translateoverlay.utils.TranslateUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ItemTranslator {
    private static ItemStack item = null;
    private static List<Text> translated = null;
    private static long submittedStamp = 0;
    private static final long COOLDOWN_TIME = 300;

    private static final ExecutorService executor = Executors.newFixedThreadPool(5);

    public static void translate(ItemStack itemStack, MatrixStack matrices, List<Text> texts, int x, int y, HandledScreen screen) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int originalWidth = 0;
        int originalHeight = 0;

        for (Text text : texts) {
            originalWidth = Math.max(originalWidth, textRenderer.getWidth(text));
            originalHeight += textRenderer.fontHeight;
        }

        if ((item == null || !item.equals(itemStack)) && System.currentTimeMillis() - submittedStamp > COOLDOWN_TIME){
            item = itemStack;
            translated = null;

            executor.submit(()->{
                submittedStamp = System.currentTimeMillis();
                List<Text> copied = new ArrayList<>(texts);
                for (int i = 0; i < copied.size(); i++){
                    Text text = texts.get(i);
                    Translator translator = Config.getTranslator();
                    copied.set(i, TranslateUtil.translateText(translator, text));
                }
                translated = copied;
            });

        } else if (translated != null){
            int translatedWidth = 0;
            int translatedHeight = 0;
            for (Text text : texts) {
                translatedWidth = Math.max(translatedWidth, textRenderer.getWidth(text));
                translatedHeight += textRenderer.fontHeight;
            }
            if (x + originalWidth + translatedWidth + 10 >= RenderUtil.getScaledWidth()){
                if (y - originalHeight - translatedHeight - 10 <= 0){
                    screen.renderTooltip(matrices, translated, x, y + originalHeight + 10);
                } else {
                    screen.renderTooltip(matrices, translated, x, y - translatedHeight - 10);
                }
            } else {
                screen.renderTooltip(matrices, translated, x + originalWidth + 10, y);
            }
        }
    }

    public static void clear(){
        item = null;
        translated = null;
        submittedStamp = 0;
    }
}
