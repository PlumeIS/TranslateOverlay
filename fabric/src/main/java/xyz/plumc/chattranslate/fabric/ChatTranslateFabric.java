package xyz.plumc.chattranslate.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import xyz.plumc.chattranslate.fabric.command.TranslateCommandFabric;
import xyz.plumc.chattranslate.fabric.command.TranslatorCommandFabric;
import xyz.plumc.chattranslate.fabriclike.ChatTranslateFabricLike;

import static xyz.plumc.chattranslate.ChatTranslate.OCRKey;
import static xyz.plumc.chattranslate.ChatTranslate.translateKey;

public class ChatTranslateFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            TranslateCommandFabric.register(dispatcher);
            TranslatorCommandFabric.register(dispatcher);
        });
        KeyBindingHelper.registerKeyBinding(translateKey);
        KeyBindingHelper.registerKeyBinding(OCRKey);
        ChatTranslateFabricLike.init();
    }
}
