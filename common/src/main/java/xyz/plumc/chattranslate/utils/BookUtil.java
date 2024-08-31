package xyz.plumc.chattranslate.utils;

import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.BookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import xyz.plumc.chattranslate.config.Config;
import xyz.plumc.chattranslate.translates.ChatTranslator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static xyz.plumc.chattranslate.translates.ChatTranslator.CLEAR_STYLE;

public class BookUtil {
    public static ItemStack getEmptyBookItemStack(){
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        WrittenBookContentComponent writtenBookContentComponent = new WrittenBookContentComponent(RawFilteredPair.of("TranslateResult"), "ChatTranslate", 0, List.of(), false);
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, writtenBookContentComponent);
        return book;
    }

    public static ItemStack getTranslatedBook(BookScreen.Contents bookAccess){
        return getTranslatedBook(bookAccess, getEmptyBookItemStack());
    }

    public static ItemStack getTranslatedBook(BookScreen.Contents bookAccess, ItemStack book){
        List<RawFilteredPair<Text>> translatedPages = new ArrayList<>();
        for (int i = 0; i < bookAccess.getPageCount(); i++) {
            StringVisitable page = bookAccess.getPage(i);

            List<Text> list = new ArrayList<>();
            page.visit((style, string) -> {
                if (!string.isEmpty()) {
                    list.add(Text.literal(string).fillStyle(style));
                }
                return Optional.empty();
            }, Style.EMPTY);
            String message = MessageUtil.serializeMutableComponent(list, ChatTranslator.STYLE_HOLDER);
            String translated = Config.getTranslator().translate(message);
            String result = translated.replaceAll(" §", "§").replaceAll("�", "");
            for (String s : CLEAR_STYLE){
                result = result.replaceAll(s, s.substring(0, 2));
            }

            translatedPages.add(RawFilteredPair.of(MessageUtil.parseComponent(result, ChatColor.BLACK)));
        }
        WrittenBookContentComponent writtenBookContentComponent = new WrittenBookContentComponent(RawFilteredPair.of("TranslateResult"),
                "ChatTranslate",
                0,
                translatedPages,
                false);
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, writtenBookContentComponent);
        return book;
    }
}
