package com.wechat.sdk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.wechat.sdk.http.MediaDownloadResult;
import com.wechat.sdk.http.WeChatHttpClient;
import com.wechat.sdk.model.account.QrCodeTicket;
import com.wechat.sdk.model.account.ShortenFetchResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * 账号管理接口服务（带参数二维码生成与下载、短Key托管）
 * 对应官方文档：账号管理 / 生成带参数的二维码 / 短key托管
 */
public class AccountApiService {

    private final WeChatHttpClient httpClient;

    public AccountApiService(WeChatHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    /**
     * 创建整型场景值临时二维码 (QR_SCENE)
     *
     * @param sceneId       场景值 ID（32位非0整型）
     * @param expireSeconds 二维码有效时间（秒），最大不超过 2592000（30天）
     */
    public QrCodeTicket createTempQrCode(int sceneId, int expireSeconds) {
        Map<String, Object> body = Map.of(
                "expire_seconds", expireSeconds,
                "action_name", "QR_SCENE",
                "action_info", Map.of("scene", Map.of("scene_id", sceneId))
        );
        return httpClient.postJson("/cgi-bin/qrcode/create", null, body, QrCodeTicket.class);
    }

    /**
     * 创建字符串场景值临时二维码 (QR_STR_SCENE)
     *
     * @param sceneStr      场景值 ID（字符串形式，长度限制为1到64）
     * @param expireSeconds 二维码有效时间（秒），最大不超过 2592000（30天）
     */
    public QrCodeTicket createTempQrCode(String sceneStr, int expireSeconds) {
        Map<String, Object> body = Map.of(
                "expire_seconds", expireSeconds,
                "action_name", "QR_STR_SCENE",
                "action_info", Map.of("scene", Map.of("scene_str", Objects.requireNonNull(sceneStr)))
        );
        return httpClient.postJson("/cgi-bin/qrcode/create", null, body, QrCodeTicket.class);
    }

    /**
     * 创建整型场景值永久二维码 (QR_LIMIT_SCENE)
     *
     * @param sceneId 场景值 ID（1~100000）
     */
    public QrCodeTicket createPermanentQrCode(int sceneId) {
        Map<String, Object> body = Map.of(
                "action_name", "QR_LIMIT_SCENE",
                "action_info", Map.of("scene", Map.of("scene_id", sceneId))
        );
        return httpClient.postJson("/cgi-bin/qrcode/create", null, body, QrCodeTicket.class);
    }

    /**
     * 创建字符串场景值永久二维码 (QR_LIMIT_STR_SCENE)
     *
     * @param sceneStr 场景值 ID（字符串形式，长度限制为1到64）
     */
    public QrCodeTicket createPermanentQrCode(String sceneStr) {
        Map<String, Object> body = Map.of(
                "action_name", "QR_LIMIT_STR_SCENE",
                "action_info", Map.of("scene", Map.of("scene_str", Objects.requireNonNull(sceneStr)))
        );
        return httpClient.postJson("/cgi-bin/qrcode/create", null, body, QrCodeTicket.class);
    }

    /**
     * 通过 ticket 构造二维码图片展示 URL
     */
    public String getQrCodeImageUrl(String ticket) {
        Objects.requireNonNull(ticket, "ticket must not be null");
        String encoded = URLEncoder.encode(ticket, StandardCharsets.UTF_8);
        return httpClient.getConfig().getMpBaseUrl() + "/cgi-bin/showqrcode?ticket=" + encoded;
    }

    /**
     * 通过 ticket 下载二维码图片字节数组 (/cgi-bin/showqrcode)
     */
    public byte[] downloadQrCodeImage(String ticket) {
        Objects.requireNonNull(ticket, "ticket must not be null");
        MediaDownloadResult result = httpClient.downloadFromBase(
                httpClient.getConfig().getMpBaseUrl(),
                "/cgi-bin/showqrcode",
                Map.of("ticket", ticket),
                null,
                false
        );
        return result.data();
    }

    /**
     * 短 key 托管 - 生成短 key (/cgi-bin/shorten/gen)
     *
     * @param longData      需要转换的长信息，不超过 4KB
     * @param expireSeconds 过期秒数，最大值为 2592000（即 30 天）
     * @return 生成的 short_key
     */
    public String generateShortKey(String longData, int expireSeconds) {
        Map<String, Object> body = Map.of(
                "long_data", Objects.requireNonNull(longData),
                "expire_seconds", expireSeconds
        );
        JsonNode node = httpClient.postJson("/cgi-bin/shorten/gen", null, body, JsonNode.class);
        return node.path("short_key").asText();
    }

    /**
     * 短 key 托管 - 还原长信息 (/cgi-bin/shorten/fetch)
     */
    public ShortenFetchResponse fetchLongData(String shortKey) {
        return httpClient.postJson(
                "/cgi-bin/shorten/fetch",
                null,
                Map.of("short_key", Objects.requireNonNull(shortKey)),
                ShortenFetchResponse.class
        );
    }
}
