package cn.plumc.translateoverlay;

import cn.plumc.translateoverlay.config.ConfigManager;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.thread.GroupAssigningThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class TranslateOverlay{
    public static final String MOD_ID = "translateoverlay";

    public static GroupAssigningThreadFactory threadFactory = new GroupAssigningThreadFactory("TranslateOverlay");
    public static Logger logger = LogManager.getLogger();
    public static KeyBinding translateKey =  new KeyBinding("key.translate" ,
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.translateoverlay");

    public static KeyBinding OCRKey =  new KeyBinding("key.ocr_translate" ,
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.translateoverlay");

    public static void init() {
        new ConfigManager();
        ConfigManager.instance.load();

    }
}
