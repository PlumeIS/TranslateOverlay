package cn.plumc.translateoverlay.translates.ocr.server;

import cn.plumc.translateoverlay.components.animation.Animation;
import cn.plumc.translateoverlay.components.animation.animas.LinearFunction;
import cn.plumc.translateoverlay.config.Config;
import cn.plumc.translateoverlay.translate.Language;
import cn.plumc.translateoverlay.translates.ChatTranslator;
import cn.plumc.translateoverlay.translates.ocr.AnimationOCRResult;
import cn.plumc.translateoverlay.translates.ocr.OCRHud;
import cn.plumc.translateoverlay.translates.ocr.OCRResult;
import cn.plumc.translateoverlay.translates.ocr.RequestHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.Text;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class  LocalOCR implements BaseOCR {
    private static final String api = "%s://%s:%s/".formatted(Config.getRPCProtocol(), Config.getRPCHost(), Config.getRPCPort());

    @Override
    public List<AnimationOCRResult> OCR(NativeImage screenshot) {
        try {
            byte[] screenshotByteArray = screenshot.getBytes();
            screenshot.close();
            String screenshotBase64 = Base64.getEncoder().encodeToString(screenshotByteArray);
            JsonObject json = new JsonObject();
            json.addProperty("token", Config.getRPCToken());
            json.addProperty("image", screenshotBase64);
            OCRHud.instance.setInfo(I18n.translate("message.translateoverlay.ocr.requesting"));
            JsonElement posted = RequestHelper.post(api + "ocr", json, ContentType.APPLICATION_JSON.getMimeType());
            if (posted == null) {
                ChatTranslator.sendBypassMessage(Text.translatable("message.translateoverlay.ocr.request.failure", "远程错误"));
                return new ArrayList<>();
            }
            JsonObject jsonObject = posted.getAsJsonObject();
            if (!jsonObject.get("success").getAsBoolean()) {
                ChatTranslator.sendBypassMessage(Text.translatable("message.translateoverlay.ocr.request.failure", jsonObject.get("message").getAsString()));
                return new ArrayList<>();
            }
            List<AnimationOCRResult> ocrResults = new ArrayList<>();
            OCRHud.instance.setInfo(I18n.translate("message.translateoverlay.ocr.translating"));
            for (JsonElement resultsElement: jsonObject.getAsJsonObject("data").getAsJsonArray("result").asList()) {
                JsonObject results = resultsElement.getAsJsonObject();
                float confidence = results.get("confidence").getAsFloat();
                if (confidence < .8f) continue;
                String text = results.get("text").getAsString();
                JsonObject box = results.get("box").getAsJsonObject();
                JsonArray start = box.getAsJsonArray("start");
                JsonArray end = box.getAsJsonArray("end");
                int left = start.get(0).getAsInt();
                int top = start.get(1).getAsInt();
                int width = end.get(0).getAsInt() - left;
                int height = end.get(1).getAsInt() - top;
                String result = Config.getTranslator().translate(text);
                ocrResults.add(new AnimationOCRResult(new Animation(0, 1, new LinearFunction(), Animation.getTime(1.5f)), null,
                                new OCRResult(
                                        (float) left / screenshot.getWidth(),
                                        (float) top / screenshot.getHeight(),
                                        (float) width / screenshot.getWidth(),
                                        (float) height / screenshot.getHeight(),
                                        text,
                                        result)
                        )
                );
            }
                return ocrResults;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init(Language language) {
        JsonObject json = new JsonObject();
        json.addProperty("token", Config.getRPCToken());
        json.addProperty("language", language.code.replace("-", "_").replace(" ", ""));
        RequestHelper.post(api+"init", json, ContentType.APPLICATION_JSON.getMimeType());
    }
}
