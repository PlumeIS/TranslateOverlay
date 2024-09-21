package cn.plumc.translateoverlay.translates;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import cn.plumc.translateoverlay.TranslateOverlay;
import cn.plumc.translateoverlay.config.Config;
import cn.plumc.translateoverlay.utils.ChatColor;
import cn.plumc.translateoverlay.utils.MessageUtil;
import cn.plumc.translateoverlay.utils.TickUtil;

public class SignTranslator {
    public static void translate(BlockHitResult rayTraceResult){
        if (rayTraceResult.getType()== HitResult.Type.BLOCK) {
            BlockPos blockPos = rayTraceResult.getBlockPos();
            BlockEntity blockEntity = MinecraftClient.getInstance().world.getBlockEntity(blockPos);
            if (blockEntity instanceof SignBlockEntity signBlockEntity){
                SignBlockEntity cloneSignBlockEntity = new SignBlockEntity(signBlockEntity.getPos(), signBlockEntity.getCachedState());
                cloneSignBlockEntity.setWorld(MinecraftClient.getInstance().world);
                TranslateOverlay.threadFactory.newThread(()->{
                    TranslateOverlay.logger.info("Translating a sign with %s in x:%s y:%s z:%s".formatted(Config.getTranslator().getTranslatorName(),
                            blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                    Text[] frontMessages = signBlockEntity.getTexts(false);
                    for (int i = 0; i < frontMessages.length; i++){
                        if (!frontMessages[i].getString().isEmpty()) {
                            String message = MessageUtil.serializeMutableComponent(frontMessages[i], ChatTranslator.STYLE_HOLDER);
                            cloneSignBlockEntity.setTextOnRow(i, MessageUtil.parseComponent(Config.getTranslator().translate(message).replaceAll(" §", "§").replaceAll("�", ""), ChatColor.BLACK));
                        }
                    }
                    TickUtil.tickRun(()->MinecraftClient.getInstance().setScreen(new ReadOnlySignScreen(cloneSignBlockEntity)));
                }).start();
            }
        }
    }

    public static class ReadOnlySignScreen extends SignEditScreen {

        public ReadOnlySignScreen(SignBlockEntity signBlockEntity) {
            super(signBlockEntity, true);
        }

        @Override
        public boolean charTyped(char p_252008_, int p_251178_) {
            return false;
        }

        @Override
        public boolean keyPressed(int p_252300_, int p_250424_, int p_250697_) {
            if (p_252300_==GLFW.GLFW_KEY_BACKSPACE||p_252300_==GLFW.GLFW_KEY_ENTER||p_252300_==GLFW.GLFW_KEY_KP_ENTER) return false;
            return super.keyPressed(p_252300_, p_250424_, p_250697_);
        }

        @Override
        public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
            super.render(matrixStack, mouseX, mouseY, delta);
            DrawableHelper.drawCenteredTextWithShadow(matrixStack, this.textRenderer, (OrderedText) Text.of(I18n.translate("sign.translate.message")), this.width / 2, 40+textRenderer.fontHeight+2, 16777215);
        }


        @Override
        public void removed() {
        }
    }
}