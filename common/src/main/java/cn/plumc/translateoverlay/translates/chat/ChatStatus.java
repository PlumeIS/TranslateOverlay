package cn.plumc.translateoverlay.translates.chat;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;

import java.util.UUID;

public class ChatStatus {
    public static int renderIndex = -1;
    public static UUID hoverUUID;
    public static UUID lastMessageUUID;
    public static ChatHudLine lastOriginalMessage;
    public static int originalMessageScrollbarPos;
}
