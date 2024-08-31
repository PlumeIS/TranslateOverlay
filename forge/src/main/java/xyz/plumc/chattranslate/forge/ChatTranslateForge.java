package xyz.plumc.chattranslate.forge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import xyz.plumc.chattranslate.ChatTranslate;
import xyz.plumc.chattranslate.command.TranslateCommand;
import xyz.plumc.chattranslate.command.TranslatorCommand;
import xyz.plumc.chattranslate.forge.listeners.ChatListener;

@Mod(ChatTranslate.MOD_ID)
@Mod.EventBusSubscriber
public class ChatTranslateForge {

    public static final CommandDispatcher<ServerCommandSource> DISPATCHER = new CommandDispatcher<>();

    public ChatTranslateForge() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ChatListener());
        ClientRegistry.registerKeyBinding(ChatTranslate.OCRKey);
        ClientRegistry.registerKeyBinding(ChatTranslate.translateKey);
        ChatTranslate.init();
        TranslateCommand.register(DISPATCHER);
        TranslatorCommand.register(DISPATCHER);
    }
}
