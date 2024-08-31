package xyz.plumc.chattranslate.utils;

import com.google.common.base.Preconditions;

import java.awt.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public final class ChatColor {
    public static final char COLOR_CHAR = '§';
    public static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";
    public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf('§') + "[0-9A-FK-ORX]");
    private static final Map<Character, ChatColor> BY_CHAR = new HashMap();
    private static final Map<String, ChatColor> BY_NAME = new HashMap();
    public static final ChatColor BLACK = new ChatColor('0', "black", new Color(0));
    public static final ChatColor DARK_BLUE = new ChatColor('1', "dark_blue", new Color(170));
    public static final ChatColor DARK_GREEN = new ChatColor('2', "dark_green", new Color(43520));
    public static final ChatColor DARK_AQUA = new ChatColor('3', "dark_aqua", new Color(43690));
    public static final ChatColor DARK_RED = new ChatColor('4', "dark_red", new Color(11141120));
    public static final ChatColor DARK_PURPLE = new ChatColor('5', "dark_purple", new Color(11141290));
    public static final ChatColor GOLD = new ChatColor('6', "gold", new Color(16755200));
    public static final ChatColor GRAY = new ChatColor('7', "gray", new Color(11184810));
    public static final ChatColor DARK_GRAY = new ChatColor('8', "dark_gray", new Color(5592405));
    public static final ChatColor BLUE = new ChatColor('9', "blue", new Color(5592575));
    public static final ChatColor GREEN = new ChatColor('a', "green", new Color(5635925));
    public static final ChatColor AQUA = new ChatColor('b', "aqua", new Color(5636095));
    public static final ChatColor RED = new ChatColor('c', "red", new Color(16733525));
    public static final ChatColor LIGHT_PURPLE = new ChatColor('d', "light_purple", new Color(16733695));
    public static final ChatColor YELLOW = new ChatColor('e', "yellow", new Color(16777045));
    public static final ChatColor WHITE = new ChatColor('f', "white", new Color(16777215));
    public static final ChatColor MAGIC = new ChatColor('k', "obfuscated");
    public static final ChatColor BOLD = new ChatColor('l', "bold");
    public static final ChatColor STRIKETHROUGH = new ChatColor('m', "strikethrough");
    public static final ChatColor UNDERLINE = new ChatColor('n', "underline");
    public static final ChatColor ITALIC = new ChatColor('o', "italic");
    public static final ChatColor RESET = new ChatColor('r', "reset");
    private static int count = 0;
    private final String toString;
    private final String name;
    private final int ordinal;
    private final Color color;

    private ChatColor(char code, String name) {
        this(code, name, (Color)null);
    }

    private ChatColor(char code, String name, Color color) {
        this.name = name;
        this.toString = new String(new char[]{'§', code});
        this.ordinal = count++;
        this.color = color;
        BY_CHAR.put(code, this);
        BY_NAME.put(name.toUpperCase(Locale.ROOT), this);
    }

    private ChatColor(String name, String toString, int rgb) {
        this.name = name;
        this.toString = toString;
        this.ordinal = -1;
        this.color = new Color(rgb);
    }

    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.toString);
        return hash;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            ChatColor other = (ChatColor)obj;
            return Objects.equals(this.toString, other.toString);
        } else {
            return false;
        }
    }

    public String toString() {
        return this.toString;
    }

    public static String stripColor(String input) {
        return input == null ? null : STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    public static ChatColor getByChar(char code) {
        return (ChatColor)BY_CHAR.get(code);
    }

    public static ChatColor of(Color color) {
        return of("#" + String.format("%08x", color.getRGB()).substring(2));
    }

    public static ChatColor of(String string) {
        Preconditions.checkArgument(string != null, "string cannot be null");
        if (string.startsWith("#") && string.length() == 7) {
            int rgb;
            try {
                rgb = Integer.parseInt(string.substring(1), 16);
            } catch (NumberFormatException var7) {
                throw new IllegalArgumentException("Illegal hex string " + string);
            }

            StringBuilder magic = new StringBuilder("§x");
            char[] var3 = string.substring(1).toCharArray();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                char c = var3[var5];
                magic.append('§').append(c);
            }

            return new ChatColor(string, magic.toString(), rgb);
        } else {
            ChatColor defined = (ChatColor)BY_NAME.get(string.toUpperCase(Locale.ROOT));
            if (defined != null) {
                return defined;
            } else {
                throw new IllegalArgumentException("Could not parse ChatColor " + string);
            }
        }
    }

    /** @deprecated */
    @Deprecated
    public static ChatColor valueOf(String name) {
        Preconditions.checkNotNull(name, "Name is null");
        ChatColor defined = (ChatColor)BY_NAME.get(name);
        Preconditions.checkArgument(defined != null, "No enum constant " + ChatColor.class.getName() + "." + name);
        return defined;
    }

    /** @deprecated */
    @Deprecated
    public static ChatColor[] values() {
        return (ChatColor[])BY_CHAR.values().toArray(new ChatColor[0]);
    }

    /** @deprecated */
    @Deprecated
    public String name() {
        return this.getName().toUpperCase(Locale.ROOT);
    }

    /** @deprecated */
    @Deprecated
    public int ordinal() {
        Preconditions.checkArgument(this.ordinal >= 0, "Cannot get ordinal of hex color");
        return this.ordinal;
    }

    public String getName() {
        return this.name;
    }

    public Color getColor() {
        return this.color;
    }
}
