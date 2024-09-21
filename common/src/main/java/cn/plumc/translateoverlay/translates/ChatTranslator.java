package cn.plumc.translateoverlay.translates;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.*;
import cn.plumc.translateoverlay.accessor.UUIDAccessor;
import cn.plumc.translateoverlay.components.TranslatedChatComponent;
import cn.plumc.translateoverlay.config.Config;
import cn.plumc.translateoverlay.config.ConfigFile;
import cn.plumc.translateoverlay.translate.translator.Translator;
import cn.plumc.translateoverlay.translates.chat.ChatStatus;
import cn.plumc.translateoverlay.utils.ChatColor;
import cn.plumc.translateoverlay.utils.MessageUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatTranslator {
    public static final String RESULT_PREFIX = "§6[§e%s§6]§f ";
    public static final String MESSAGE_PREFIX = "§6[TranslateOverlay]§f ";
    public static final String STYLE_HOLDER = "�";
    public static final List<String> CLEAR_STYLE = List.of("§0 ", "§1 ", "§2 ", "§3 ", "§4 ", "§5 ", "§6 ", "§7 ", "§8 ", "§9 ", "§a ", "§b ", "§c ", "§d ", "§e ", "§f ", "§k ", "§l ", "§m ", "§n ", "§o ", "§r ");
    public static TranslatedChatComponent translatedChatComponent = new TranslatedChatComponent(MinecraftClient.getInstance());

    static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void translate(ChatHudLine message){
        Text component = (Text) message.getText();
        executor.submit(()->{
            Text result;
            Translator translator = Config.getTranslator();
            switch (translator.getHandlerMethod()) {
                case NULL -> result = translateComponent(component);
                case SPLIT -> result = translateComponents(component);
                case REJOIN_HOLDER -> result = translateComponentsRH(component);
                default -> result = translateComponentsR(component);
            }
            ChatStatus.lastOriginalMessage = message;
            translatedChatComponent.addMessage(result, UUIDAccessor.getUUID(message));
        });
    }

    private static Text translateComponent(Text component){
        return new LiteralText(Config.getTranslator().translate(component.getString()));
    }
    private static Text translateComponents(Text rawComponent){
        MutableText translateComponents = LiteralText.EMPTY.copy();
        rawComponent.visit((style, asString) -> {
            String translated = Config.getTranslator().translate(asString);
            translateComponents.append(new LiteralText(translated).fillStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return translateComponents;
    }
    private static Text translateComponentsR(Text rawComponent){
        String message = MessageUtil.serializeMutableComponent(rawComponent, null);
        return new LiteralText(Config.getTranslator().translate(message));
    }
    private static Text translateComponentsRH(Text rawComponent){
        String message = MessageUtil.serializeMutableComponent(rawComponent, STYLE_HOLDER);
        String translated = Config.getTranslator().translate(message);
        String result = translated.replaceAll(" §", "§").replaceAll("�", "");
        for (String s : CLEAR_STYLE){
            result = result.replaceAll(s, s.substring(0, 2));
        }
        return MessageUtil.parseComponent(result, ChatColor.WHITE);
    }

    public static boolean checkStatus(String message){
        if (!Config.getToggle()) return false;
        if (message.startsWith(RESULT_PREFIX.formatted(Config.getLangTo().name))) return false;
        if (message.startsWith(MESSAGE_PREFIX)) return false;
        for (String filter: ConfigFile.filters){
            if (message.toLowerCase().contains(filter.toLowerCase())) return false;
        }
        return true;
    }
    public static void sendBypassMessage(Text component){
        MinecraftClient.getInstance().player.sendSystemMessage(new LiteralText(MESSAGE_PREFIX +component.getString()), UUID.randomUUID());
    }

}
