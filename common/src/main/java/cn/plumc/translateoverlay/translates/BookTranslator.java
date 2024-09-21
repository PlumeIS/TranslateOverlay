package cn.plumc.translateoverlay.translates;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.item.ItemStack;
import cn.plumc.translateoverlay.TranslateOverlay;
import cn.plumc.translateoverlay.utils.BookUtil;
import cn.plumc.translateoverlay.utils.TickUtil;

public class BookTranslator {
    public static void translate(BookScreen.Contents bookAccess){
        TranslateOverlay.threadFactory.newThread(()->{
            ItemStack translatedBook = BookUtil.getTranslatedBook(bookAccess);
            TickUtil.tickRun(()-> {
                MinecraftClient.getInstance().setScreen(new BookScreen(BookScreen.Contents.create(translatedBook)));
            });
        }).start();
    }
}
