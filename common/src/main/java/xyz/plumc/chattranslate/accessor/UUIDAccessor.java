package xyz.plumc.chattranslate.accessor;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.UUID;

public class UUIDAccessor {
    public static UUID getUUID(ChatHudLine<?> line){
        try {
            Field uuid = line.getClass().getDeclaredField("uuid");
            uuid.setAccessible(true);
            return (UUID) uuid.get(line);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setUUID(ChatHudLine<?> line, UUID value){
        try {
            Field uuid = line.getClass().getDeclaredField("uuid");
            uuid.setAccessible(true);
            uuid.set(line, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
