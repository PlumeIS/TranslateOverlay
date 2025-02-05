package cn.plumc.translateoverlay.translates;

import cn.plumc.translateoverlay.config.Config;
import cn.plumc.translateoverlay.translate.translator.Translator;
import cn.plumc.translateoverlay.utils.TranslateUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.minecraft.client.gui.hud.InGameHud.SCOREBOARD_ENTRY_COMPARATOR;

public class ScoreboardTranslator {
    @Environment(EnvType.CLIENT)
    record SidebarEntry(Text name, Text score, int scoreWidth) {}
    @Environment(EnvType.CLIENT)
    record TranslatedSidebarEntry(Text name){}

    private static List<SidebarEntry> original = new ArrayList<>();
    private static Text title = null;
    private static Text translatedTitle = null;
    private static List<TranslatedSidebarEntry> translated = null;

    private static final ExecutorService executor = Executors.newFixedThreadPool(5);

    public static void translate(DrawContext context, ScoreboardObjective objective){
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Scoreboard scoreboard = objective.getScoreboard();
        Text title = objective.getDisplayName();
        List<SidebarEntry> entries = new ArrayList<>();
        NumberFormat numberFormat = objective.getNumberFormatOr(StyledNumberFormat.RED);
        scoreboard.getScoreboardEntries(objective).stream()
                .filter((score) -> !score.hidden())
                .sorted(SCOREBOARD_ENTRY_COMPARATOR)
                .limit(15L)
                .forEach((entry) -> {
                    Team team = scoreboard.getScoreHolderTeam(entry.owner());
                    Text rawName = entry.name();
                    Text name = Team.decorateName(team, rawName);
                    Text point = entry.formatted(numberFormat);
                    int pointWidth = textRenderer.getWidth(point);
                    entries.add(new SidebarEntry(name, point, pointWidth));
                });

        if (!title.equals(ScoreboardTranslator.title)){
            executor.submit(()->{
                Translator translator = Config.getTranslator();
                translatedTitle = TranslateUtil.translateText(translator, title);;
            });
            ScoreboardTranslator.title = title;
        }

        if (!entries.equals(original)){
            executor.submit(()->{
                List<TranslatedSidebarEntry> translatedEntries = new ArrayList<>();
                for (SidebarEntry entry : original){
                    Translator translator = Config.getTranslator();
                    translatedEntries.add(new TranslatedSidebarEntry(TranslateUtil.translateText(translator, entry.name())));
                }
                translated = translatedEntries;
            });
            ScoreboardTranslator.original = entries;
        }

        if (translatedTitle != null && translated != null && !translated.isEmpty()){
            renderScoreboardSidebar(context);
        }
    }

    private static void renderScoreboardSidebar(DrawContext context) {
        // 计算最大宽度
        int originalWidth = calculateMaxWidth(title, original.toArray(new SidebarEntry[0]));
        int maxWidth = calculateTranslatedWidth(translatedTitle, translated);
        // 渲染计分板
        renderSidebar(context, translated, translatedTitle, maxWidth, originalWidth);
    }

    private static int calculateMaxWidth(Text displayName, SidebarEntry[] sidebarEntries) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int maxWidth = textRenderer.getWidth(displayName);
        int colonWidth = textRenderer.getWidth(": ");

        for (SidebarEntry entry : sidebarEntries) {
            int entryWidth = textRenderer.getWidth(entry.name()) + (entry.scoreWidth() > 0 ? colonWidth + entry.scoreWidth() : 0);
            maxWidth = Math.max(maxWidth, entryWidth);
        }

        return maxWidth;
    }

    private static int calculateTranslatedWidth(Text displayName, List<TranslatedSidebarEntry> translated) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int maxWidth = textRenderer.getWidth(displayName);
        int colonWidth = textRenderer.getWidth(": ");

        for (TranslatedSidebarEntry entry : translated) {
            int entryWidth = textRenderer.getWidth(entry.name());
            maxWidth = Math.max(maxWidth, entryWidth);
        }

        return maxWidth;
    }

    private static void renderSidebar(DrawContext context, List<TranslatedSidebarEntry> translated, Text displayName, int maxWidth, int originalWidth) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.draw(() -> {
            int entryCount = translated.size();
            int totalHeight = entryCount * 9;
            int centerY = context.getScaledWindowHeight() / 2 + totalHeight / 3;
            int originalBoxWidth = originalWidth + 4;
            int startX = context.getScaledWindowWidth() - maxWidth - 3 - originalBoxWidth;
            int endX = context.getScaledWindowWidth() - 1 - originalBoxWidth;
            int backgroundColor1 = MinecraftClient.getInstance().options.getTextBackgroundColor(0.3F);
            int backgroundColor2 = MinecraftClient.getInstance().options.getTextBackgroundColor(0.4F);
            int startY = centerY - totalHeight;

            // 绘制背景
            context.fill(startX - 2, startY - 10, endX, startY, 0x33ff99 | backgroundColor2);
            context.fill(startX - 2, startY, endX, centerY, 0x33ff99 | backgroundColor1);

            // 绘制标题
            int titleX = startX + (maxWidth - textRenderer.getWidth(displayName)) / 2;
            context.drawText(textRenderer, displayName, titleX, startY - 9, -1, false);

            // 绘制条目
            for (int i = 0; i < entryCount; i++) {
                TranslatedSidebarEntry entry = translated.get(i);
                int entryY = centerY - (entryCount - i) * 9;
                context.drawText(textRenderer, entry.name(), startX, entryY, -1, false);
            }
        });
    }

    public static void clear(){
        original = new ArrayList<>();
        title = null;
        translatedTitle = null;
        translated = null;
    }
}
