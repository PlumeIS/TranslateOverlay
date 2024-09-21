package cn.plumc.translateoverlay.command.argument;

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
import cn.plumc.translateoverlay.translates.ocr.OCR;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class OCRArgument implements ArgumentType<OCR> {
    private static final Collection<String> EXAMPLES = Arrays.asList("local", "baidu");

    public static OCRArgument ocrs(){return new OCRArgument();}
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((ocr) -> {
        return Text.translatable("argument.enum.invalid", ocr);
    });

    public static OCR getOCR(CommandContext<ServerCommandSource> context, String argument){
        return context.getArgument(argument, OCR.class);
    }

    @Override
    public OCR parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();
        OCR ocr = OCR.getOCR(name);
        if (ocr==null){
            throw ERROR_INVALID_VALUE.create(name);
        } else{
            return ocr;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Arrays.stream(OCR.values()).map((t)-> t.name), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
