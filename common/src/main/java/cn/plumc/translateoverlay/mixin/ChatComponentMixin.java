package cn.plumc.translateoverlay.mixin;

import cn.plumc.translateoverlay.accessor.UUIDAccessor;
import cn.plumc.translateoverlay.translates.ChatTranslator;
import cn.plumc.translateoverlay.translates.chat.ChatStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static cn.plumc.translateoverlay.translates.ChatTranslator.checkStatus;

@Mixin(ChatHud.class)
public abstract class ChatComponentMixin {
    @Unique

    @Inject(method = "tickRemovalQueueIfExists", at = @At("HEAD"))
    private void onTick(CallbackInfo ci){
        ChatTranslator.translatedChatComponent.tick();
    }

    @Inject(at = @At(value = "HEAD"), method = "addVisibleMessage")
    private void onMessage(ChatHudLine message, CallbackInfo ci){
        if (!checkStatus(message.content().getString())) return;
        ChatTranslator.translate(message);
    }

    @Inject(method = "addVisibleMessage", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", shift = At.Shift.AFTER))
    private void onAddLine(ChatHudLine message, CallbackInfo ci) {
        UUIDAccessor.setUUID(visibleMessages.get(0), UUIDAccessor.getUUID(message));
    }


    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci){
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

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", ordinal = 0, shift = At.Shift.BEFORE))
    private void onRenderBackgroundStart(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci){
        ChatStatus.renderingLine = this.visibleMessages.get(ChatStatus.renderIndex);
        ChatStatus.rendering = true;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", ordinal = 0, shift = At.Shift.AFTER))
    private void onRenderBackgroundEnd(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci){
        ChatStatus.renderIndex ++;
        ChatStatus.rendering = false;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderEnd(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci){
        ChatStatus.renderingLine = null;
        ChatStatus.renderIndex = -1;
        ChatTranslator.translatedChatComponent.render(context, currentTick, mouseX, mouseY);
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
