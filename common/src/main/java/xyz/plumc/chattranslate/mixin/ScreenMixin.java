package xyz.plumc.chattranslate.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.plumc.chattranslate.translates.ocr.OCRHud;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        OCRHud.instance.render(context, mouseX, mouseY, delta);
    }

    @Inject(method = "clearAndInit", at = @At("HEAD"))
    private void onRebuildWidgets(CallbackInfo info) {
        OCRHud.instance.clearWithFadeOut();
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo info) {
        OCRHud.instance.clearWithFadeOut();
    }
}
