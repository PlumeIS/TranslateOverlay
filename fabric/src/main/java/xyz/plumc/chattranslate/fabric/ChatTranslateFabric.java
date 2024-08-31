package xyz.plumc.chattranslate.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import xyz.plumc.chattranslate.fabric.command.TranslateCommandFabric;
import xyz.plumc.chattranslate.fabric.command.TranslatorCommandFabric;
import xyz.plumc.chattranslate.ChatTranslate;

import static xyz.plumc.chattranslate.ChatTranslate.OCRKey;
import static xyz.plumc.chattranslate.ChatTranslate.translateKey;

public class ChatTranslateFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TranslateCommandFabric.register(ClientCommandManager.DISPATCHER);
        TranslatorCommandFabric.register(ClientCommandManager.DISPATCHER);
        KeyBindingHelper.registerKeyBinding(translateKey);
        KeyBindingHelper.registerKeyBinding(OCRKey);
        ChatTranslate.init();
    }
}
