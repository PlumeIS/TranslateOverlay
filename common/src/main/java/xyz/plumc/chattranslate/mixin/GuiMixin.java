package xyz.plumc.chattranslate.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.plumc.chattranslate.translates.ocr.OCRHud;

@Mixin(InGameHud.class)
public class GuiMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "TAIL"))
    public void render(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (client.currentScreen==null) OCRHud.instance.render(matrices, null, null, tickDelta);
    }
}
