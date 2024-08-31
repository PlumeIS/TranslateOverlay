package xyz.plumc.chattranslate.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.plumc.chattranslate.config.ConfigManager;
import xyz.plumc.chattranslate.utils.TickUtil;

@Mixin(MinecraftClient.class)
public class MinecraftMixin {
    @Inject(at = @At("HEAD"), method = "tick")
    private void tick(CallbackInfo ci) {
        TickUtil.onClientTick();
    }
    
    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo ci) {
        ConfigManager.instance.save(null);
    }
}
