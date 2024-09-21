package cn.plumc.translateoverlay.command;

import cn.plumc.translateoverlay.TranslateOverlay;
import cn.plumc.translateoverlay.command.argument.FilterArgument;
import cn.plumc.translateoverlay.command.argument.LanguageArgument;
import cn.plumc.translateoverlay.command.argument.OCRArgument;
import cn.plumc.translateoverlay.command.argument.TranslatorArgument;
import cn.plumc.translateoverlay.config.Config;
import cn.plumc.translateoverlay.config.ConfigFile;
import cn.plumc.translateoverlay.config.ConfigManager;
import cn.plumc.translateoverlay.translate.Language;
import cn.plumc.translateoverlay.translate.Translator;
import cn.plumc.translateoverlay.translates.ChatTranslator;
import cn.plumc.translateoverlay.translates.ocr.OCR;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

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
        TranslateOverlay.threadFactory.newThread(() -> {
            Config.setTranslator(translator);
            ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.set.successful"));
        }).start();
        return 1;
    }

    public static int help() {
        ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.help"));
        return 1;
    }

    public static int language(LanguageArgument.LanguageSide side, Language language){
        if (side==LanguageArgument.LanguageSide.FROM) {
            Config.setLangFrom(language);
            TranslateOverlay.threadFactory.newThread(()->{
                Config.getOCR().init(language);
            }).start();
        }
        if (side==LanguageArgument.LanguageSide.TO) Config.setLangTo(language);
        Config.getTranslator().clearCache();
        ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.language.successful"));
        return 1;
    }

    public static int ocr(OCR ocr){
        Config.setOCR(ocr);
        ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.ocr.successful"));
        return 1;
    }

    public static int toggle(boolean toggle){
        Config.setToggle(toggle);
        ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.toggle", toggle?I18n.translate("commands.translator.toggle.true"):I18n.translate("commands.translator.toggle.false")));
        return 1;
    }

    public static int addFilter(String filter){
        ConfigFile.filters.add(filter);
        ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.filter.add", filter));
        return 1;
    }

    public static int removeFilter(String filter){
        if (ConfigFile.filters.contains(filter)) {
            ConfigFile.filters.remove(filter);
            ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.remove.successful", filter));
            return 1;
        }
        else{
            ConfigFile.filters.remove(ConfigFile.filters.get(Integer.parseInt(filter)));
            ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.remove.fail", filter));
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
            ChatTranslator.sendBypassMessage(Text.literal(stringBuilder.toString().substring(0, stringBuilder.toString().length()-1)));
            return 1;
        } else ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.filter.list.invalid_page", maxPage));
        return 0;
    }

    public static int configLoad(){
        TranslateOverlay.threadFactory.newThread(()->{
            try {
                ConfigManager.instance.load();
                Config.load();
                ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.config.load.successful"));
            } catch (Exception e) {
                ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.config.load.fail", e.getMessage()));
            }
        }).start();
        return 1;
    }

    public static int configSave(){
        TranslateOverlay.threadFactory.newThread(()->{
            ConfigManager.instance.save(null);
            ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.config.save.successful"));
        }).start();
        return 1;
    }

    public static int configWrite(String raw){
        TranslateOverlay.threadFactory.newThread(()-> {
            Gson gson = new Gson();
            try {
                JsonElement jsonElement = JsonParser.parseString(raw);
                for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                    try {
                        Field field = ConfigFile.class.getDeclaredField(entry.getKey());
                        field.setAccessible(true);
                        field.set(ConfigFile.class, gson.fromJson(entry.getValue(), field.getType()));
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.config.write.fail", "Skip key %s".formatted(entry.getKey())));
                    } catch (Exception e) {
                        ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.config.write.fail", e.getMessage()));
                    }
                }
                ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.config.write.successful"));
                Config.load();
            } catch (Exception e) {
                ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.config.write.fail", e.getMessage()));
            }
        }).start();
        return 1;
    }

    public static int configOpen(){
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(ConfigManager.CONFIG_DIR);
            } catch (IOException e) {
                ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.config.open.fail", e.getMessage()));
            }
        } else ChatTranslator.sendBypassMessage(Text.translatable("commands.translator.config.open.fail", "Unsupported operation"));
        return 1;
    }
}
