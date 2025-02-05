package cn.plumc.translateoverlay.translate.translator;

import cn.plumc.translateoverlay.translate.HandlerMethod;
import cn.plumc.translateoverlay.translate.Language;
import cn.plumc.translateoverlay.utils.HttpHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BaiduTranslator extends Translator{
    public final String API_URL = "http://api.fanyi.baidu.com/api/trans/vip/translate?q=%s&from=%s&to=%s&appid=%s&salt=%s&sign=%s";
    public static Map<Language, String> LANGUAGE_MAPPING = new HashMap<>();
    static {
        LANGUAGE_MAPPING.put(Language.AUTO_DETECT, "auto");
        LANGUAGE_MAPPING.put(Language.CHINESE_SIMPLIFIED, "zh");
        LANGUAGE_MAPPING.put(Language.JAPANESE, "jp");
        LANGUAGE_MAPPING.put(Language.KOREAN, "kor");
        LANGUAGE_MAPPING.put(Language.FRENCH, "fra");
        LANGUAGE_MAPPING.put(Language.SPANISH, "spa");
        LANGUAGE_MAPPING.put(Language.ARABIC, "ara");
        LANGUAGE_MAPPING.put(Language.BULGARIAN, "bul");
        LANGUAGE_MAPPING.put(Language.ESTONIAN, "est");
        LANGUAGE_MAPPING.put(Language.DANISH, "dan");
        LANGUAGE_MAPPING.put(Language.FINNISH, "fin");
        LANGUAGE_MAPPING.put(Language.ROMANIAN, "rom");
        LANGUAGE_MAPPING.put(Language.SLOVENIAN, "slo");
        LANGUAGE_MAPPING.put(Language.SWEDISH, "swe");
        LANGUAGE_MAPPING.put(Language.CHINESE_TRADITIONAL, "cht");
        LANGUAGE_MAPPING.put(Language.VIETNAMESE, "vie");
    }
    private final String APPID;
    private final String token;

    HttpHelper httpHelper = new HttpHelper();

    public BaiduTranslator(String APPID, String token){
        this.APPID = APPID;
        this.token = token;
    }

    @Override
    public String translate(String message, String langFrom, String langTo) throws IOException {
        return delay(1000, ()->{
            try {
                int salt = new Random().nextInt(0, 10000);
                MessageDigest digest = MessageDigest.getInstance("MD5");
                digest.update((APPID+message+salt+token).getBytes(StandardCharsets.UTF_8));
                String sign = new BigInteger(1, digest.digest()).toString(16);

                String path = API_URL.formatted(HttpHelper.valid(message), langFrom, langTo, APPID, salt, sign);
                String response = httpHelper.sendGet(path, null);
                JsonObject result = JsonParser.parseString(response).getAsJsonObject();

                return result.get("trans_result").getAsJsonArray().get(0).getAsJsonObject().get("dst").getAsString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public HandlerMethod getHandlerMethod() {
        return HandlerMethod.REJOIN_HOLDER;
    }

    @Override
    public Map<Language, String> getLanguageMapping() {
        return LANGUAGE_MAPPING;
    }

    @Override
    public String getTranslatorName() {
        return "BaiduTranslator";
    }
}
