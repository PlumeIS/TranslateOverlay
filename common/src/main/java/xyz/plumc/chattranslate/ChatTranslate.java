package xyz.plumc.chattranslate;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.thread.GroupAssigningThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import xyz.plumc.chattranslate.config.ConfigManager;

public class ChatTranslate{
    public static final String MOD_ID = "chattranslate";

    public static GroupAssigningThreadFactory threadFactory = new GroupAssigningThreadFactory("ChatTranslate");
    public static Logger logger = LogManager.getLogger();
    public static KeyBinding translateKey =  new KeyBinding("key.translate" ,
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.chattranslate");

    public static KeyBinding OCRKey =  new KeyBinding("key.ocr_translate" ,
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.chattranslate");

    public static void init() {
        new ConfigManager();
        ConfigManager.instance.load();

    }
}
