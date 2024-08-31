package xyz.plumc.chattranslate.mixin;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.plumc.chattranslate.accessor.UUIDAccessor;
import xyz.plumc.chattranslate.components.TranslatedChatComponent;
import xyz.plumc.chattranslate.translates.chat.ChatStatus;

import java.util.UUID;

@Mixin(DrawableHelper.class)
public abstract class DrawableHelperMixin {
    @Shadow
    public static void fill(MatrixStack matrices, int x1, int y1, int x2, int y2, int z, int color) {
    }

    @Inject(method = "fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V", at = @At("HEAD"), cancellable = true)
    private static void onFill(MatrixStack matrixStack, int minX, int minY, int maxX, int maxY, int color, CallbackInfo ci) {
        if (!ChatStatus.rendering) return;

        UUID uuid = UUIDAccessor.getUUID(ChatStatus.renderingLine);
        if (ChatStatus.hoverUUID != null && ChatStatus.hoverUUID == uuid){
            ci.cancel();
            fill(matrixStack, minX, minY, maxX, maxY, 0, TranslatedChatComponent.HOVER_COLOR | color);
        } else {
            if (ChatStatus.lastMessageUUID != null && ChatStatus.lastMessageUUID == uuid){
                ci.cancel();
                fill(matrixStack, minX, minY, maxX, maxY, 0, 0x33ff99 | ((int)((color >> 24) * 0.5)) << 24);
            }
        }
    }
}
