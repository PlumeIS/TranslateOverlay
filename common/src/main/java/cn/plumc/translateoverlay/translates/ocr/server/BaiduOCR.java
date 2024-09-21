package cn.plumc.translateoverlay.translates.ocr.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.resource.language.I18n;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import cn.plumc.translateoverlay.components.animation.Animation;
import cn.plumc.translateoverlay.components.animation.animas.LinearFunction;
import cn.plumc.translateoverlay.config.Config;
import cn.plumc.translateoverlay.translate.Language;
import cn.plumc.translateoverlay.translates.ocr.AnimationOCRResult;
import cn.plumc.translateoverlay.translates.ocr.OCRHud;
import cn.plumc.translateoverlay.translates.ocr.OCRResult;
import cn.plumc.translateoverlay.translates.ocr.RequestHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class BaiduOCR implements BaseOCR{
    public final static String URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general?access_token=%s";
    private boolean initialized = false;
    private Language language;
    private String accessToken;
    public List<AnimationOCRResult> OCR(NativeImage screenshot){
        try {
            byte[] screenshotByteArray = screenshot.getBytes();
            screenshot.close();
            String screenshotBase64 = Base64.getEncoder().encodeToString(screenshotByteArray);

            List<NameValuePair> param = new ArrayList<>();
            param.add(new BasicNameValuePair("image",screenshotBase64));
            param.add(new BasicNameValuePair("language_type", "ENG"));
            param.add(new BasicNameValuePair("probability","true"));
            JsonElement response = RequestHelper.post(URL.formatted(accessToken), new UrlEncodedFormEntity(param, "UTF-8"));
            JsonArray wordsResult = response.getAsJsonObject().get("words_result").getAsJsonArray();
            List<AnimationOCRResult> ocrResults = new ArrayList<>();
            OCRHud.instance.setInfo(I18n.translate("message.translateoverlay.ocr.translating"));
            for (JsonElement resultsElement: wordsResult.asList()){
                JsonObject results = resultsElement.getAsJsonObject();
                float probability = results.get("probability").getAsJsonObject().get("average").getAsFloat();
                if (probability < .8f) continue;
                String words = results.get("words").getAsString();
                JsonObject location = results.get("location").getAsJsonObject();
                int top = location.get("top").getAsInt();
                int left = location.get("left").getAsInt();
                int width = location.get("width").getAsInt();
                int height = location.get("height").getAsInt();
                String result = Config.getTranslator().translate(words);
                ocrResults.add(new AnimationOCRResult(new Animation(0, 1, new LinearFunction(), Animation.getTime(1.5f)), null,
                        new OCRResult(
                        (float) left /screenshot.getWidth(),
                        (float) top /screenshot.getHeight(),
                        (float) width /screenshot.getWidth(),
                        (float) height /screenshot.getHeight(),
                        words,
                        result)
                        )
                );
            }
            return ocrResults;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void init(Language language){
        if (!initialized) {
            JsonElement jsonElement = RequestHelper.post("https://aip.baidubce.com/oauth/2.0/token?client_id=%s&client_secret=%s&grant_type=client_credentials".formatted(Config.getBaiduAPIKey(), Config.getBaiduSecretKey()), null, "application/json");
            accessToken = jsonElement.getAsJsonObject().get("access_token").getAsString();
        }
        initialized = true;
        this.language = language;
    }
}
