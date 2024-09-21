package cn.plumc.translateoverlay.forge.listeners;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cn.plumc.translateoverlay.forge.TranslateOverlayForge;

@Mod.EventBusSubscriber
public class ChatListener {
    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) throws CommandSyntaxException {
        if (event.getMessage().startsWith("!")){
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            ServerCommandSource source = new ServerCommandSource(CommandOutput.DUMMY, player.getPos(), player.getRotationClient(), null, 4, player.getName().getString(), player.getDisplayName(), null, player);
            ParseResults<ServerCommandSource> parse = TranslateOverlayForge.DISPATCHER.parse(event.getMessage().substring(1), source);
            if(!parse.getContext().getNodes().isEmpty()){
                event.setCanceled(true);
                TranslateOverlayForge.DISPATCHER.execute(parse);
            }
        }
    }
}
