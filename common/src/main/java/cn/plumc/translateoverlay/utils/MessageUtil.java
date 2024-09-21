package cn.plumc.translateoverlay.utils;

import net.minecraft.text.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MessageUtil {
    public static String serializeMutableComponent(Text rawComponent, @Nullable String holder) {
        return serializeMutableComponent(rawComponent.withoutStyle(), holder);
    }

    public static String serializeMutableComponent(List<Text> components, @Nullable String holder) {
        StringBuilder message = new StringBuilder();
        String last = null;
        for (Text component : components) {
            String string = component.getString();
            Style style = component.getStyle();
            String parsedStyle = parseStyleWithHolder(style, last, holder);
            if (!(parsedStyle.isEmpty())) last = parsedStyle;
            message.append(parsedStyle).append(string);
        }
        return message.toString();
    }

    public static String parseStyleWithHolder(Style style, @Nullable String last, @Nullable String holder) {
        holder = (holder == null ? "" : holder);
        StringBuilder stringBuilder = new StringBuilder(holder);
        TextColor textColor = style.getColor();
        if (!(textColor == null)) {
            String color = textColor.getName();
            switch (color) {
                case "black":
                    stringBuilder.append("§0");
                    break;
                case "dark_blue":
                    stringBuilder.append("§1");
                    break;
                case "dark_green":
                    stringBuilder.append("§2");
                    break;
                case "dark_aqua":
                    stringBuilder.append("§3");
                    break;
                case "dark_red":
                    stringBuilder.append("§4");
                    break;
                case "dark_purple":
                    stringBuilder.append("§5");
                    break;
                case "gold":
                    stringBuilder.append("§6");
                    break;
                case "gray":
                    stringBuilder.append("§7");
                    break;
                case "dark_gray":
                    stringBuilder.append("§8");
                    break;
                case "blue":
                    stringBuilder.append("§9");
                    break;
                case "green":
                    stringBuilder.append("§a");
                    break;
                case "aqua":
                    stringBuilder.append("§b");
                    break;
                case "red":
                    stringBuilder.append("§c");
                    break;
                case "light_purple":
                    stringBuilder.append("§d");
                    break;
                case "yellow":
                    stringBuilder.append("§e");
                    break;
                case "white":
                    stringBuilder.append("§f");
                    break;
                default:
                    stringBuilder.append("§x");
                    for (char c : color.substring(1).toCharArray()) {
                        stringBuilder.append("§");
                        stringBuilder.append(c);
                    }
                    break;
            }
        }
        if (style.isBold()) stringBuilder.append("§l");
        if (style.isItalic()) stringBuilder.append("§o");
        if (style.isStrikethrough()) stringBuilder.append("§m");
        if (style.isUnderlined()) stringBuilder.append("§n");
        if (style.isObfuscated()) stringBuilder.append("§k");

        if (last != null) {
            if (last.equals(stringBuilder + holder)) return "";
            if (style.isEmpty()) stringBuilder.append("§r");
        }
        if (stringBuilder.toString().equals(holder)) {
            return "";
        }
        return stringBuilder.append(holder).toString();
    }


    public static MutableText parseComponent(String message, ChatColor defaultColor) {
        MutableText components = Text.empty();
        StringBuilder builder = new StringBuilder();
        MutableText component = Text.empty();

        for (int i = 0; i < message.length(); ++i) {
            char c = message.charAt(i);
            if (c == 167) {
                ++i;
                if (i >= message.length()) {
                    break;
                }

                c = message.charAt(i);
                if (c >= 'A' && c <= 'Z') {
                    c = (char) (c + 32);
                }

                ChatColor format;
                if (c == 'x' && i + 12 < message.length()) {
                    StringBuilder hex = new StringBuilder("#");

                    for (int j = 0; j < 6; ++j) {
                        hex.append(message.charAt(i + 2 + j * 2));
                    }

                    try {
                        format = ChatColor.of(hex.toString());
                    } catch (IllegalArgumentException var11) {
                        format = null;
                    }

                    i += 12;
                } else {
                    format = ChatColor.getByChar(c);
                }

                if (format != null) {
                    if (format == ChatColor.BOLD) {
                        component.setStyle(component.getStyle().withBold(true));
                    } else if (format == ChatColor.ITALIC) {
                        component.setStyle(component.getStyle().withItalic(true));
                    } else if (format == ChatColor.UNDERLINE) {
                        component.setStyle(component.getStyle().withUnderline(true));
                    } else if (format == ChatColor.STRIKETHROUGH) {
                        component.setStyle(component.getStyle().withStrikethrough(true));
                    } else if (format == ChatColor.MAGIC) {
                        component.setStyle(component.getStyle().withObfuscated(true));
                    } else {
                        if (format == ChatColor.RESET) {
                            format = defaultColor;
                        }

                        component.content = new LiteralTextContent(builder.toString());
                        components.append(component);
                        builder = new StringBuilder();

                        component = Text.empty();
                        component.setStyle(component.getStyle().withColor(format.getColor().getRGB()));
                    }
                }
            } else {
                builder.append(c);
            }
        }

        component.content = new LiteralTextContent(builder.toString());
        components.append(component);
        return components;
    }
}
