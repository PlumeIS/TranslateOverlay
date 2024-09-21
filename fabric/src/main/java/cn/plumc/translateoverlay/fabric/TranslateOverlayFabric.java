package cn.plumc.translateoverlay.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import cn.plumc.translateoverlay.fabric.command.TranslateCommandFabric;
import cn.plumc.translateoverlay.fabric.command.TranslatorCommandFabric;
import cn.plumc.translateoverlay.fabriclike.TranslateOverlayFabricLike;

import static cn.plumc.translateoverlay.TranslateOverlay.OCRKey;
import static cn.plumc.translateoverlay.TranslateOverlay.translateKey;

public class TranslateOverlayFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            TranslateCommandFabric.register(dispatcher);
            TranslatorCommandFabric.register(dispatcher);
        });
        KeyBindingHelper.registerKeyBinding(translateKey);
        KeyBindingHelper.registerKeyBinding(OCRKey);
        TranslateOverlayFabricLike.init();
    }
}
