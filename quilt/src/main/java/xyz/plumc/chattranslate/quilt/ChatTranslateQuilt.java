package xyz.plumc.chattranslate.quilt;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import xyz.plumc.chattranslate.fabriclike.ChatTranslateFabricLike;
import xyz.plumc.chattranslate.quilt.command.TranslateCommandQuilt;
import xyz.plumc.chattranslate.quilt.command.TranslatorCommandQuilt;

import static xyz.plumc.chattranslate.ChatTranslate.OCRKey;
import static xyz.plumc.chattranslate.ChatTranslate.translateKey;

public class ChatTranslateQuilt implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            TranslateCommandQuilt.register(dispatcher);
            TranslatorCommandQuilt.register(dispatcher);
        });
        KeyBindingHelper.registerKeyBinding(translateKey);
        KeyBindingHelper.registerKeyBinding(OCRKey);
        ChatTranslateFabricLike.init();
    }
}
