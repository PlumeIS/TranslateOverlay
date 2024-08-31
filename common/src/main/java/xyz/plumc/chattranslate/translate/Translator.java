package xyz.plumc.chattranslate.translate;

public enum Translator {
    BING("bing", "BingTranslator"),
    GOOGLE("google", "GoogleTranslator"),
    BAIDU("baidu", "BaiduTranslator");

    public final String platform;

    public final String name;
    Translator(String platform, String name){
        this.platform = platform;
        this.name = name;
    }

    public static Translator of(String platform){
        for (Translator translator: Translator.values()){
            if (translator.platform.equals(platform)){
                return translator;
            }
        }
        return Translator.BING;
    }

    public static Translator ofByName(String name){
        for (Translator translator: Translator.values()){
            if (translator.name.equals(name)){
                return translator;
            }
        }
        return Translator.BING;
    }
}
