package xyz.plumc.chattranslate.mixin;

import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(ChatHudLine.class)
public abstract class GuiMessageMixin {
    @Unique
    private UUID uuid = UUID.randomUUID();
}
