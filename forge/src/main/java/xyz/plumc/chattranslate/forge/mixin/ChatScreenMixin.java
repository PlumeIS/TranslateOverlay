package xyz.plumc.chattranslate.forge.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.plumc.chattranslate.forge.uitls.ClientCommandSuggestions;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Redirect(method = "init", at = @At(value = "NEW", target = "Lnet/minecraft/client/gui/screen/CommandSuggestor;"))
    public CommandSuggestor initialize(MinecraftClient client, Screen owner, TextFieldWidget textField, TextRenderer textRenderer, boolean slashOptional, boolean suggestingWhenEmpty, int inWindowIndexOffset, int maxSuggestionSize, boolean chatScreenSized, int color) {
        return new ClientCommandSuggestions(client, owner, textField, textRenderer, slashOptional, suggestingWhenEmpty, inWindowIndexOffset, maxSuggestionSize, chatScreenSized, color);
    }
}
