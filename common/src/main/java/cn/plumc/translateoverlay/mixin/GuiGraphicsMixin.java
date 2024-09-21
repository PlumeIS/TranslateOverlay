package cn.plumc.translateoverlay.mixin;

import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import cn.plumc.translateoverlay.accessor.UUIDAccessor;
import cn.plumc.translateoverlay.components.TranslatedChatComponent;
import cn.plumc.translateoverlay.translates.chat.ChatStatus;

import java.util.UUID;

@Mixin(DrawContext.class)
public abstract class GuiGraphicsMixin {
    @Shadow public abstract void fill(int minX, int minY, int maxX, int maxY, int z, int color);

    @Inject(method = "fill(IIIII)V", at = @At("HEAD"), cancellable = true)
    private void onFill(int minX, int minY, int maxX, int maxY, int color, CallbackInfo ci) {
        if (!ChatStatus.rendering) return;

        UUID uuid = UUIDAccessor.getUUID(ChatStatus.renderingLine);
        if (ChatStatus.hoverUUID != null && ChatStatus.hoverUUID == uuid){
            ci.cancel();
            this.fill(minX, minY, maxX, maxY, 0, TranslatedChatComponent.HOVER_COLOR | color);
        } else {
            if (ChatStatus.lastMessageUUID != null && ChatStatus.lastMessageUUID == uuid){
                ci.cancel();
                this.fill(minX, minY, maxX, maxY, 0, 0x33ff99 | ((int)((color >> 24) * 0.5)) << 24);
            }
        }
    }
}
