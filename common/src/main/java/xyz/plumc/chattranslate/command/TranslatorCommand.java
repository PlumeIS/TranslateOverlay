package xyz.plumc.chattranslate.command;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import xyz.plumc.chattranslate.ChatTranslate;
import xyz.plumc.chattranslate.command.argument.FilterArgument;
import xyz.plumc.chattranslate.command.argument.LanguageArgument;
import xyz.plumc.chattranslate.command.argument.OCRArgument;
import xyz.plumc.chattranslate.command.argument.TranslatorArgument;
import xyz.plumc.chattranslate.config.Config;
import xyz.plumc.chattranslate.config.ConfigFile;
import xyz.plumc.chattranslate.config.ConfigManager;
import xyz.plumc.chattranslate.translate.Language;
import xyz.plumc.chattranslate.translate.Translator;
import xyz.plumc.chattranslate.translates.ChatTranslator;
import xyz.plumc.chattranslate.translates.ocr.OCR;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class TranslatorCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(CommandManager.literal("translator").executes(context -> TranslatorCommand.help())
                    .then(CommandManager.literal("help").executes(context -> TranslatorCommand.help())
                )
                .then(CommandManager.literal("set").executes(context -> TranslatorCommand.help())
                            .then(CommandManager.argument("translator", TranslatorArgument.translators()).executes((context) -> setTranslator(TranslatorArgument.getTranslator(context, "translator")))
                        )
                )
                .then(CommandManager.literal("from").executes(context -> TranslatorCommand.help())
                            .then(CommandManager.argument("language", LanguageArgument.languageFrom()).executes((context)-> language(LanguageArgument.LanguageSide.FROM, LanguageArgument.getLanguage(context, "language")))
                        )
                )
                .then(CommandManager.literal("to").executes(context -> TranslatorCommand.help())
                            .then(CommandManager.argument("language", LanguageArgument.languageTo()).executes((context)-> language(LanguageArgument.LanguageSide.TO, LanguageArgument.getLanguage(context, "language")))
                        )
                )
                .then(CommandManager.literal("ocr").executes(context -> TranslatorCommand.help())
                        .then(CommandManager.argument("ocr", OCRArgument.ocrs()).executes((context)-> ocr(OCRArgument.getOCR(context, "ocr")))
                        )
                )
                .then(CommandManager.literal("toggle").executes((context) -> toggle(!Config.getToggle()))
                            .then(CommandManager.literal("true").executes((context)-> toggle(true))
                        )
                            .then(CommandManager.literal("false").executes((context) -> toggle(false))
                        )
                )
                .then(CommandManager.literal("filter").executes(context -> TranslatorCommand.help())
                        .then(CommandManager.literal("add").executes(context -> TranslatorCommand.help())
                                .then(CommandManager.argument("filter", StringArgumentType.string()).executes(context -> addFilter(StringArgumentType.getString(context, "filter")))
                            )
                        )
                        .then(CommandManager.literal("remove").executes(context -> TranslatorCommand.help())
                                    .then(CommandManager.argument("filter", FilterArgument.filter()).executes(context -> removeFilter(FilterArgument.getFilter(context, "filter")))
                                )
                        )
                        .then(CommandManager.literal("list").executes(context -> listFilter(0))
                                    .then(CommandManager.argument("page", IntegerArgumentType.integer(0)).executes(context -> listFilter(IntegerArgumentType.getInteger(context, "page")))
                                )
                        )
                )
                .then(CommandManager.literal("config").executes(context -> TranslatorCommand.help())
                        .then(CommandManager.literal("load").executes(context -> TranslatorCommand.configLoad()))
                        .then(CommandManager.literal("save").executes(context -> TranslatorCommand.configSave()))
                        .then(CommandManager.literal("write")
                                .then(CommandManager.argument("data", StringArgumentType.string()).executes(context -> TranslatorCommand.configWrite(StringArgumentType.getString(context, "data"))))
                        )
                        .then(CommandManager.literal("open").executes(context -> TranslatorCommand.configOpen()))
                )
        );
    }

    public static int setTranslator(Translator translator) {
        ChatTranslate.threadFactory.newThread(() -> {
            Config.setTranslator(translator);
            ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.set.successful"));
        }).start();
        return 1;
    }

    public static int help() {
        ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.help"));
        return 1;
    }

    public static int language(LanguageArgument.LanguageSide side, Language language){
        if (side==LanguageArgument.LanguageSide.FROM) {
            Config.setLangFrom(language);
            ChatTranslate.threadFactory.newThread(()->{
                Config.getOCR().init(language);
            }).start();
        }
        if (side==LanguageArgument.LanguageSide.TO) Config.setLangTo(language);
        Config.getTranslator().clearCache();
        ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.language.successful"));
        return 1;
    }

    public static int ocr(OCR ocr){
        Config.setOCR(ocr);
        ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.ocr.successful"));
        return 1;
    }

    public static int toggle(boolean toggle){
        Config.setToggle(toggle);
        ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.toggle", toggle?I18n.translate("commands.translator.toggle.true"):I18n.translate("commands.translator.toggle.false")));
        return 1;
    }

    public static int addFilter(String filter){
        ConfigFile.filters.add(filter);
        ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.filter.add", filter));
        return 1;
    }

    public static int removeFilter(String filter){
        if (ConfigFile.filters.contains(filter)) {
            ConfigFile.filters.remove(filter);
            ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.remove.successful", filter));
            return 1;
        }
        else{
            ConfigFile.filters.remove(ConfigFile.filters.get(Integer.parseInt(filter)));
            ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.remove.fail", filter));
            return 0;
        }
    }

    public static int listFilter(int page){
        List<String> filters = ConfigFile.filters;
        int maxPage = Math.max(0, MathHelper.ceil(filters.size() / 5.0F) - 1);
        if (page<=maxPage){
            String prefix = I18n.translate("commands.translator.filter.list.message", page, maxPage);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(prefix);
            for (int i = page*5; i<page*5+5&&i<filters.size(); i++){
                stringBuilder.append(I18n.translate("commands.translator.filter.list.entry", i, filters.get(i)));
            }
            ChatTranslator.sendBypassMessage(new LiteralText(stringBuilder.toString().substring(0, stringBuilder.toString().length()-1)));
            return 1;
        } else ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.filter.list.invalid_page", maxPage));
        return 0;
    }

    public static int configLoad(){
        ChatTranslate.threadFactory.newThread(()->{
            try {
                ConfigManager.instance.load();
                Config.load();
                ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.config.load.successful"));
            } catch (Exception e) {
                ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.config.load.fail", e.getMessage()));
            }
        }).start();
        return 1;
    }

    public static int configSave(){
        ChatTranslate.threadFactory.newThread(()->{
            ConfigManager.instance.save(null);
            ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.config.save.successful"));
        }).start();
        return 1;
    }

    public static int configWrite(String raw){
        ChatTranslate.threadFactory.newThread(()-> {
            Gson gson = new Gson();
            try {
                JsonElement jsonElement = JsonParser.parseString(raw);
                for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                    try {
                        Field field = ConfigFile.class.getDeclaredField(entry.getKey());
                        field.setAccessible(true);
                        field.set(ConfigFile.class, gson.fromJson(entry.getValue(), field.getType()));
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.config.write.fail", "Skip key %s".formatted(entry.getKey())));
                    } catch (Exception e) {
                        ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.config.write.fail", e.getMessage()));
                    }
                }
                ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.config.write.successful"));
                Config.load();
            } catch (Exception e) {
                ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.config.write.fail", e.getMessage()));
            }
        }).start();
        return 1;
    }

    public static int configOpen(){
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(ConfigManager.CONFIG_DIR);
            } catch (IOException e) {
                ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.config.open.fail", e.getMessage()));
            }
        } else ChatTranslator.sendBypassMessage(new TranslatableText("commands.translator.config.open.fail", "Unsupported operation"));
        return 1;
    }
}
