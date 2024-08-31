package xyz.plumc.chattranslate.forge.uitls;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import xyz.plumc.chattranslate.forge.ChatTranslateForge;

import java.util.Collection;

public class ClientCommandSuggestions extends CommandSuggestor {

	public ClientCommandSuggestions(MinecraftClient client, Screen owner, TextFieldWidget textField, TextRenderer textRenderer, boolean slashOptional, boolean suggestingWhenEmpty, int inWindowIndexOffset, int maxSuggestionSize, boolean chatScreenSized, int color) {
		super(client, owner, textField, textRenderer, slashOptional, suggestingWhenEmpty, inWindowIndexOffset, maxSuggestionSize, chatScreenSized, color);
	}

	@Override
	public void refresh() {
		var minecraft = MinecraftClient.getInstance();

		String chatInput = this.textField.getText();

		if (chatInput.startsWith("!")) {
			if (this.parse != null && !this.parse.getReader().getString().equals(chatInput)) {
				this.parse = null;
			}

			if (!this.completingSuggestions) {
				this.textField.setSuggestion(null);
				this.window = null;
			}

			this.messages.clear();
			StringReader reader = new StringReader(chatInput);
			for(int i = 0; i < "!".length(); i++)
				reader.skip();

			boolean isCommand = chatInput.startsWith("!");
			boolean canParse = this.slashOptional || isCommand;
			int cursorPosition = this.textField.getCursor();

			if (canParse) {
				CommandDispatcher<CommandSource> dispatcher = (CommandDispatcher<CommandSource>) (CommandDispatcher<?>) ChatTranslateForge.DISPATCHER;

				if (this.parse == null) {
					this.parse = dispatcher.parse(reader, minecraft.player.networkHandler.getCommandSource());
				}

				int errorPosition = this.suggestingWhenEmpty ? reader.getCursor() : 1;
				if (cursorPosition >= errorPosition && (this.window == null || !this.completingSuggestions)) {
					this.pendingSuggestions = dispatcher.getCompletionSuggestions(this.parse, cursorPosition);
					this.pendingSuggestions.thenRun(() ->
					{
						if (this.pendingSuggestions.isDone()) {
							this.show();
						}
					});
				}
			} else {
				String s1 = chatInput.substring(0, cursorPosition);
				int k = getLastPlayerNameStart(s1);
				Collection<String> collection = minecraft.player.networkHandler.getCommandSource().getPlayerNames();
				this.pendingSuggestions = CommandSource.suggestMatching(collection, new SuggestionsBuilder(s1, k));
			}
		} else {
			super.refresh();
		}
	}
}