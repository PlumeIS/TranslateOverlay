package xyz.plumc.chattranslate.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import xyz.plumc.chattranslate.command.TranslatorCommand;
import xyz.plumc.chattranslate.config.Config;
import xyz.plumc.chattranslate.fabric.command.argument.FilterArgument;
import xyz.plumc.chattranslate.fabric.command.argument.LanguageArgument;
import xyz.plumc.chattranslate.fabric.command.argument.OCRArgument;
import xyz.plumc.chattranslate.fabric.command.argument.TranslatorArgument;

public class TranslatorCommandFabric {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher){
        dispatcher.register(ClientCommandManager.literal("translator").executes(context -> TranslatorCommand.help())
                .then(ClientCommandManager.literal("help").executes(context -> TranslatorCommand.help())
                )
                .then(ClientCommandManager.literal("set").executes(context -> TranslatorCommand.help())
                        .then(ClientCommandManager.argument("translator", xyz.plumc.chattranslate.command.argument.TranslatorArgument.translators()).executes((context) -> TranslatorCommand.setTranslator(TranslatorArgument.getTranslator(context, "translator")))
                        )
                )
                .then(ClientCommandManager.literal("from").executes(context -> TranslatorCommand.help())
                        .then(ClientCommandManager.argument("language", xyz.plumc.chattranslate.command.argument.LanguageArgument.languageFrom()).executes((context)-> TranslatorCommand.language(xyz.plumc.chattranslate.command.argument.LanguageArgument.LanguageSide.FROM, LanguageArgument.getLanguage(context, "language")))
                        )
                )
                .then(ClientCommandManager.literal("to").executes(context -> TranslatorCommand.help())
                        .then(ClientCommandManager.argument("language", xyz.plumc.chattranslate.command.argument.LanguageArgument.languageTo()).executes((context)-> TranslatorCommand.language(xyz.plumc.chattranslate.command.argument.LanguageArgument.LanguageSide.TO, LanguageArgument.getLanguage(context, "language")))
                        )
                )
                .then(ClientCommandManager.literal("ocr").executes(context -> TranslatorCommand.help())
                        .then(ClientCommandManager.argument("ocr", xyz.plumc.chattranslate.command.argument.OCRArgument.ocrs()).executes((context)-> TranslatorCommand.ocr(OCRArgument.getOCR(context, "ocr")))
                        )
                )
                .then(ClientCommandManager.literal("toggle").executes((context) -> TranslatorCommand.toggle(!Config.getToggle()))
                        .then(ClientCommandManager.literal("true").executes((context)-> TranslatorCommand.toggle(true))
                        )
                        .then(ClientCommandManager.literal("false").executes((context) -> TranslatorCommand.toggle(false))
                        )
                )
                .then(ClientCommandManager.literal("filter").executes(context -> TranslatorCommand.help())
                        .then(ClientCommandManager.literal("add").executes(context -> TranslatorCommand.help())
                                .then(ClientCommandManager.argument("filter", StringArgumentType.string()).executes(context -> TranslatorCommand.addFilter(StringArgumentType.getString(context, "filter")))
                                )
                        )
                        .then(ClientCommandManager.literal("remove").executes(context -> TranslatorCommand.help())
                                .then(ClientCommandManager.argument("filter", xyz.plumc.chattranslate.command.argument.FilterArgument.filter()).executes(context -> TranslatorCommand.removeFilter(FilterArgument.getFilter(context, "filter")))
                                )
                        )
                        .then(ClientCommandManager.literal("list").executes(context -> TranslatorCommand.listFilter(0))
                                .then(ClientCommandManager.argument("page", IntegerArgumentType.integer(0)).executes(context -> TranslatorCommand.listFilter(IntegerArgumentType.getInteger(context, "page")))
                                )
                        )
                )
                .then(ClientCommandManager.literal("config").executes(context -> TranslatorCommand.help())
                        .then(ClientCommandManager.literal("load").executes(context -> TranslatorCommand.configLoad()))
                        .then(ClientCommandManager.literal("save").executes(context -> TranslatorCommand.configSave()))
                        .then(ClientCommandManager.literal("write")
                                .then(ClientCommandManager.argument("data", StringArgumentType.string()).executes(context -> TranslatorCommand.configWrite(StringArgumentType.getString(context, "data"))))
                        )
                        .then(ClientCommandManager.literal("open").executes(context -> TranslatorCommand.configOpen()))
                )
        );
    }
}
