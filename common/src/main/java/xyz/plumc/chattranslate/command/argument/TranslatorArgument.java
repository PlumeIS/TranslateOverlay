package xyz.plumc.chattranslate.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import xyz.plumc.chattranslate.translate.Translator;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class TranslatorArgument implements ArgumentType<Translator> {
    private static final Collection<String> EXAMPLES = Arrays.asList("bing", "google", "baidu");

    public static TranslatorArgument translators(){return new TranslatorArgument();}
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((translator) -> {
        return new TranslatableText("argument.enum.invalid", translator);
    });

    public static Translator getTranslator(CommandContext<ServerCommandSource> context, String argument){
        return context.getArgument(argument, Translator.class);
    }

    @Override
    public Translator parse(StringReader reader) throws CommandSyntaxException {
        String translate = reader.readUnquotedString();
        Translator translator = Translator.of(translate);
        if (translator==null){
            throw ERROR_INVALID_VALUE.create(translate);
        } else{
            return translator;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Arrays.stream(Translator.values()).map((t)-> t.platform), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
