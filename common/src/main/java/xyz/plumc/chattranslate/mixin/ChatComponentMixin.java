package xyz.plumc.chattranslate.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.plumc.chattranslate.accessor.UUIDAccessor;
import xyz.plumc.chattranslate.components.TranslatedChatComponent;
import xyz.plumc.chattranslate.translates.ChatTranslator;
import xyz.plumc.chattranslate.translates.chat.ChatStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static xyz.plumc.chattranslate.translates.ChatTranslator.checkStatus;

@Mixin(ChatHud.class)
public abstract class ChatComponentMixin {
    @Unique
    List<ChatHudLine<OrderedText>> currectLines = new ArrayList<>();

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;IIZ)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", shift = At.Shift.AFTER, ordinal = 1))
    private void onMessage(Text message, int messageId, int timestamp, boolean refresh, CallbackInfo ci){
        if (!checkStatus(message.getString())) return;
        ChatTranslator.translate(this.messages.get(0));
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(MatrixStack matrices, int tickDelta, CallbackInfo ci){
        if (ChatStatus.hoverUUID != null){
            if (ChatStatus.originalMessageScrollbarPos == -1) ChatStatus.originalMessageScrollbarPos = scrolledLines;
            for (int i = 0; i < visibleMessages.size(); i++){
                if (ChatStatus.hoverUUID == (UUIDAccessor.getUUID(visibleMessages.get(i)))){
                    this.scrolledLines += i - scrolledLines - 3;
                    int size = this.visibleMessages.size();
                    if (this.scrolledLines > size - this.getVisibleLineCount()) {
                        this.scrolledLines = size - this.getVisibleLineCount();
                    }
                    if (this.scrolledLines <= 0) {
                        this.scrolledLines = 0;
                    }
                    break;
                }
            }
        } else if (ChatStatus.originalMessageScrollbarPos != -1){
            scrolledLines = ChatStatus.originalMessageScrollbarPos;
            ChatStatus.originalMessageScrollbarPos = -1;
        }

        ChatStatus.renderIndex = this.scrolledLines;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V", ordinal = 0))
    private void hookRenderBackgroundStart(MatrixStack matrixStack, int minX, int minY, int maxX, int maxY, int color){
        UUID uuid = UUIDAccessor.getUUID(this.visibleMessages.get(ChatStatus.renderIndex));
        if (ChatStatus.hoverUUID != null && ChatStatus.hoverUUID == uuid){
            DrawableHelper.fill(matrixStack, minX, minY, maxX, maxY, TranslatedChatComponent.HOVER_COLOR | color);
        } else if (ChatStatus.lastMessageUUID != null && ChatStatus.lastMessageUUID == uuid){
                DrawableHelper.fill(matrixStack, minX, minY, maxX, maxY, 0x33ff99 | ((int)((color >> 24) * 0.5)) << 24);
        } else {
            DrawableHelper.fill(matrixStack, minX, minY, maxX, maxY, color);
        }
        ChatStatus.renderIndex++;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderEnd(MatrixStack matrices, int tickDelta, CallbackInfo ci){
        ChatStatus.renderIndex = -1;
        int mouseX = (int)(this.client.mouse.getX() * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth());
        int mouseY = (int)(this.client.mouse.getY() * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight());
        ChatTranslator.translatedChatComponent.render(matrices, tickDelta, mouseX, mouseY);
    }

    @Redirect(method = "addMessage(Lnet/minecraft/text/Text;IIZ)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", ordinal = 0))
    private void hookTrimmedMessagesAdd(List instance, int i, Object e) {
        ChatHudLine<OrderedText> line = (ChatHudLine<OrderedText>) e;
        currectLines.add(line);
        this.visibleMessages.add(i, line);
    }

    @Redirect(method = "addMessage(Lnet/minecraft/text/Text;IIZ)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", ordinal = 1))
    private void hookAllMessagesAdd(List instance, int i, Object e) {
        ChatHudLine message = (ChatHudLine<Text>) e;
        for (ChatHudLine<OrderedText> line: ImmutableList.copyOf(currectLines)){
            UUIDAccessor.setUUID(line, UUIDAccessor.getUUID(message));
        }
        currectLines.clear();
        this.messages.add(i, message);
    }



    @Inject(method = "clear", at = @At("HEAD"))
    private void onClearMessages(boolean clearSentMsgHistory, CallbackInfo ci){
        ChatTranslator.translatedChatComponent.clearMessages(clearSentMsgHistory);
        ChatStatus.hoverUUID = null;
        ChatStatus.lastMessageUUID = null;
    }

    @Inject(method = "reset", at = @At("HEAD"))
    private void onRescaleChat(CallbackInfo ci){
        ChatTranslator.translatedChatComponent.rescaleChat();
    }

    @Shadow private int scrolledLines;
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private List<ChatHudLine<OrderedText>> visibleMessages;

    @Shadow public abstract int getVisibleLineCount();

    @Shadow @Final private List<ChatHudLine> messages;

    @Inject(method = "scroll", at = @At("HEAD"))
    private void onScrollChat(double amount, CallbackInfo ci){
        if (ChatTranslator.translatedChatComponent.inMouse()){
            scrolledLines -= amount;
            ChatTranslator.translatedChatComponent.scrollChat(amount);
        }
    }
}
