package cn.plumc.translateoverlay.translate.translator;

import cn.plumc.translateoverlay.utils.HttpHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import net.minecraft.text.Text;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import cn.plumc.translateoverlay.translate.HandlerMethod;
import cn.plumc.translateoverlay.translates.ChatTranslator;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BingTranslator extends Translator {
    public static final String INFO_URL = "https://cn.bing.com/search?q=translate";
    public static final String TRANSLATE_URL = "https://cn.bing.com/ttranslatev3?&IG=%s&IID=%s";

    public static final Pattern IGPattern = Pattern.compile("_IG=\"(.*?)\"");
    public static final Pattern IIDPattern = Pattern.compile("_iid=\"(.*?)\"");
    public static final Pattern tokenDataPattern = Pattern.compile("params_AbusePreventionHelper = \\[(.*?)\\];");
    public Map<String, String> cookies = new HashMap<>();

    public static final String ignores = " \n\t\r";

    private String IG;
    private String IID;
    private String key;
    private String token;
    private int maxAge;
    private long updateTime;

    private boolean updated = false;

    HttpHelper helper = new HttpHelper();


    public BingTranslator() {
        update();
    }

    @Override
    public String translate(String text, String langFrom, String langTo) throws IOException {
        if (isOverAge()) update();
        boolean ignore = true;
        for (char c : text.toCharArray()) {
            if (!ignores.contains(String.valueOf(c))){
                ignore = false;
                break;
            }
        }
        if (ignore) return text;
        Map<String, String> translateData = new HashMap<>();
        translateData.put("fromLang", langFrom);
        translateData.put("text", text);
        translateData.put("to", langTo);
        translateData.put("token", token);
        translateData.put("key", key);
        translateData.put("tryFetchingGenderDebiasedTranslations", "true");
        String response = helper.sendPost(TRANSLATE_URL.formatted(IG, IID), cookies, translateData);
        return readTranslated(response);
    }

    @Override
    public String getTranslatorName() {
        return "BingTranslator";
    }

    @Override
    public HandlerMethod getHandlerMethod() {
        return HandlerMethod.REJOIN_HOLDER;
    }

    public void update() {
        cookies.put("MUID", generateRandomToken());
        String infoPage = helper.sendGet(INFO_URL, cookies);

        Matcher IGMatcher = IGPattern.matcher(infoPage);
        Matcher IIDMatcher = IIDPattern.matcher(infoPage);
        Matcher tokenDataMatcher = tokenDataPattern.matcher(infoPage);

        if (IGMatcher.find() && IIDMatcher.find() && tokenDataMatcher.find()){
            IG = IGMatcher.group(0).substring(5, IGMatcher.group(0).length() - 2);
            IID = IIDMatcher.group(0).substring(6, IIDMatcher.group(0).length() - 2);

            String tokenDataString = tokenDataMatcher.group(0);
            tokenDataString = tokenDataString.substring(32, tokenDataString.length() - 2);
            String[] tokenData = tokenDataString.split(",");
            key = tokenData[0];
            token = tokenData[1].substring(1, tokenData[1].length() - 1);
            maxAge = Integer.parseInt(tokenData[2]);
            updateTime = System.currentTimeMillis();
            updated = true;
        } else {
            ChatTranslator.sendBypassMessage(Text.translatable("message.translateoverlay.translator.bing.update_error"));
        }
    }

    private static String generateRandomToken() {
        Random random = new Random();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private boolean isOverAge() {
        if (!updated) return true;
        return System.currentTimeMillis() - updateTime > maxAge;
    }

    private String readTranslated(String response) {
        Gson gson = new Gson();
        try {
            JsonArray jsonTranslated = gson.fromJson(response, JsonArray.class);
            return jsonTranslated.get(0).getAsJsonObject().getAsJsonArray("translations").get(0).getAsJsonObject().get("text").getAsString();
        } catch (JsonSyntaxException ignored) {
            ChatTranslator.sendBypassMessage(Text.translatable("message.translateoverlay.translator.bing.response_error"));
            return null;
        } catch (NullPointerException ignored){
            this.update();
            return null;
        }
    }
}

