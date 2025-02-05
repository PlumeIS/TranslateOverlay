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
import cn.plumc.translateoverlay.utils.TranslateUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatTranslator {
    public static final String RESULT_PREFIX = "§6[§e%s§6]§f ";
    public static final String MESSAGE_PREFIX = "§6[TranslateOverlay]§f ";
    public static TranslatedChatComponent translatedChatComponent = new TranslatedChatComponent(MinecraftClient.getInstance());

    static ExecutorService executor = Executors.newFixedThreadPool(5);

    public static void translate(ChatHudLine message){
        Text component = (Text) message.getText();
        executor.submit(()->{
            Text result;
            Translator translator = Config.getTranslator();
            result = TranslateUtil.translateText(translator, component);
            ChatStatus.lastOriginalMessage = message;
            translatedChatComponent.addMessage(result, UUIDAccessor.getUUID(message));
        });
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
        if (MinecraftClient.getInstance().player!=null) MinecraftClient.getInstance().player.sendSystemMessage(new LiteralText(MESSAGE_PREFIX +component.getString()), UUID.randomUUID());
    }

}
