package xyz.plumc.chattranslate.config;

import xyz.plumc.chattranslate.translate.Language;
import xyz.plumc.chattranslate.translate.Translator;
import xyz.plumc.chattranslate.translate.translator.BaiduTranslator;
import xyz.plumc.chattranslate.translate.translator.BingTranslator;
import xyz.plumc.chattranslate.translates.ocr.OCR;
import xyz.plumc.chattranslate.translates.ocr.server.BaiduOCR;
import xyz.plumc.chattranslate.translates.ocr.server.BaiduPT;
import xyz.plumc.chattranslate.translates.ocr.server.BaseOCR;
import xyz.plumc.chattranslate.translates.ocr.server.LocalOCR;

import static xyz.plumc.chattranslate.config.ConfigFile.*;


public class Config {
    private static xyz.plumc.chattranslate.translate.translator.Translator translator;
    private static BaseOCR ocr;

    public static void load(){
        setupTranslator(Translator.of(ConfigFile.translator));
        setupOCR(OCR.getOCR(ocrName));
    }

    private static void setupTranslator(Translator translate){
        if (translate == Translator.BING) translator = new BingTranslator();
        if (translate == Translator.GOOGLE) translator = null;
        if (translate == Translator.BAIDU) translator = new BaiduTranslator(getBaiduAPPID(), getBaiduToken());
    }

    private static void setupOCR(OCR ocrName){
        if (ocrName == OCR.BAIDU) ocr = new BaiduOCR();
        if (ocrName == OCR.LOCAL) ocr = new LocalOCR();
        if (ocrName == OCR.BAIDU_PT) ocr = new BaiduPT();
        try {
            ocr.init(Language.of(langFrom));
        } catch (Exception ignored) {}
    }

    public static Language getLangFrom() {
        return Language.of(langFrom);
    }
    public static Language getLangTo() {
        return Language.of(langTo);
    }
    public static void setLangFrom(Language langFrom) {
        ConfigFile.langFrom = langFrom.code;
    }
    public static void setLangTo(Language langTo) {
        ConfigFile.langTo = langTo.code;
    }

    public static void setTranslator(Translator translator) {
        ConfigFile.translator = translator.name;
        setupTranslator(translator);
    }

    public static void setOCR(OCR ocr) {
        setupOCR(ocr);
        ocrName = ocr.name;
    }

    public static void setToggle(boolean toggle) {
        ConfigFile.toggle = toggle;
    }

    public static boolean getToggle(){
        return toggle;
    }
    public static String getBaiduAPPID(){
        return baiduAPPID;
    }
    public static String getBaiduToken(){
        return baiduToken;
    }
    public static String getBaiduAPIKey(){
        return baiduAPIKey;
    }
    public static String getBaiduSecretKey(){
        return baiduSecretKey;
    }
    public static String getRPCProtocol(){
        return RPCProtocol;
    }
    public static String getRPCHost(){
        return RPCHost;
    }
    public static int getRPCPort(){
        return RPCPort;
    }
    public static String getRPCToken(){
        return RPCToken;
    }

    public static xyz.plumc.chattranslate.translate.translator.Translator getTranslator(){
        return translator;
    }
    public static BaseOCR getOCR(){
        return ocr;
    }
}