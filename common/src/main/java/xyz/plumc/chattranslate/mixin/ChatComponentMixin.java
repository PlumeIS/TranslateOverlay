package xyz.plumc.chattranslate.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.message.MessageSignatureData;
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
import xyz.plumc.chattranslate.translates.ChatTranslator;
import xyz.plumc.chattranslate.translates.chat.ChatStatus;

import java.util.ArrayList;
import java.util.List;

import static xyz.plumc.chattranslate.translates.ChatTranslator.checkStatus;

@Mixin(ChatHud.class)
public abstract class ChatComponentMixin {
    @Unique
    List<ChatHudLine.Visible> currectLines = new ArrayList<>();

    @Inject(method = "tickRemovalQueueIfExists", at = @At("HEAD"))
    private void onTick(CallbackInfo ci){
        ChatTranslator.translatedChatComponent.tick();
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", shift = At.Shift.AFTER, ordinal = 1))
    private void onMessage(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci){
        if (!checkStatus(message.getString())) return;
        ChatTranslator.translate(this.messages.get(0));
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(MatrixStack matrices, int currentTick, int mouseX, int mouseY, CallbackInfo ci){

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

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V", ordinal = 0, shift = At.Shift.BEFORE))
    private void onRenderBackgroundStart(MatrixStack matrices, int currentTick, int mouseX, int mouseY, CallbackInfo ci){
        ChatStatus.renderingLine = this.visibleMessages.get(ChatStatus.renderIndex);
        ChatStatus.rendering = true;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V", ordinal = 0, shift = At.Shift.AFTER))
    private void onRenderBackgroundEnd(MatrixStack matrices, int currentTick, int mouseX, int mouseY, CallbackInfo ci){
        ChatStatus.renderIndex ++;
        ChatStatus.rendering = false;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderEnd(MatrixStack matrices, int currentTick, int mouseX, int mouseY, CallbackInfo ci){
        ChatStatus.renderingLine = null;
        ChatStatus.renderIndex = -1;
        ChatTranslator.translatedChatComponent.render(matrices, currentTick, mouseX, mouseY);
    }

    @Redirect(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", ordinal = 0))
    private void hookTrimmedMessagesAdd(List instance, int i, Object e) {
        ChatHudLine.Visible line = (ChatHudLine.Visible) e;
        currectLines.add(line);
        this.visibleMessages.add(i, line);
    }

    @Redirect(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", ordinal = 1))
    private void hookAllMessagesAdd(List instance, int i, Object e) {
        ChatHudLine message = (ChatHudLine) e;
        for (ChatHudLine.Visible line: ImmutableList.copyOf(currectLines)){
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
    @Shadow @Final private List<ChatHudLine.Visible> visibleMessages;

    @Shadow public abstract int getVisibleLineCount();

    @Shadow @Final private List<ChatHudLine> messages;

    @Inject(method = "scroll", at = @At("HEAD"))
    private void onScrollChat(int posInc, CallbackInfo ci){
        if (ChatTranslator.translatedChatComponent.inMouse()){
            scrolledLines -= posInc;
            ChatTranslator.translatedChatComponent.scrollChat(posInc);
        }
    }
}
