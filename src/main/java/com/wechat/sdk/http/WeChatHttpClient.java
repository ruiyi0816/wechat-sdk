package com.wechat.sdk.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.wechat.sdk.config.WeChatConfig;
import com.wechat.sdk.exception.WeChatApiException;
import com.wechat.sdk.exception.WeChatException;
import com.wechat.sdk.http.HttpTransport.RawHttpRequest;
import com.wechat.sdk.http.HttpTransport.RawHttpResponse;
import com.wechat.sdk.token.AccessTokenManager;
import com.wechat.sdk.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于 JDK 21 {@link HttpClient} 的微信服务端 HTTP 客户端。
 * 支持自动注入 access_token、统一错误码解析、token 失效自动重试以及流式/分块文件上传下载。
 */
public class WeChatHttpClient {

    private static final Logger log = LoggerFactory.getLogger(WeChatHttpClient.class);
    private static final Pattern FILENAME_PATTERN = Pattern.compile("filename=\"?([^\";]+)\"?");

    private final WeChatConfig config;
    private final HttpTransport transport;
    private volatile HttpClient jdkHttpClient;
    private AccessTokenManager accessTokenManager;

    public WeChatHttpClient(WeChatConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.transport = this::executeWithJdkHttpClient;
    }

    public WeChatHttpClient(WeChatConfig config, HttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.jdkHttpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.transport = this::executeWithJdkHttpClient;
    }

