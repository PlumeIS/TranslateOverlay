package cn.plumc.translateoverlay.quilt;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import cn.plumc.translateoverlay.fabriclike.TranslateOverlayFabricLike;
import cn.plumc.translateoverlay.quilt.command.TranslateCommandQuilt;
import cn.plumc.translateoverlay.quilt.command.TranslatorCommandQuilt;

import static cn.plumc.translateoverlay.TranslateOverlay.OCRKey;
import static cn.plumc.translateoverlay.TranslateOverlay.translateKey;

public class TranslateOverlayQuilt implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            TranslateCommandQuilt.register(dispatcher);
            TranslatorCommandQuilt.register(dispatcher);
        });
        KeyBindingHelper.registerKeyBinding(translateKey);
        KeyBindingHelper.registerKeyBinding(OCRKey);
        TranslateOverlayFabricLike.init();
    }
}
