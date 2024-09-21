package cn.plumc.translateoverlay.translates;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.LecternScreen;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import cn.plumc.translateoverlay.TranslateOverlay;
import cn.plumc.translateoverlay.config.Config;
import cn.plumc.translateoverlay.utils.TickUtil;

import java.util.function.Predicate;

public class LecternTranslator {
    public static void translate(BlockHitResult rayTraceResult){
        if (rayTraceResult.getType()== HitResult.Type.BLOCK){
            BlockPos blockPos = rayTraceResult.getBlockPos();
            BlockEntity blockEntity = MinecraftClient.getInstance().world.getBlockEntity(blockPos);
            if (blockEntity instanceof LecternBlockEntity){
                MinecraftClient minecraft = MinecraftClient.getInstance();
                PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, rayTraceResult);
                minecraft.getNetworkHandler().sendPacket(packet);
                TickUtil.tickWait(new BookViewScreenPredicate(), ()->{
                    TranslateOverlay.logger.info("Waiting a lectern translation with %s in x:%s y:%s z:%s".formatted(Config.getTranslator().getTranslatorName(),
                            blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                    if (MinecraftClient.getInstance().currentScreen instanceof LecternScreen lecternScreen){
                        BookTranslator.translate(lecternScreen.contents);
                    }
                }, 5000);
            }
        }
    }
    private static class BookViewScreenPredicate implements Predicate<Object>{
        @Override
        public boolean test(Object o) {
            return MinecraftClient.getInstance().currentScreen!=null&&MinecraftClient.getInstance().currentScreen instanceof BookScreen;
        }
    }
}
