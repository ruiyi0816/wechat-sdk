package com.wechat.sdk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.wechat.sdk.exception.WeChatException;
import com.wechat.sdk.http.MediaDownloadResult;
import com.wechat.sdk.http.WeChatHttpClient;
import com.wechat.sdk.model.WeChatResponse;
import com.wechat.sdk.model.material.MaterialCountResponse;
import com.wechat.sdk.model.material.MediaUploadResponse;
import com.wechat.sdk.util.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * 素材管理接口服务（临时素材与永久素材）
 * 对应官方文档：素材管理
 */
public class MaterialApiService {

    private final WeChatHttpClient httpClient;

    public MaterialApiService(WeChatHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    // ==================== 1. 临时素材 ====================

    /**
     * 新增临时素材 (/cgi-bin/media/upload)
     *
     * @param type        媒体文件类型：image / voice / video / thumb
     * @param fileName    文件名
     * @param fileBytes   文件字节数组
     * @param contentType MIME 类型
     */
    public MediaUploadResponse uploadTempMedia(String type, String fileName, byte[] fileBytes, String contentType) {
        return httpClient.postMultipart(
                "/cgi-bin/media/upload",
                Map.of("type", Objects.requireNonNull(type)),
                "media",
                Objects.requireNonNull(fileName),
                Objects.requireNonNull(fileBytes),
                contentType,
                null,
                MediaUploadResponse.class
        );
    }

    /**
     * 新增临时素材（从本地文件路径上传）
     */
    public MediaUploadResponse uploadTempMedia(String type, Path filePath) {
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            return uploadTempMedia(type, filePath.getFileName().toString(), bytes, contentType);
        } catch (IOException e) {
            throw new WeChatException("Failed to read local file for temp media upload: " + filePath, e);
        }
    }

    /**
     * 获取临时素材 (/cgi-bin/media/get)
     */
    public MediaDownloadResult getTempMedia(String mediaId) {
        return httpClient.download("/cgi-bin/media/get", Map.of("media_id", Objects.requireNonNull(mediaId)), null, true);
    }

    /**
     * 获取高清语音素材 (/cgi-bin/media/get/jssdk)
     */
    public MediaDownloadResult getHqVoiceMedia(String mediaId) {
        return httpClient.download("/cgi-bin/media/get/jssdk", Map.of("media_id", Objects.requireNonNull(mediaId)), null, true);
    }

    // ==================== 2. 永久素材 ====================

    /**
     * 上传图文消息内的图片获取 URL (/cgi-bin/media/uploadimg)
     */
    public String uploadArticleImage(String fileName, byte[] fileBytes, String contentType) {
        MediaUploadResponse resp = httpClient.postMultipart(
                "/cgi-bin/media/uploadimg",
                null,
                "media",
                Objects.requireNonNull(fileName),
                Objects.requireNonNull(fileBytes),
                contentType != null ? contentType : "image/jpeg",
                null,
                MediaUploadResponse.class
        );
        return resp.getUrl();
    }

    /**
     * 新增其他类型永久素材 (/cgi-bin/material/add_material)
     *
     * @param type             媒体文件类型：image / voice / video / thumb
     * @param fileName         文件名
     * @param fileBytes        文件字节数组
     * @param contentType      MIME 类型
     * @param videoTitle       当 type 为 video 时必填：视频素材的标题
     * @param videoDescription 当 type 为 video 时必填：视频素材的描述
     */
    public MediaUploadResponse uploadPermanentMaterial(
            String type,
            String fileName,
            byte[] fileBytes,
            String contentType,
            String videoTitle,
            String videoDescription
    ) {
        Map<String, String> extraForm = null;
        if ("video".equalsIgnoreCase(type) && videoTitle != null) {
            String descJson = JsonUtils.toJson(Map.of(
                    "title", videoTitle,
                    "introduction", videoDescription != null ? videoDescription : ""
            ));
            extraForm = Map.of("description", descJson);
        }
        return httpClient.postMultipart(
                "/cgi-bin/material/add_material",
                Map.of("type", Objects.requireNonNull(type)),
                "media",
                Objects.requireNonNull(fileName),
                Objects.requireNonNull(fileBytes),
                contentType,
                extraForm,
                MediaUploadResponse.class
        );
    }

    /**
     * 获取永久素材 (/cgi-bin/material/get_material)
     */
    public MediaDownloadResult getPermanentMaterial(String mediaId) {
        return httpClient.download(
                "/cgi-bin/material/get_material",
                null,
                Map.of("media_id", Objects.requireNonNull(mediaId)),
                true
        );
    }

    /**
     * 删除永久素材 (/cgi-bin/material/del_material)
     */
    public WeChatResponse deletePermanentMaterial(String mediaId) {
        return httpClient.postJson(
                "/cgi-bin/material/del_material",
                null,
                Map.of("media_id", Objects.requireNonNull(mediaId)),
                WeChatResponse.class
        );
    }

    /**
     * 获取素材总数 (/cgi-bin/material/get_materialcount)
     */
    public MaterialCountResponse getMaterialCount() {
        return httpClient.get("/cgi-bin/material/get_materialcount", null, MaterialCountResponse.class);
    }

    /**
     * 获取素材列表 (/cgi-bin/material/batchget_material)
     *
     * @param type   素材的类型：image / video / voice / news
     * @param offset 从全部素材的该偏移位置开始返回，0表示从第一个素材返回
     * @param count  返回素材的数量，取值在1到20之间
     */
    public JsonNode batchGetMaterial(String type, int offset, int count) {
        Map<String, Object> body = Map.of(
                "type", Objects.requireNonNull(type),
                "offset", offset,
                "count", count
        );
        return httpClient.postJson("/cgi-bin/material/batchget_material", null, body, JsonNode.class);
    }
}
