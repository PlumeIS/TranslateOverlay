package xyz.plumc.chattranslate.translates.chat;

import net.minecraft.client.gui.hud.ChatHudLine;

import java.util.UUID;

public class ChatStatus {
    public static int renderIndex = -1;
    public static ChatHudLine.Visible renderingLine;
    public static boolean rendering = false;
    public static UUID hoverUUID;
    public static UUID lastMessageUUID;
    public static ChatHudLine lastOriginalMessage;
    public static int originalMessageScrollbarPos;
}
