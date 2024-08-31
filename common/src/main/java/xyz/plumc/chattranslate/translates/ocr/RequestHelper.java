package xyz.plumc.chattranslate.translates.ocr;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;
import xyz.plumc.chattranslate.translates.ChatTranslator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class RequestHelper {
    public static class RequestUtil{
        public interface FormData{
            void apply(ByteArrayOutputStream stream, String boundary, boolean end) throws IOException;
        }
        public record FileFormData(String name, String fileName, String type, byte[] file, Charset charset) implements FormData{
            public void apply(ByteArrayOutputStream stream, String boundary, boolean end) throws IOException {
                stream.write("""
                        --%s
                        Content-Disposition: form-data; name="%s"; filename="%s"
                        Content-Type: %s
                        
                        """.formatted(boundary, name, fileName, type).getBytes(charset));
                stream.write(file);
                stream.write("\r\n".getBytes(charset));
                if (end) stream.write(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
            }
        }
        public record TextFormData(String name, String text, Charset charset) implements FormData{
            public void apply(ByteArrayOutputStream stream, String boundary, boolean end) throws IOException {
                stream.write("""
                        --%s
                        Content-Disposition: form-data; name="%s"
                        
                        %s
                        """.formatted(boundary, name, text).getBytes(charset));
                if (end) stream.write(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
            }
        }
        public static FileFormData getFileFormData(String formName, String filename, byte[] file, Charset charset){
            String contentType = "application/octet-stream";
            if (filename.endsWith(".png")) {
                contentType = "image/png";
            }else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".jpe")) {
                contentType = "image/jpeg";
            }else if (filename.endsWith(".gif")) {
                contentType = "image/gif";
            }else if (filename.endsWith(".ico")) {
                contentType = "image/image/x-icon";
            }
            return new FileFormData(formName, filename, contentType, file, charset);
        }
        public static TextFormData getTextFormData(String formName, String text, Charset charset){
            return new TextFormData(formName, text, charset);
        }
    }
    public static JsonElement post(String url, UrlEncodedFormEntity entity) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost();
            URI uri = new URI(url);
            httpPost.setURI(uri);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
            httpPost.setConfig(requestConfig);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(entity);
            return getResponse(httpClient.execute(httpPost));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static JsonElement post(String url, List<? extends RequestUtil.FormData> form) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost();
            URI uri = new URI(url);
            httpPost.setURI(uri);
            String boundary = UUID.randomUUID().toString().replaceAll("-", "");
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
            httpPost.setConfig(requestConfig);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (RequestUtil.FormData formData : form) {
                formData.apply(out, boundary, form.indexOf(formData) == form.size() - 1);
            }
            BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
            basicHttpEntity.setContent(new ByteArrayInputStream(out.toByteArray()));
            httpPost.setEntity(basicHttpEntity);
            return getResponse(httpClient.execute(httpPost));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonElement post(String url, JsonObject params, String contentType) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            Gson gson = new Gson();
            HttpPost httpPost = new HttpPost();
            URI uri = new URI(url);
            httpPost.setURI(uri);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
            httpPost.setConfig(requestConfig);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", contentType);
            httpPost.setEntity(new StringEntity(gson.toJson(params), "UTF-8"));
            return getResponse(httpClient.execute(httpPost));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonElement get(String url, String contentType){
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet();
            URI uri = new URI(url);
            httpGet.setURI(uri);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).build();
            httpGet.setConfig(requestConfig);

            httpGet.setHeader("Content-Type", contentType);
            return getResponse(httpClient.execute(httpGet));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private static JsonElement getResponse(CloseableHttpResponse execute) throws IOException {
        int statusCode = execute.getStatusLine().getStatusCode();
        if (200 == statusCode) {
            String result = EntityUtils.toString(execute.getEntity());
            return new JsonParser().parse(result);
        } else {
            if (MinecraftClient.getInstance().player != null)
                ChatTranslator.sendBypassMessage(new TranslatableText("message.chattranslate.ocr.request.failure"));
            return null;
        }
    }
}
