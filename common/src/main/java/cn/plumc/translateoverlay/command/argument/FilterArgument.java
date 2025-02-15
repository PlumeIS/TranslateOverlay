package cn.plumc.translateoverlay.command.argument;

import cn.plumc.translateoverlay.config.ConfigFile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class FilterArgument implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Arrays.asList("AAA", "abc", "foo");

    public static FilterArgument filter(){return new FilterArgument();}

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readUnquotedString();
    }

    public static String getFilter(CommandContext<ServerCommandSource> context, String argument){
        return context.getArgument(argument, String.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ConfigFile.filters, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
