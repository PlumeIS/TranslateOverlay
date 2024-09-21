package cn.plumc.translateoverlay.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import cn.plumc.translateoverlay.fabric.command.TranslateCommandFabric;
import cn.plumc.translateoverlay.fabric.command.TranslatorCommandFabric;
import cn.plumc.translateoverlay.TranslateOverlay;

import static cn.plumc.translateoverlay.TranslateOverlay.OCRKey;
import static cn.plumc.translateoverlay.TranslateOverlay.translateKey;

public class TranslateOverlayFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TranslateCommandFabric.register(ClientCommandManager.DISPATCHER);
        TranslatorCommandFabric.register(ClientCommandManager.DISPATCHER);
        KeyBindingHelper.registerKeyBinding(translateKey);
        KeyBindingHelper.registerKeyBinding(OCRKey);
        TranslateOverlay.init();
    }
}
