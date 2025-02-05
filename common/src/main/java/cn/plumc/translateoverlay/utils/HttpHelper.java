package cn.plumc.translateoverlay.utils;

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
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class HttpHelper {
    private final CloseableHttpClient httpClient;
    private static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36 Edg/115.0.1901.183";
    private static final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(5000)
            .setConnectionRequestTimeout(5000)
            .setSocketTimeout(5000)
            .setRedirectsEnabled(true).build();

    public HttpHelper() {
        httpClient = HttpClients.createDefault();
    }

    public String sendGet(String uri, @Nullable Map<String, String> cookies) {
        HttpGet httpGet = new HttpGet(uri);
        if (Objects.nonNull(cookies)) {
            httpGet.setHeader(HttpHeaders.USER_AGENT, userAgent);
            httpGet.setHeader("Cookie", cookies.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("; ")));
        }
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(httpGet);
            return readResponse(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            httpGet.releaseConnection();
        }
    }

    public String sendPost(String url, Map<String, String> cookies, Map<String, String> data) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(HttpHeaders.USER_AGENT, userAgent);
        httpPost.setHeader("Cookie", cookies.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("; ")));
        httpPost.setConfig(requestConfig);
        CloseableHttpResponse response;
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(data.entrySet().stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())).collect(Collectors.toList()), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost);
            return readResponse(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            httpPost.releaseConnection();
        }
    }

    private String readResponse(HttpEntity entity) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        reader.lines().forEach((line) -> builder.append(line).append("\n"));
        return builder.toString();
    }

    public static String valid(String value){
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
