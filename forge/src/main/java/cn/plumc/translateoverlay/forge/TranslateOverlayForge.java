package cn.plumc.translateoverlay.forge;

import cn.plumc.translateoverlay.TranslateOverlay;
import cn.plumc.translateoverlay.command.TranslateCommand;
import cn.plumc.translateoverlay.command.TranslatorCommand;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TranslateOverlay.MOD_ID)
@Mod.EventBusSubscriber
public class TranslateOverlayForge {
    public TranslateOverlayForge() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().register(new KeyListener());
        TranslateOverlay.init();
    }
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        TranslateCommand.register(event.getDispatcher());
        TranslatorCommand.register(event.getDispatcher());
    }

    @Mod.EventBusSubscriber
    static class KeyListener {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(TranslateOverlay.OCRKey);
            event.register(TranslateOverlay.translateKey);
        }
    }
}
