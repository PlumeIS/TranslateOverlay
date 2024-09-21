package cn.plumc.translateoverlay.components;

import cn.plumc.translateoverlay.components.animation.Animation;
import cn.plumc.translateoverlay.components.animation.animas.SemFunction;
import cn.plumc.translateoverlay.translates.chat.ChatStatus;
import cn.plumc.translateoverlay.translates.chat.TranslatedGuiMessage;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Nullables;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public class TranslatedChatComponent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text DELETED_CHAT_MESSAGE = Text.translatable("chat.deleted_marker").formatted(Formatting.GRAY, Formatting.ITALIC);
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
    private final List<DelayedMessageDeletion> messageDeletionQueue = new ArrayList<DelayedMessageDeletion>();

    public TranslatedChatComponent(MinecraftClient minecraft) {
        this.minecraft = minecraft;
    }

    public void tick() {
        if (!this.messageDeletionQueue.isEmpty()) {
            this.processMessageDeletionQueue();
        }
    }

    public void render(DrawContext guiGraphics, int tickCount, int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        guiHeight = guiGraphics.getScaledWindowHeight();
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
        int height = guiGraphics.getScaledWindowHeight();
        guiGraphics.getMatrices().push();
        guiGraphics.getMatrices().scale(scale, scale, 1.0f);
        guiGraphics.getMatrices().translate(4.0f, -minecraft.inGameHud.getChatHud().getHeight(), 0.0f);
        int flooredHeight = MathHelper.floor((float)(height - 40) / scale);
        int endIndexAt = this.getMessageEndIndexAt(this.screenToChatX(mouseX), this.screenToChatY(mouseY));
        double opacity = this.minecraft.options.getChatOpacity().getValue() * (double)0.9f + (double)0.1f;
        double baseBackgroundOpacity = this.minecraft.options.getTextBackgroundOpacity().getValue();
        double lineSpacing = this.minecraft.options.getChatLineSpacing().getValue();
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
            guiGraphics.getMatrices().push();
            guiGraphics.getMatrices().translate(0.0f, 0.0f, 50.0f);
            int animaX = !isInChat ? (int) line.animationW().step() : 0;
            if (hoverUUID == line.uuid()){
                guiGraphics.fill(-4 + animaX, lineX - lineHeight - animationHeight, width + 4 + 4 + animaX, lineX - animationHeight, HOVER_COLOR | lineBackgroundOpacity << 24);
            } else if (messageIndex < lastMessageLines){
                guiGraphics.fill(-4 + animaX, lineX - lineHeight - animationHeight, width + 4 + 4 + animaX, lineX - animationHeight, INDICATOR_COLOR | ((int)(lineTextOpacity * 0.3)) << 24);
            }
            else {
                guiGraphics.fill(-4 + animaX, lineX - lineHeight - animationHeight, width + 4 + 4 + animaX, lineX - animationHeight, lineBackgroundOpacity << 24);
            }

            int indicatorColor = INDICATOR_COLOR | lineTextOpacity << 24;
            guiGraphics.fill(-4 + animaX, lineX - lineHeight - animationHeight, -2 + animaX, lineX - animationHeight, indicatorColor);

            guiGraphics.getMatrices().translate(0.0f, 0.0f, 50.0f);
            guiGraphics.drawTextWithShadow(this.minecraft.textRenderer, line.content(), animaX, lineEndX - animationHeight, 0xFFFFFF + (lineTextOpacity << 24));
            guiGraphics.getMatrices().pop();
        }

        if (counter >= 1 || isInChat){
            int resultAppearance = tickCount - trimmedMessages.get(0).addedTime();
            double resultOpacity = isInChat ? 1.0 : this.getTimeFactor(resultAppearance);
            int resultLineOpacity = (int)(255.0 * resultOpacity * opacity);
            if (!(resultLineOpacity <= 3) || isInChat){
                guiGraphics.getMatrices().push();
                int endHeight = counter * getLineHeight();
                guiGraphics.getMatrices().translate(0, -2, 50.0f);
                lineX = flooredHeight - counter * lineHeight;
                guiGraphics.drawTextWithShadow(this.minecraft.textRenderer, Text.translatable("message.translateoverlay.result_notice"), 0, lineX+lineSpaced-animationHeight, 0xFFFFFF + (resultLineOpacity << 24));
                guiGraphics.getMatrices().pop();
            }
        }

        long listenerSize = this.minecraft.getMessageHandler().getUnprocessedMessageCount();
        if (listenerSize > 0L) {
            int chatOpacity = (int)(128.0 * opacity);
            appearance = (int)(255.0 * baseBackgroundOpacity);
            guiGraphics.getMatrices().push();
            guiGraphics.getMatrices().translate(0.0f, flooredHeight, 0.0f);
            guiGraphics.fill(-2, 0, ceiledWidth + 4, 9, appearance << 24);
            guiGraphics.getMatrices().translate(0.0f, 0.0f, 0.0f);
            guiGraphics.drawTextWithShadow(this.minecraft.textRenderer, Text.translatable("chat.queue", listenerSize), 0, 1, 0xFFFFFF + (chatOpacity << 24));
            guiGraphics.getMatrices().pop();
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
                guiGraphics.fill(lineX, -af, lineX + 2, -af - lineTextOpacity, 100, boxColor + (lineBackgroundOpacity << 24));
                guiGraphics.fill(lineX + 2, -af, lineX + 1, -af - lineTextOpacity, 100, 0xCCCCCC + (lineBackgroundOpacity << 24));
            }
        }
        guiGraphics.getMatrices().pop();
    }

    private boolean isChatHidden() {
        return this.minecraft.options.getChatVisibility().getValue() == ChatVisibility.HIDDEN;
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
        this.addMessage(chatComponent, null, this.minecraft.isConnectedToLocalServer() ? MessageIndicator.singlePlayer() : MessageIndicator.system(), uuid);
    }

    public void addMessage(Text chatComponent, @Nullable MessageSignatureData headerSignature, @Nullable MessageIndicator tag, UUID uuid) {
        this.logChatMessage(chatComponent, tag);
        this.addMessage(chatComponent, headerSignature, this.minecraft.inGameHud.getTicks(), tag, false, uuid);
    }

    private void logChatMessage(Text chatComponent, @Nullable MessageIndicator tag) {
        String string = chatComponent.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
        String string2 = Nullables.map(tag, MessageIndicator::loggedName);
        if (string2 != null) {
            LOGGER.info("[{}] [CHAT] {}", (Object)string2, (Object)string);
        } else {
            LOGGER.info("[CHAT] {}", (Object)string);
        }
    }

    public void addMessage(Text chatComponent, @Nullable MessageSignatureData headerSignature, int addedTime, @Nullable MessageIndicator tag, boolean onlyTrim, UUID uuid) {
        int i = MathHelper.floor((double)this.getWidth() / this.getScale());
        if (tag != null && tag.icon() != null) {
            i -= tag.icon().width + 4 + 2;
        }
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
            this.trimmedMessages.add(0, new TranslatedGuiMessage.AnimaLine(addedTime, formattedCharSequence, tag, bl2, new Animation(-MathHelper.ceil((float)this.getWidth() / (float)this.getScale()), 0, new SemFunction(), Animation.getTime(1.5f)), new Animation(-getLineHeight(), 0, new SemFunction(), Animation.getTime(0.75f)), uuid));
            lastMessageLines++;
        }
        ChatStatus.lastMessageUUID = uuid;
        while (this.trimmedMessages.size() > 1000) {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }
        if (!onlyTrim) {
            this.allMessages.add(0, new TranslatedGuiMessage(addedTime, chatComponent, headerSignature, tag, uuid));
            while (this.allMessages.size() > 1000) {
                this.allMessages.remove(this.allMessages.size() - 1);
            }
        }
    }

    private void processMessageDeletionQueue() {
        int i = this.minecraft.inGameHud.getTicks();
        this.messageDeletionQueue.removeIf(delayedMessageDeletion -> {
            if (i >= delayedMessageDeletion.deletableAfter()) {
                return this.deleteMessageOrDelay(delayedMessageDeletion.signature()) == null;
            }
            return false;
        });
    }

    @Nullable
    private DelayedMessageDeletion deleteMessageOrDelay(MessageSignatureData messageSignature) {
        int i = this.minecraft.inGameHud.getTicks();
        ListIterator<TranslatedGuiMessage> listIterator = this.allMessages.listIterator();
        while (listIterator.hasNext()) {
            TranslatedGuiMessage TranslatedGuiMessage = listIterator.next();
            if (!messageSignature.equals(TranslatedGuiMessage.signature())) continue;
            int j = TranslatedGuiMessage.addedTime() + 60;
            if (i >= j) {
                listIterator.set(this.createDeletedMarker(TranslatedGuiMessage));
                this.refreshTrimmedMessage();
                return null;
            }
            return new DelayedMessageDeletion(messageSignature, j);
        }
        return null;
    }

    private TranslatedGuiMessage createDeletedMarker(TranslatedGuiMessage message) {
        return new TranslatedGuiMessage(message.addedTime(), DELETED_CHAT_MESSAGE, null, MessageIndicator.system(), message.uuid());
    }

    public void rescaleChat() {
        this.resetChatScroll();
        this.refreshTrimmedMessage();
    }

    public void clearMessages(boolean clearSentMsgHistory) {
        this.minecraft.getMessageHandler().processAll();
        this.messageDeletionQueue.clear();
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
            this.addMessage(TranslatedGuiMessage.content(), TranslatedGuiMessage.signature(), TranslatedGuiMessage.addedTime(), TranslatedGuiMessage.tag(), true, TranslatedGuiMessage.uuid());
        }
    }

    /**
     * Resets the chat scroll (executed when the GUI is closed, among others)
     */
    public void resetChatScroll() {
        this.chatScrollbarPos = 0;
        this.newMessageSinceScroll = false;
    }

    public void scrollChat(int posInc) {
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
        double d = (double)this.minecraft.getWindow().getScaledHeight()/ - y - 40.0 - minecraft.inGameHud.getChatHud().getHeight();
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
        return ChatHud.getWidth(this.minecraft.options.getChatWidth().getValue());
    }

    public int getHeight() {
        return ChatHud.getHeight(this.isChatFocused() ? this.minecraft.options.getChatHeightFocused().getValue() : this.minecraft.options.getChatHeightUnfocused().getValue());
    }

    public double getScale() {
        return this.minecraft.options.getChatScale().getValue();
    }

    public int getLinesPerPage() {
        return this.getHeight() / this.getLineHeight();
    }

    private int getLineHeight() {
        return (int)((double)this.minecraft.textRenderer.fontHeight * (this.minecraft.options.getChatLineSpacing().getValue() + 1.0));
    }

    @Environment(value= EnvType.CLIENT)
    record DelayedMessageDeletion(MessageSignatureData signature, int deletableAfter) {
    }

    public boolean inMouse(){
        int x = 0;
        int y = MathHelper.floor((float) (guiHeight - (getHeight() + 40)) / (float) getScale());
        return x <= mouseX && mouseX <= MathHelper.ceil((float)this.getWidth() / (float)getScale())+4+4 && y-getHeight() <= mouseY && mouseY <= y;
    }
}