    public WeChatHttpClient(WeChatConfig config, HttpTransport transport) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.transport = Objects.requireNonNull(transport, "transport must not be null");
    }

    private RawHttpResponse executeWithJdkHttpClient(RawHttpRequest rawReq) throws IOException, InterruptedException {
        HttpClient client = getOrCreateJdkHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder(rawReq.uri())
                .timeout(config.getRequestTimeout());
        if (rawReq.headers() != null) {
            rawReq.headers().forEach(builder::header);
        }
        if ("POST".equalsIgnoreCase(rawReq.method())) {
            byte[] payload = rawReq.body() != null ? rawReq.body() : new byte[0];
            builder.POST(HttpRequest.BodyPublishers.ofByteArray(payload));
        } else {
            builder.GET();
        }
        HttpResponse<byte[]> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        return new RawHttpResponse(response.statusCode(), response.headers().map(), response.body());
    }

    private HttpClient getOrCreateJdkHttpClient() {
        HttpClient client = this.jdkHttpClient;
        if (client == null) {
            synchronized (this) {
                client = this.jdkHttpClient;
                if (client == null) {
                    client = HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_2)
                            .connectTimeout(config.getConnectTimeout())
                            .followRedirects(HttpClient.Redirect.NORMAL)
                            .build();
                    this.jdkHttpClient = client;
                }
            }
        }
        return client;
    }

    public void setAccessTokenManager(AccessTokenManager accessTokenManager) {
        this.accessTokenManager = accessTokenManager;
    }

    public WeChatConfig getConfig() {
        return config;
    }

    public <T> T get(String path, Map<String, String> queryParams, Class<T> responseType) {
        return get(path, queryParams, responseType, true);
    }

    public <T> T get(String path, Map<String, String> queryParams, Class<T> responseType, boolean withAccessToken) {
        return executeWithTokenRetry(path, withAccessToken, token -> {
            URI uri = buildUri(config.getApiBaseUrl(), path, queryParams, token);
            RawHttpRequest request = new RawHttpRequest("GET", uri, Map.of("Accept", "application/json"), null);
            String responseBody = sendStringRequest(path, request);
            return parseAndVerifyResponse(path, responseBody, responseType);
        });
    }

    public <T> T postJson(String path, Map<String, String> queryParams, Object body, Class<T> responseType) {
        return postJson(path, queryParams, body, responseType, true);
    }

    public <T> T postJson(String path, Map<String, String> queryParams, Object body, Class<T> responseType, boolean withAccessToken) {
        return executeWithTokenRetry(path, withAccessToken, token -> {
            URI uri = buildUri(config.getApiBaseUrl(), path, queryParams, token);
            String jsonPayload = JsonUtils.toJson(body);
            RawHttpRequest request = new RawHttpRequest(
                    "POST",
                    uri,
                    Map.of("Content-Type", "application/json; charset=UTF-8", "Accept", "application/json"),
                    jsonPayload.getBytes(StandardCharsets.UTF_8)
            );
            String responseBody = sendStringRequest(path, request);
            return parseAndVerifyResponse(path, responseBody, responseType);
        });
    }

    public <T> T postRawBytes(String path, Map<String, String> queryParams, byte[] payload, String contentType, Class<T> responseType) {
        return executeWithTokenRetry(path, true, token -> {
            URI uri = buildUri(config.getApiBaseUrl(), path, queryParams, token);
            RawHttpRequest request = new RawHttpRequest(
                    "POST",
                    uri,
                    Map.of("Content-Type", contentType != null ? contentType : "application/octet-stream"),
                    payload
            );
            String responseBody = sendStringRequest(path, request);
            return parseAndVerifyResponse(path, responseBody, responseType);
        });
    }

    public <T> T postMultipart(
            String path,
            Map<String, String> queryParams,
            String fieldName,
            String fileName,
            byte[] fileBytes,
            String contentType,
            Map<String, String> extraFormFields,
            Class<T> responseType
    ) {
        return executeWithTokenRetry(path, true, token -> {
            URI uri = buildUri(config.getApiBaseUrl(), path, queryParams, token);
            String boundary = "----WeChatSdkBoundary" + UUID.randomUUID().toString().replace("-", "");
            byte[] multipartBody = buildMultipartBody(boundary, fieldName, fileName, fileBytes, contentType, extraFormFields);
            RawHttpRequest request = new RawHttpRequest(
                    "POST",
                    uri,
                    Map.of("Content-Type", "multipart/form-data; boundary=" + boundary, "Accept", "application/json"),
                    multipartBody
            );
            String responseBody = sendStringRequest(path, request);
            return parseAndVerifyResponse(path, responseBody, responseType);
        });
    }

    public MediaDownloadResult download(String path, Map<String, String> queryParams, Object optionalPostBody, boolean withAccessToken) {
        return downloadFromBase(config.getApiBaseUrl(), path, queryParams, optionalPostBody, withAccessToken);
    }

    public MediaDownloadResult downloadFromBase(
            String baseUrl,
            String path,
            Map<String, String> queryParams,
            Object optionalPostBody,
            boolean withAccessToken
    ) {
        return executeWithTokenRetry(path, withAccessToken, token -> {
            URI uri = buildUri(baseUrl, path, queryParams, token);
            RawHttpRequest request;
            if (optionalPostBody != null) {
                String jsonPayload = JsonUtils.toJson(optionalPostBody);
                request = new RawHttpRequest(
                        "POST",
                        uri,
                        Map.of("Content-Type", "application/json; charset=UTF-8"),
                        jsonPayload.getBytes(StandardCharsets.UTF_8)
                );
            } else {
                request = new RawHttpRequest("GET", uri, Map.of(), null);
            }

            try {
                RawHttpResponse response = transport.execute(request);
                verifyHttpStatus(path, response.statusCode());
                String contentType = response.firstHeader("Content-Type");
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                byte[] bodyBytes = response.body();

                // 检查是否返回了 JSON（例如错误响应或视频下载链接）
                if (contentType.contains("application/json") || contentType.contains("text/plain")
                        || looksLikeJsonObject(bodyBytes)) {
                    String text = new String(bodyBytes, StandardCharsets.UTF_8).trim();
                    if (text.startsWith("{") && text.endsWith("}")) {
                        JsonNode node = JsonUtils.readTree(text);
                        checkErrorNode(path, node);
                        if (node.has("video_url") || node.has("down_url")) {
                            String videoUrl = node.has("video_url")
                                    ? node.path("video_url").asText(null)
                                    : node.path("down_url").asText(null);
                            String title = node.path("title").asText(null);
                            String desc = node.path("description").asText(null);
                            return new MediaDownloadResult(bodyBytes, contentType, null, title, desc, videoUrl);
                        }
                    }
                }

                String disposition = response.firstHeader("Content-Disposition");
                String fileName = extractFileName(disposition);
                return new MediaDownloadResult(bodyBytes, contentType, fileName, null, null, null);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new WeChatException("HTTP download interrupted for " + path, e);
            } catch (IOException e) {
                throw new WeChatException("HTTP download failed for " + path, e);
            }
        });
    }

    private <T> T executeWithTokenRetry(String path, boolean withAccessToken, TokenAwareCallable<T> callable) {
        if (!withAccessToken) {
            return callable.call(null);
        }
        if (accessTokenManager == null) {
            throw new WeChatException("AccessTokenManager is not initialized on WeChatHttpClient");
        }
        int attempts = 0;
        int maxAttempts = config.isAutoRetryOnTokenExpired() ? Math.max(1, config.getMaxRetries()) + 1 : 1;
        while (true) {
            attempts++;
            String token = accessTokenManager.getAccessToken();
            try {
                return callable.call(token);
            } catch (WeChatApiException ex) {
                if (ex.isAccessTokenInvalid() && attempts < maxAttempts) {
                    log.warn("WeChat access_token invalid (errcode={}) on path {}, refreshing token and retrying (attempt {}/{})",
                            ex.getErrCode(), path, attempts, maxAttempts);
                    accessTokenManager.refreshAccessToken();
                    continue;
                }
                throw ex;
            }
        }
    }

    private String sendStringRequest(String path, RawHttpRequest request) {
        try {
            log.debug("Sending WeChat API {} request to {}", request.method(), request.uri());
            RawHttpResponse response = transport.execute(request);
            verifyHttpStatus(path, response.statusCode());
            return new String(response.body(), StandardCharsets.UTF_8);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WeChatException("HTTP request interrupted for " + path, e);
        } catch (IOException e) {
            throw new WeChatException("HTTP request failed for " + path, e);
        }
    }

    private void verifyHttpStatus(String path, int statusCode) {
        if (statusCode < 200 || statusCode >= 300) {
            throw new WeChatException("Unexpected HTTP status code " + statusCode + " for path " + path);
        }
    }

    private <T> T parseAndVerifyResponse(String path, String responseBody, Class<T> responseType) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new WeChatException("Empty response body from WeChat API for path " + path);
        }
        JsonNode root = JsonUtils.readTree(responseBody);
        checkErrorNode(path, root);
        if (responseType == String.class) {
            return responseType.cast(responseBody);
        }
        if (responseType == JsonNode.class) {
            return responseType.cast(root);
        }
        return JsonUtils.fromJson(responseBody, responseType);
    }

    private void checkErrorNode(String path, JsonNode root) {
        if (root != null && root.has("errcode")) {
            int errCode = root.path("errcode").asInt(0);
            if (errCode != 0) {
                String errMsg = root.path("errmsg").asText("");
                String rid = root.path("rid").asText(null);
                throw new WeChatApiException(errCode, errMsg, rid, path);
            }
        }
    }

    public URI buildUri(String baseUrl, String path, Map<String, String> queryParams, String accessToken) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        Map<String, String> mergedParams = new LinkedHashMap<>();
        if (accessToken != null && !accessToken.isBlank()) {
            mergedParams.put("access_token", accessToken);
        }
        if (queryParams != null) {
            mergedParams.putAll(queryParams);
        }
        StringBuilder sb = new StringBuilder(baseUrl).append(normalizedPath);
        if (!mergedParams.isEmpty()) {
            sb.append(normalizedPath.contains("?") ? "&" : "?");
            boolean first = true;
            for (Map.Entry<String, String> entry : mergedParams.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                if (!first) {
                    sb.append('&');
                }
                first = false;
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
        }
        return URI.create(sb.toString());
    }

    private static byte[] buildMultipartBody(
            String boundary,
            String fieldName,
            String fileName,
            byte[] fileBytes,
            String contentType,
            Map<String, String> extraFormFields
    ) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String crlf = "\r\n";

            out.write(("--" + boundary + crlf).getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"" + crlf)
                    .getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Type: " + (contentType != null ? contentType : "application/octet-stream") + crlf + crlf)
                    .getBytes(StandardCharsets.UTF_8));
            out.write(fileBytes);
            out.write(crlf.getBytes(StandardCharsets.UTF_8));

            if (extraFormFields != null) {
                for (Map.Entry<String, String> entry : extraFormFields.entrySet()) {
                    if (entry.getValue() == null) {
                        continue;
                    }
                    out.write(("--" + boundary + crlf).getBytes(StandardCharsets.UTF_8));
                    out.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + crlf + crlf)
                            .getBytes(StandardCharsets.UTF_8));
                    out.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                    out.write(crlf.getBytes(StandardCharsets.UTF_8));
                }
            }

            out.write(("--" + boundary + "--" + crlf).getBytes(StandardCharsets.UTF_8));
            return out.toByteArray();
        } catch (IOException e) {
            throw new WeChatException("Failed to build multipart body", e);
        }
    }

    private static boolean looksLikeJsonObject(byte[] bytes) {
        if (bytes == null || bytes.length < 2 || bytes.length > 65536) {
            return false;
        }
        int start = 0;
        while (start < bytes.length && Character.isWhitespace(bytes[start])) {
            start++;
        }
        return start < bytes.length && bytes[start] == '{';
    }

    private static String extractFileName(String contentDisposition) {
        if (contentDisposition == null || contentDisposition.isBlank()) {
            return null;
        }
        Matcher matcher = FILENAME_PATTERN.matcher(contentDisposition);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @FunctionalInterface
    private interface TokenAwareCallable<T> {
        T call(String accessToken);
    }
}
