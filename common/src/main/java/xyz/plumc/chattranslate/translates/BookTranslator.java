package xyz.plumc.chattranslate.translates;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.item.ItemStack;
import xyz.plumc.chattranslate.ChatTranslate;
import xyz.plumc.chattranslate.utils.BookUtil;
import xyz.plumc.chattranslate.utils.TickUtil;

public class BookTranslator {
    public static void translate(BookScreen.Contents bookAccess){
        ChatTranslate.threadFactory.newThread(()->{
            ItemStack translatedBook = BookUtil.getTranslatedBook(bookAccess);
            TickUtil.tickRun(()-> {
                MinecraftClient.getInstance().setScreen(new BookScreen(BookScreen.Contents.create(translatedBook)));
            });
        }).start();
    }
}
