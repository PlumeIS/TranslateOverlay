package xyz.plumc.chattranslate.translates.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import xyz.plumc.chattranslate.components.animation.Animation;

import java.util.UUID;

@Environment(value= EnvType.CLIENT)
public record TranslatedGuiMessage(int addedTime, Text content, UUID uuid) {
    @Environment(value= EnvType.CLIENT)
    public record AnimaLine(int addedTime, OrderedText content, boolean endOfEntry, Animation animationW, @Nullable Animation animationH, UUID uuid) {
    }
}