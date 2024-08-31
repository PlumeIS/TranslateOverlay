package xyz.plumc.chattranslate.quilt.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import xyz.plumc.chattranslate.command.TranslateCommand;

public class TranslateCommandQuilt {
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
