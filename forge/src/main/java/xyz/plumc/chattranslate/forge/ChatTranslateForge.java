package xyz.plumc.chattranslate.forge;

import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.plumc.chattranslate.ChatTranslate;
import xyz.plumc.chattranslate.command.TranslateCommand;
import xyz.plumc.chattranslate.command.TranslatorCommand;

@Mod(ChatTranslate.MOD_ID)
@Mod.EventBusSubscriber
public class ChatTranslateForge {
    public ChatTranslateForge() {
        MinecraftForge.EVENT_BUS.register(this);
        ClientRegistry.registerKeyBinding(ChatTranslate.OCRKey);
        ClientRegistry.registerKeyBinding(ChatTranslate.translateKey);
        ChatTranslate.init();
    }
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterCommandsEvent event) {
        TranslateCommand.register(event.getDispatcher());
        TranslatorCommand.register(event.getDispatcher());
    }
}
