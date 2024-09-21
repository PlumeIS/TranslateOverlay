package cn.plumc.translateoverlay.utils;

import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import cn.plumc.translateoverlay.config.Config;
import cn.plumc.translateoverlay.translates.ChatTranslator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static cn.plumc.translateoverlay.translates.ChatTranslator.CLEAR_STYLE;

public class BookUtil {
    public static ItemStack getEmptyBookItemStack(){
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        NbtCompound compoundTag = new NbtCompound();
        compoundTag.putString("title", "TranslateResult");
        compoundTag.putString("author", "TranslateOverlay");
        book.setNbt(compoundTag);
        return book;
    }

    public static ItemStack getTranslatedBook(BookScreen.Contents bookAccess){
        return getTranslatedBook(bookAccess, getEmptyBookItemStack());
    }

    public static ItemStack getTranslatedBook(BookScreen.Contents bookAccess, ItemStack book){
        List<String> translatedPages = new ArrayList<>();
        ItemStack translatedBook = new ItemStack(Items.WRITTEN_BOOK);
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

            translatedPages.add(result);
        }
        NbtCompound itemTag = book.getNbt().copy();
        NbtList pagesTag = new NbtList();
        for (String i : translatedPages) {
            pagesTag.add(NbtString.of(i));
        }
        itemTag.put("pages", pagesTag);
        translatedBook.setNbt(itemTag);
        return book;
    }
}
