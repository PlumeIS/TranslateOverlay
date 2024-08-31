package xyz.plumc.chattranslate.mixin;

import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(ChatHudLine.Visible.class)
public abstract class GuiMessage$LineMixin{
    @Unique
    private UUID uuid = UUID.randomUUID();
}
