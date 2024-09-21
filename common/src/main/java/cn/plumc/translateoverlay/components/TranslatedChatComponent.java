package cn.plumc.translateoverlay.components;

import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.plumc.translateoverlay.TranslateOverlay;
import cn.plumc.translateoverlay.components.animation.Animation;
import cn.plumc.translateoverlay.components.animation.animas.SemFunction;
import cn.plumc.translateoverlay.translates.chat.ChatStatus;
import cn.plumc.translateoverlay.translates.chat.TranslatedGuiMessage;

import java.util.*;

public class TranslatedChatComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslateOverlay.MOD_ID);
    private final MinecraftClient minecraft;

    private int guiHeight;
    private int mouseX;
    private int mouseY;
    private int lastMessageLines;

    public int currentBoxWidth;
    public Animation currentWidthAnimation;

    private static final int INDICATOR_COLOR = 0x33ff99;
    public static final int HOVER_COLOR = 0x00bfbf;
    /**
     * A list of messages previously sent through the chat GUI
     */
    private final List<String> recentChat = Lists.newArrayList();
    /**
     * Chat lines to be displayed in the chat box
     */
    private final List<TranslatedGuiMessage> allMessages = Lists.newArrayList();
    /**
     * List of the ChatLines currently drawn
     */
    private final List<TranslatedGuiMessage.AnimaLine> trimmedMessages = Lists.newArrayList();
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;

    public TranslatedChatComponent(MinecraftClient minecraft) {
        this.minecraft = minecraft;
    }

    public void render(MatrixStack matrices, int tickCount, int mouseX, int mouseY) {
        matrices.pop();
        matrices.push();
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        guiHeight = minecraft.getWindow().getScaledHeight();
        int lineX;
        int boxColor;
        int lineBackgroundOpacity;
        int lineTextOpacity;
        int appearance;
        if (this.isChatHidden()) {
            return;
        }
        int linesPerPage = this.getLinesPerPage();
        int messageSize = this.trimmedMessages.size();
        if (messageSize <= 0) {
            return;
        }
        boolean isInChat = this.isChatFocused();
        float scale = (float)this.getScale();
        int ceiledWidth = MathHelper.ceil((float)this.getWidth() / scale);
        int height = minecraft.getWindow().getScaledHeight();;
        matrices.push();
        matrices.scale(scale, scale, 1.0f);
        matrices.translate(4.0f, -minecraft.inGameHud.getChatHud().getHeight(), 0.0f);
        int flooredHeight = MathHelper.floor((float)(height - 40) / scale);
        int endIndexAt = this.getMessageEndIndexAt(this.screenToChatX(mouseX), this.screenToChatY(mouseY));
        double opacity = this.minecraft.options.chatOpacity * (double)0.9f + (double)0.1f;
        double baseBackgroundOpacity = this.minecraft.options.textBackgroundOpacity;
        double lineSpacing = this.minecraft.options.chatLineSpacing;
        int lineHeight = this.getLineHeight();
        int lineSpaced = (int)Math.round(-8.0 * (lineSpacing + 1.0) + 4.0 * lineSpacing);
        int counter = 0;

        UUID hoverUUID = null;
        ChatStatus.hoverUUID = null;
        int hoverIndex = getMessageLineIndexAt(screenToChatX(mouseX), screenToChatY(mouseY));
        if (hoverIndex!=-1) {
            TranslatedGuiMessage.AnimaLine hoverLine = this.trimmedMessages.get(hoverIndex);
            hoverUUID = hoverLine.uuid();
            ChatStatus.hoverUUID = hoverLine.uuid();
        }

        int boxWidth = 0;
        int animationHeight = 0;
        for (int i = 0; i + this.chatScrollbarPos < this.trimmedMessages.size() && i < linesPerPage; ++i) {
            int messageIndex = i + this.chatScrollbarPos;
            TranslatedGuiMessage.AnimaLine line = this.trimmedMessages.get(messageIndex);
            if (line == null || tickCount - line.addedTime() >= 200 && !isInChat) continue;
            if (!isInChat) animationHeight += line.animationH().step();
            if (!isInChat) boxWidth = Math.max(boxWidth, this.minecraft.textRenderer.getWidth(line.content()));
        }
        if (!isInChat){
            if (boxWidth != currentBoxWidth){
                currentWidthAnimation = new Animation(currentBoxWidth, boxWidth, new SemFunction(), Animation.getTime(0.75f));
                currentBoxWidth = boxWidth;
            }
        }

        for (int i = 0; i + this.chatScrollbarPos < this.trimmedMessages.size() && i < linesPerPage; ++i) {
            int width = currentWidthAnimation != null && !isInChat ? (int) currentWidthAnimation.step() : ceiledWidth;
            int messageIndex = i + this.chatScrollbarPos;
            TranslatedGuiMessage.AnimaLine line = this.trimmedMessages.get(messageIndex);
            if (line == null || (appearance = tickCount - line.addedTime()) >= 200 && !isInChat) continue;
            double lineOpacity = (isInChat ? 1.0 : this.getTimeFactor(appearance));
            lineTextOpacity = (int)(255.0 * lineOpacity * opacity);
            lineBackgroundOpacity = (int)(255.0 * lineOpacity * baseBackgroundOpacity * 0.6);
            ++counter;
            if (lineTextOpacity <= 3) continue;
            boxColor = 0;
            lineX = flooredHeight - i * lineHeight;
            int lineEndX = lineX + lineSpaced;
            matrices.push();
            matrices.translate(0.0f, 0.0f, 50.0f);
            int animaX = !isInChat ? (int) line.animationW().step() : 0;
            if (hoverUUID == line.uuid()){
                DrawableHelper.fill(matrices, -4 + animaX, lineX - lineHeight - animationHeight, width + 4 + 4 + animaX, lineX - animationHeight, HOVER_COLOR | lineBackgroundOpacity << 24);
            } else if (messageIndex < lastMessageLines){
                DrawableHelper.fill(matrices, -4 + animaX, lineX - lineHeight - animationHeight, width + 4 + 4 + animaX, lineX - animationHeight, INDICATOR_COLOR | ((int)(lineTextOpacity * 0.3)) << 24);
            }
            else {
                DrawableHelper.fill(matrices, -4 + animaX, lineX - lineHeight - animationHeight, width + 4 + 4 + animaX, lineX - animationHeight, lineBackgroundOpacity << 24);
            }

            int indicatorColor = INDICATOR_COLOR | lineTextOpacity << 24;
            DrawableHelper.fill(matrices, -4 + animaX, lineX - lineHeight - animationHeight, -2 + animaX, lineX - animationHeight, indicatorColor);

            matrices.translate(0.0f, 0.0f, 50.0f);
            DrawableHelper.drawWithShadow(matrices, this.minecraft.textRenderer, line.content(), animaX, lineEndX - animationHeight, 0xFFFFFF + (lineTextOpacity << 24));
            matrices.pop();
        }

        if (counter >= 1 || isInChat){
            int resultAppearance = tickCount - trimmedMessages.get(0).addedTime();
            double resultOpacity = isInChat ? 1.0 : this.getTimeFactor(resultAppearance);
            int resultLineOpacity = (int)(255.0 * resultOpacity * opacity);
            if (!(resultLineOpacity <= 3) || isInChat){
                matrices.push();
                int endHeight = counter * getLineHeight();
                matrices.translate(0, -2, 50.0f);
                lineX = flooredHeight - counter * lineHeight;
                DrawableHelper.drawTextWithShadow(matrices, this.minecraft.textRenderer, new TranslatableText("message.translateoverlay.result_notice"), 0, lineX+lineSpaced-animationHeight, 0xFFFFFF + (resultLineOpacity << 24));
                matrices.pop();
            }
        }

        if (isInChat) {
            int chatLineHeight = this.getLineHeight();
            appearance = messageSize * chatLineHeight;
            int perPageHeight = counter * chatLineHeight;
            int af = this.chatScrollbarPos * perPageHeight / messageSize - flooredHeight;
            lineTextOpacity = perPageHeight * perPageHeight / appearance;
            if (appearance != perPageHeight) {
                lineBackgroundOpacity = af > 0 ? 170 : 96;
                boxColor = this.newMessageSinceScroll ? 0xCC3333 : 0x3333AA;
                lineX = ceiledWidth + 4;
                DrawableHelper.fill(matrices, lineX, -af, lineX + 2, -af - lineTextOpacity, boxColor + (lineBackgroundOpacity << 24));
                DrawableHelper.fill(matrices, lineX + 2, -af, lineX + 1, -af - lineTextOpacity,  0xCCCCCC + (lineBackgroundOpacity << 24));
            }
        }
        matrices.pop();
    }

    private boolean isChatHidden() {
        return this.minecraft.options.chatVisibility == ChatVisibility.HIDDEN;
    }

    private static double getTimeFactor(int counter) {
        double d = (double)counter / 200.0;
        d = 1.0 - d;
        d *= 10.0;
        d = MathHelper.clamp(d, 0.0, 1.0);
        d *= d;
        return d;
    }

    public void addMessage(Text chatComponent, UUID uuid) {
        resetChatScroll();
        this.logChatMessage(chatComponent);
        this.addMessage(chatComponent, this.minecraft.inGameHud.getTicks(), false, uuid);
    }

    private void logChatMessage(Text chatComponent) {
        String string = chatComponent.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
        LOGGER.info("[CHAT] {}", (Object)string);
    }

    public void addMessage(Text chatComponent, int addedTime,boolean onlyTrim, UUID uuid) {
        int i = MathHelper.floor((double)this.getWidth() / this.getScale());
        List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(chatComponent, i, this.minecraft.textRenderer);
        boolean bl = this.isChatFocused();
        lastMessageLines = 0;
        for (int j = 0; j < list.size(); ++j) {
            OrderedText formattedCharSequence = list.get(j);
            if (bl && this.chatScrollbarPos > 0) {
                this.newMessageSinceScroll = true;
                this.scrollChat(1);
            }
            boolean bl2 = j == list.size() - 1;
            this.trimmedMessages.add(0, new TranslatedGuiMessage.AnimaLine(addedTime, formattedCharSequence, bl2, new Animation(-MathHelper.ceil((float)this.getWidth() / (float)this.getScale()), 0, new SemFunction(), Animation.getTime(1.5f)), new Animation(-getLineHeight(), 0, new SemFunction(), Animation.getTime(0.75f)), uuid));
            lastMessageLines++;
        }
        ChatStatus.lastMessageUUID = uuid;
        while (this.trimmedMessages.size() > 1000) {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }
        if (!onlyTrim) {
            this.allMessages.add(0, new TranslatedGuiMessage(addedTime, chatComponent, uuid));
            while (this.allMessages.size() > 1000) {
                this.allMessages.remove(this.allMessages.size() - 1);
            }
        }
    }

    public void rescaleChat() {
        this.resetChatScroll();
        this.refreshTrimmedMessage();
    }

    public void clearMessages(boolean clearSentMsgHistory) {
        this.trimmedMessages.clear();
        this.allMessages.clear();
        if (clearSentMsgHistory) {
            this.recentChat.clear();
        }
    }

    private void refreshTrimmedMessage() {
        this.trimmedMessages.clear();
        for (int i = this.allMessages.size() - 1; i >= 0; --i) {
            TranslatedGuiMessage TranslatedGuiMessage = this.allMessages.get(i);
            this.addMessage(TranslatedGuiMessage.content(), TranslatedGuiMessage.addedTime(), true, TranslatedGuiMessage.uuid());
        }
    }

    /**
     * Resets the chat scroll (executed when the GUI is closed, among others)
     */
    public void resetChatScroll() {
        this.chatScrollbarPos = 0;
        this.newMessageSinceScroll = false;
    }

    public void scrollChat(double posInc) {
        this.chatScrollbarPos += posInc;
        int i = this.trimmedMessages.size();
        if (this.chatScrollbarPos > i - this.getLinesPerPage()) {
            this.chatScrollbarPos = i - this.getLinesPerPage();
        }
        if (this.chatScrollbarPos <= 0) {
            this.chatScrollbarPos = 0;
            this.newMessageSinceScroll = false;
        }
    }

    private double screenToChatX(double x) {
        return x / this.getScale() - 4.0;
    }

    private double screenToChatY(double y) {
        double d = (double)this.minecraft.getWindow().getScaledHeight() - y - 40.0 - minecraft.inGameHud.getChatHud().getHeight();
        return d / (this.getScale() * (double)this.getLineHeight());
    }

    private int getMessageEndIndexAt(double mouseX, double mouseY) {
        int i = this.getMessageLineIndexAt(mouseX, mouseY);
        if (i == -1) {
            return -1;
        }
        while (i >= 0) {
            if (this.trimmedMessages.get(i).endOfEntry()) {
                return i;
            }
            --i;
        }
        return i;
    }

    private int getMessageLineIndexAt(double mouseX, double mouseY) {
        int j;
        if (!this.isChatFocused() || this.minecraft.options.hudHidden || this.isChatHidden()) {
            return -1;
        }
        if (mouseX < -4.0 || mouseX > (double)MathHelper.floor((double)this.getWidth() / this.getScale())) {
            return -1;
        }
        int i = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
        if (mouseY >= 0.0 && mouseY < (double)i && (j = MathHelper.floor(mouseY + (double)this.chatScrollbarPos)) >= 0 && j < this.trimmedMessages.size()) {
            return j;
        }
        return -1;
    }

    /**
     * Returns {@code true} if the chat GUI is open
     */
    private boolean isChatFocused() {
        return this.minecraft.currentScreen instanceof ChatScreen;
    }

    public int getWidth() {
        return ChatHud.getWidth(this.minecraft.options.chatWidth);
    }

    public int getHeight() {
        return ChatHud.getHeight(this.isChatFocused() ? this.minecraft.options.chatHeightFocused : this.minecraft.options.chatHeightUnfocused);
    }

    public double getScale() {
        return this.minecraft.options.chatScale;
    }

    public int getLinesPerPage() {
        return this.getHeight() / this.getLineHeight();
    }

    private int getLineHeight() {
        return (int)((double)this.minecraft.textRenderer.fontHeight * (this.minecraft.options.chatLineSpacing + 1.0));
    }

    public boolean inMouse(){
        int x = 0;
        int y = MathHelper.floor((float) (guiHeight - (getHeight() + 40)) / (float) getScale());
        return x <= mouseX && mouseX <= MathHelper.ceil((float)this.getWidth() / (float)getScale())+4+4 && y-getHeight() <= mouseY && mouseY <= y;
    }
}