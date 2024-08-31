package xyz.plumc.chattranslate.forge;

import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import xyz.plumc.chattranslate.ChatTranslate;
import xyz.plumc.chattranslate.command.TranslateCommand;
import xyz.plumc.chattranslate.command.TranslatorCommand;

@Mod(ChatTranslate.MOD_ID)
@Mod.EventBusSubscriber
public class ChatTranslateForge {
    public ChatTranslateForge() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().register(new KeyListener());
        ChatTranslate.init();
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
            event.register(ChatTranslate.OCRKey);
            event.register(ChatTranslate.translateKey);
        }
    }
}
