package cn.plumc.translateoverlay.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import cn.plumc.translateoverlay.utils.KeyUtil;

@Mixin(Keyboard.class)
public class KeyboardHandlerMixin {
    @Inject(method = "onKey", at = @At("HEAD"))
    public void onKeyPress(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null) {
            if (action == 0) {
                KeyUtil.onScreenKeyReleased(key, scanCode);
            } else {
                KeyUtil.onScreenKeyPress(key, scanCode);
            }
        }
    }
}
