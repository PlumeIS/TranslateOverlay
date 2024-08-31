package xyz.plumc.chattranslate.translates.ocr.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.texture.NativeImage;
import xyz.plumc.chattranslate.components.animation.Animation;
import xyz.plumc.chattranslate.components.animation.animas.LinearFunction;
import xyz.plumc.chattranslate.config.Config;
import xyz.plumc.chattranslate.translate.Language;
import net.minecraft.client.resource.language.I18n;
import xyz.plumc.chattranslate.translates.ocr.AnimationOCRResult;
import xyz.plumc.chattranslate.translates.ocr.OCRHud;
import xyz.plumc.chattranslate.translates.ocr.OCRResult;
import xyz.plumc.chattranslate.translates.ocr.RequestHelper;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static xyz.plumc.chattranslate.translate.translator.BaiduTranslator.LANGUAGE_MAPPING;

public class BaiduPT implements BaseOCR{
    public static final String API = "http://api.fanyi.baidu.com/api/trans/sdk/picture?from=%s&to=%s&appid=%s&salt=%s&sign=%s&cuid=APICUID&mac=mac";
    public static final String CUID = "APICUID";
    public static final String MAC = "mac";
    public Language language;

    @Override
    public List<AnimationOCRResult> OCR(NativeImage screenshot) {
        try {
            byte[] img = screenshot.getBytes();
            int salt = new Random().nextInt(10000);
            MessageDigest digest = MessageDigest.getInstance("MD5");

            digest.update(img);
            String imgMd5 = new BigInteger(1, digest.digest()).toString(16);
            digest.update((Config.getBaiduAPPID()+imgMd5+salt+CUID+MAC+Config.getBaiduToken()).getBytes(StandardCharsets.UTF_8));
            String sign = new BigInteger(1, digest.digest()).toString(16);

            URL url = new URL(API.formatted(LANGUAGE_MAPPING.get(language), LANGUAGE_MAPPING.get(Config.getLangTo()), Config.getBaiduAPPID(), salt, sign));
            OCRHud.instance.setInfo(I18n.translate("message.chattranslate.ocr.requesting"));
            JsonElement posted = RequestHelper.post(url.toString(), List.of(RequestHelper.RequestUtil.getFileFormData("image", "file.png", img, StandardCharsets.UTF_8)));

            if (posted!=null){
                JsonObject jsonObject = posted.getAsJsonObject();
                if (jsonObject.get("error_code").getAsInt() == 0) {
                    List<AnimationOCRResult> results = new ArrayList<>();
                    for (JsonElement result : jsonObject.getAsJsonObject("data").getAsJsonArray("content")) {
                        String src = result.getAsJsonObject().get("src").getAsString();
                        String dst = result.getAsJsonObject().get("dst").getAsString();
                        String rectRaw = result.getAsJsonObject().get("rect").getAsString();
                        List<Integer> rect = Arrays.stream(rectRaw.split(" ")).map(Integer::parseInt).toList();
                        results.add(new AnimationOCRResult(new Animation(0, 1, new LinearFunction(), Animation.getTime(1.5f)), null,
                                        new OCRResult(
                                                (float) rect.get(0) / screenshot.getWidth(),
                                                (float) rect.get(1) / screenshot.getHeight(),
                                                (float) rect.get(2) / screenshot.getWidth(),
                                                (float) rect.get(3) / screenshot.getHeight(),
                                                src,
                                                dst)
                                )
                        );
                    }
                    OCRHud.instance.clearInfo();
                    return results;
                }
                else {
                    OCRHud.instance.setInfo(I18n.translate("message.chattranslate.ocr.baidu_pt.fail", jsonObject.get("error_msg").getAsString()));
                }
            } else {
                OCRHud.instance.setInfo(I18n.translate("message.chattranslate.ocr.baidu_pt.fail", "Server request error"));
            }
            return new ArrayList<>();
        } catch (NoSuchAlgorithmException | IOException ignored) {
            throw new RuntimeException();
        }
    }

    public void init(Language language){
        this.language = language;
    }
}
