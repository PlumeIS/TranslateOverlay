package cn.plumc.translateoverlay.utils;

import cn.plumc.translateoverlay.config.Config;
import cn.plumc.translateoverlay.translate.translator.Translator;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

public class TranslateUtil {
    public static final String STYLE_HOLDER = "�";
    public static final List<String> CLEAR_STYLE = List.of("§0 ", "§1 ", "§2 ", "§3 ", "§4 ", "§5 ", "§6 ", "§7 ", "§8 ", "§9 ", "§a ", "§b ", "§c ", "§d ", "§e ", "§f ", "§k ", "§l ", "§m ", "§n ", "§o ", "§r ");

    public static Text translateText(Translator translator, Text component){
        Text result;
        switch (translator.getHandlerMethod()) {
            case NULL -> result = TranslateUtil.translateComponentOriginal(component);
            case SPLIT -> result = TranslateUtil.translateComponents(component);
            case REJOIN_HOLDER -> result = TranslateUtil.translateSerializedComponentsWithHolder(component);
            default -> result = TranslateUtil.translateSerializedComponents(component);
        }
        return result;
    }


    public static Text translateComponentOriginal(Text component){
        return Text.literal(Config.getTranslator().translate(component.getString()));
    }
    public static Text translateComponents(Text rawComponent){
        MutableText translateComponents = Text.empty();
        for (Text component : rawComponent.withoutStyle()){
            String string = component.getString();
            Style style = component.getStyle();
            String translated = Config.getTranslator().translate(string);
            translateComponents.append(Text.literal(translated).fillStyle(style));
        }
        return translateComponents;
    }
    public static Text translateSerializedComponents(Text rawComponent){
        String message = MessageUtil.serializeMutableComponent(rawComponent, null);
        return Text.literal(Config.getTranslator().translate(message));
    }
    public static Text translateSerializedComponentsWithHolder(Text rawComponent){
        String message = MessageUtil.serializeMutableComponent(rawComponent, STYLE_HOLDER);
        String translated = Config.getTranslator().translate(message);
        String result = translated.replaceAll(" §", "§").replaceAll("�", "");
        for (String s : CLEAR_STYLE){
            result = result.replaceAll(s, s.substring(0, 2));
        }
        return MessageUtil.parseComponent(result, ChatColor.WHITE);
    }
}
