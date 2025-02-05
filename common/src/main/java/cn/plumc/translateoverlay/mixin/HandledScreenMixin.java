package cn.plumc.translateoverlay.mixin;

import cn.plumc.translateoverlay.translates.ItemTranslator;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Shadow @Nullable protected Slot focusedSlot;

    @Shadow protected abstract List<Text> getTooltipFromItem(ItemStack stack);

    @Inject(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V"))
    private void onDrawMouseoverTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        ItemStack itemStack = this.focusedSlot.getStack();
        List<Text> texts = this.getTooltipFromItem(itemStack);
        ItemTranslator.translate(itemStack, context, texts, x, y);
    }
}
