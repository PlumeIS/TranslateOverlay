package cn.plumc.translateoverlay.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import cn.plumc.translateoverlay.translates.*;

public class TranslateCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(CommandManager.literal("translate").executes(context -> TranslateCommand.translate())
                .then(CommandManager.literal("book").executes(context -> TranslateCommand.translateBook()))
                .then(CommandManager.literal("sign").executes(context -> TranslateCommand.translateSign()))
                .then(CommandManager.literal("lectern").executes(context -> TranslateCommand.translateLectern()))
                .then(CommandManager.literal("OCR").executes((context) -> translateOCR(false))
                        .then(CommandManager.literal("GUI").executes((context) -> translateOCR(true)))));
    }

    public static int translateOCR(boolean withGUI) {
        OCRTranslator.translate(withGUI);
        return 1;
    }

    public static int translate() {
        if (translateBook() == 1) {
            return 1;
        }
        if (translateSign() == 1) {
            return 1;
        }
        if (translateLectern() == 1) {
            return 1;
        }
        return 0;
    }

    public static int translateBook(){
        if (MinecraftClient.getInstance().currentScreen instanceof BookScreen bookViewScreen){
            BookTranslator.translate(bookViewScreen.contents);
            return 1;
        } else if (MinecraftClient.getInstance().player.getInventory().getMainHandStack().isOf(Items.WRITTEN_BOOK)
                ||MinecraftClient.getInstance().player.getInventory().getMainHandStack().isOf(Items.WRITABLE_BOOK)){
            ChatTranslator.sendBypassMessage(new TranslatableText("commands.translate.book.message"));
            BookTranslator.translate(BookScreen.Contents.create(MinecraftClient.getInstance().player.getInventory().getMainHandStack()));
            return 1;
        }
        return 0;
    }

    public static int translateLectern(){
        BlockHitResult rayTraceResult = rayTrace(5.0D);
        if (rayTraceResult.getType()== HitResult.Type.BLOCK){
            BlockPos blockPos = rayTraceResult.getBlockPos();
            BlockEntity blockEntity = MinecraftClient.getInstance().world.getBlockEntity(blockPos);
            if (blockEntity instanceof LecternBlockEntity){
                ChatTranslator.sendBypassMessage(new TranslatableText("commands.translate.lectern.message"));
                LecternTranslator.translate(rayTraceResult);
                return 1;
            }
        }
        return 0;
    }

    public static int translateSign(){
        BlockHitResult rayTraceResult = rayTrace(20.0D);
        if (rayTraceResult.getType()== HitResult.Type.BLOCK) {
            BlockPos blockPos = rayTraceResult.getBlockPos();
            BlockEntity blockEntity = MinecraftClient.getInstance().world.getBlockEntity(blockPos);
            if (blockEntity instanceof SignBlockEntity){
                ChatTranslator.sendBypassMessage(new TranslatableText("commands.translate.sign.message"));
                SignTranslator.translate(rayTraceResult);
                return 1;
            }
        }
        return 0;
    }

    private static BlockHitResult rayTrace(double maxDistance) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        Vec3d playerPos = player.getCameraPosVec(1.0F);
        Vec3d lookVec = player.getRotationVector();
        Vec3d endPos = playerPos.add(lookVec.x * maxDistance, lookVec.y * maxDistance, lookVec.z * maxDistance);
        return MinecraftClient.getInstance().world.raycast(new RaycastContext(playerPos, endPos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
    }
}
