package cn.plumc.translateoverlay.forge;

import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cn.plumc.translateoverlay.TranslateOverlay;
import cn.plumc.translateoverlay.command.TranslateCommand;
import cn.plumc.translateoverlay.command.TranslatorCommand;

@Mod(TranslateOverlay.MOD_ID)
@Mod.EventBusSubscriber
public class TranslateOverlayForge {
    public TranslateOverlayForge() {
        MinecraftForge.EVENT_BUS.register(this);
        ClientRegistry.registerKeyBinding(TranslateOverlay.OCRKey);
        ClientRegistry.registerKeyBinding(TranslateOverlay.translateKey);
        TranslateOverlay.init();
    }
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        TranslateCommand.register(event.getDispatcher());
        TranslatorCommand.register(event.getDispatcher());
    }
}
