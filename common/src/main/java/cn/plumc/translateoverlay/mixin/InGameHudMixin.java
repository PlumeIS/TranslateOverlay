package cn.plumc.translateoverlay.mixin;

import cn.plumc.translateoverlay.translates.ScoreboardTranslator;
import cn.plumc.translateoverlay.translates.TitleTranslator;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "setTitle", at = @At("HEAD"))
    private void onSetTitle(Text title, CallbackInfo ci) {
        TitleTranslator.translateTitle(title);
    }

    @Inject(method = "setSubtitle", at = @At("HEAD"))
    private void onSetSubtitle(Text title, CallbackInfo ci) {
        TitleTranslator.translateSubtitle(title);
    }

    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci){
        TitleTranslator.translateOverlayMessage(message);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        TitleTranslator.render(matrices, tickDelta);
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"))
    private void onRenderScoreboardSidebar(MatrixStack matrices, ScoreboardObjective objective, CallbackInfo ci) {
        ScoreboardTranslator.translate(matrices, objective);
    }
}
