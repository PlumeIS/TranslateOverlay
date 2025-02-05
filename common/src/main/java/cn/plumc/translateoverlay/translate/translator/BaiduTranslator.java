package cn.plumc.translateoverlay.translate.translator;

import cn.plumc.translateoverlay.translate.HandlerMethod;
import cn.plumc.translateoverlay.utils.HttpHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import cn.plumc.translateoverlay.translate.Language;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.*;
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
    public String translate(String message, String langFrom, String langTo) {
        int salt = new Random().nextInt(10000);
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update((APPID+message+salt+token).getBytes(StandardCharsets.UTF_8));
            String sign = new BigInteger(1, digest.digest()).toString(16);
            URL api = new URL(API_URL.formatted(message, langFrom, langTo, APPID, salt, sign));
            URLConnection connection = api.openConnection();
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
            return json.getAsJsonArray("trans_result").get(0).getAsJsonObject().get("dst").getAsString();
        } catch (NoSuchAlgorithmException ignored) {
        } catch (IOException e) {
            return e.getMessage();
        }
        throw new RuntimeException();
    }

    @Override
    public String getTranslatorName() {
        return "BaiduTranslator";
    }
}
