package com.wechat.sdk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.wechat.sdk.http.WeChatHttpClient;
import com.wechat.sdk.model.WeChatResponse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 发布能力接口服务
 * 对应官方文档：发布能力 (freepublish)
 */
public class FreePublishApiService {

    private final WeChatHttpClient httpClient;

    public FreePublishApiService(WeChatHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    /**
     * 发布接口 (/cgi-bin/freepublish/submit)
     *
     * @param mediaId 要发布的草稿的 media_id
     * @return 发布任务 ID (publish_id)
     */
    public String submitPublish(String mediaId) {
        JsonNode node = httpClient.postJson("/cgi-bin/freepublish/submit", null, Map.of("media_id", Objects.requireNonNull(mediaId)), JsonNode.class);
        return node.path("publish_id").asText();
    }

    /**
     * 发布状态轮询接口 (/cgi-bin/freepublish/get)
     */
    public JsonNode getPublishStatus(String publishId) {
        return httpClient.postJson("/cgi-bin/freepublish/get", null, Map.of("publish_id", Objects.requireNonNull(publishId)), JsonNode.class);
    }

    /**
     * 删除发布文章 (/cgi-bin/freepublish/delete)
     *
     * @param articleId 成功发布时返回的 article_id
     * @param index     要删除的文章在图文消息中的位置，第一篇编号为 1，该字段不填或填 0 会删除全部文章
     */
    public WeChatResponse deletePublishedArticle(String articleId, Integer index) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("article_id", Objects.requireNonNull(articleId));
        if (index != null) {
            body.put("index", index);
        }
        return httpClient.postJson("/cgi-bin/freepublish/delete", null, body, WeChatResponse.class);
    }

    /**
     * 通过 article_id 获取已发布文章 (/cgi-bin/freepublish/getarticle)
     */
    public JsonNode getPublishedArticle(String articleId) {
        return httpClient.postJson("/cgi-bin/freepublish/getarticle", null, Map.of("article_id", Objects.requireNonNull(articleId)), JsonNode.class);
    }

    /**
     * 获取成功发布列表 (/cgi-bin/freepublish/batchget)
     */
    public JsonNode batchGetPublishedArticles(int offset, int count, int noContent) {
        Map<String, Object> body = Map.of(
                "offset", offset,
                "count", count,
                "no_content", noContent
        );
        return httpClient.postJson("/cgi-bin/freepublish/batchget", null, body, JsonNode.class);
    }
}
