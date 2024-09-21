package cn.plumc.translateoverlay.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import cn.plumc.translateoverlay.command.TranslateCommand;

public class TranslateCommandFabric {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher){
        dispatcher.register(
                ClientCommandManager.literal("translate").executes(context -> TranslateCommand.translate())
                    .then(ClientCommandManager.literal("book").executes(context -> TranslateCommand.translateBook())
                )
                    .then(ClientCommandManager.literal("sign").executes(context -> TranslateCommand.translateSign())
                )
                    .then(ClientCommandManager.literal("lectern").executes(context -> TranslateCommand.translateLectern())
                )
                    .then(ClientCommandManager.literal("OCR").executes((context) -> TranslateCommand.translateOCR(false))
                            .then(ClientCommandManager.literal("GUI").executes((context) -> TranslateCommand.translateOCR(true))
                        )
                )
        );
    }
}