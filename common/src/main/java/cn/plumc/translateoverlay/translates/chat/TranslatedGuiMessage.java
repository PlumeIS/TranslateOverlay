package cn.plumc.translateoverlay.translates.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import cn.plumc.translateoverlay.components.animation.Animation;

import java.util.UUID;

@Environment(value= EnvType.CLIENT)
public record TranslatedGuiMessage(int addedTime, Text content, @Nullable MessageSignatureData signature, @Nullable MessageIndicator tag, UUID uuid) {
    @Nullable
    public MessageIndicator.Icon icon() {
        return this.tag != null ? this.tag.icon() : null;
    }

    @Nullable
    public MessageSignatureData signature() {
        return this.signature;
    }

    @Nullable
    public MessageIndicator tag() {
        return this.tag;
    }

    @Environment(value= EnvType.CLIENT)
    public record AnimaLine(int addedTime, OrderedText content, @Nullable MessageIndicator tag, boolean endOfEntry, Animation animationW, @Nullable Animation animationH, UUID uuid) {
        @Nullable
        public MessageIndicator tag() {
            return this.tag;
        }
    }
}