package xyz.plumc.chattranslate.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.plumc.chattranslate.translates.ocr.OCRHud;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        OCRHud.instance.render(matrices, mouseX, mouseY, delta);
    }

    @Inject(method = "resize", at = @At("HEAD"))
    private void onRebuildWidgets(CallbackInfo info) {
        OCRHud.instance.clearWithFadeOut();
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo info) {
        OCRHud.instance.clearWithFadeOut();
    }
}
