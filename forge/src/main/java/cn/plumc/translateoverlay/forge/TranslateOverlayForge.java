package cn.plumc.translateoverlay.forge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import cn.plumc.translateoverlay.TranslateOverlay;
import cn.plumc.translateoverlay.command.TranslateCommand;
import cn.plumc.translateoverlay.command.TranslatorCommand;
import cn.plumc.translateoverlay.forge.listeners.ChatListener;

@Mod(TranslateOverlay.MOD_ID)
@Mod.EventBusSubscriber
public class TranslateOverlayForge {

    public static final CommandDispatcher<ServerCommandSource> DISPATCHER = new CommandDispatcher<>();

    public TranslateOverlayForge() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ChatListener());
        ClientRegistry.registerKeyBinding(TranslateOverlay.OCRKey);
        ClientRegistry.registerKeyBinding(TranslateOverlay.translateKey);
        TranslateOverlay.init();
        TranslateCommand.register(DISPATCHER);
        TranslatorCommand.register(DISPATCHER);
    }
}
