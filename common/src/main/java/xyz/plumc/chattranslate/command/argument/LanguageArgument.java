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
import xyz.plumc.chattranslate.translate.Language;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class LanguageArgument implements ArgumentType<Language> {
    LanguageSide side;
    public LanguageArgument(LanguageSide side){
        super();
        this.side = side;
    }
    public static LanguageArgument languageTo(){return new LanguageArgument(LanguageSide.TO);}

    public static LanguageArgument languageFrom(){return new LanguageArgument(LanguageSide.FROM);}

    public static LanguageArgument language(LanguageSide side){return new LanguageArgument(side);}

    public static Language getLanguage(CommandContext<ServerCommandSource> context, String argument){
        return context.getArgument(argument, Language.class);
    }
    private static final Collection<String> EXAMPLES = Arrays.asList("auto-detect", "en", "zh-Hans");
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((language) -> {
        return Text.translatable("argument.enum.invalid", language);
    });
    @Override
    public Language parse(StringReader reader) throws CommandSyntaxException {
        String translate = reader.readUnquotedString();
        Language language = Language.of(translate);
        if (language==null||(side == LanguageSide.TO && (language == Language.AUTO_DETECT))){
            throw ERROR_INVALID_VALUE.create(translate);
        } else{
            return language;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (side==LanguageSide.FROM){
            return CommandSource.suggestMatching(Arrays.stream(Language.values()).map((l)-> l.code), builder);
        } else {
            return CommandSource.suggestMatching(Arrays.stream(Language.values()).map((l)->l.code).filter((l)-> !l.equals(Language.AUTO_DETECT.code)), builder);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static enum LanguageSide{
        FROM,
        TO
    }
}
