package com.wechat.sdk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.wechat.sdk.http.WeChatHttpClient;
import com.wechat.sdk.model.WeChatResponse;
import com.wechat.sdk.model.material.DraftArticle;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 草稿箱管理接口服务
 * 对应官方文档：草稿箱
 */
public class DraftApiService {

    private final WeChatHttpClient httpClient;

    public DraftApiService(WeChatHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    /**
     * 新建草稿 (/cgi-bin/draft/add)
     *
     * @param articles 图文草稿列表
     * @return 草稿的 media_id
     */
    public String addDraft(List<DraftArticle> articles) {
        Objects.requireNonNull(articles, "articles must not be null");
        JsonNode node = httpClient.postJson("/cgi-bin/draft/add", null, Map.of("articles", articles), JsonNode.class);
        return node.path("media_id").asText();
    }

    /**
     * 获取草稿 (/cgi-bin/draft/get)
     */
    public JsonNode getDraft(String mediaId) {
        return httpClient.postJson("/cgi-bin/draft/get", null, Map.of("media_id", Objects.requireNonNull(mediaId)), JsonNode.class);
    }

    /**
     * 删除草稿 (/cgi-bin/draft/delete)
     */
    public WeChatResponse deleteDraft(String mediaId) {
        return httpClient.postJson("/cgi-bin/draft/delete", null, Map.of("media_id", Objects.requireNonNull(mediaId)), WeChatResponse.class);
    }

    /**
     * 修改草稿 (/cgi-bin/draft/update)
     *
     * @param mediaId 要修改的图文消息的 id
     * @param index   要更新的文章在图文消息中的位置（多图文消息时，此字段才有意义），第一篇为 0
     * @param article 更新后的文章内容
     */
    public WeChatResponse updateDraft(String mediaId, int index, DraftArticle article) {
        Map<String, Object> body = Map.of(
                "media_id", Objects.requireNonNull(mediaId),
                "index", index,
                "articles", Objects.requireNonNull(article)
        );
        return httpClient.postJson("/cgi-bin/draft/update", null, body, WeChatResponse.class);
    }

    /**
     * 获取草稿总数 (/cgi-bin/draft/count)
     */
    public int getDraftCount() {
        JsonNode node = httpClient.get("/cgi-bin/draft/count", null, JsonNode.class);
        return node.path("total_count").asInt();
    }

    /**
     * 获取草稿列表 (/cgi-bin/draft/batchget)
     *
     * @param offset    从全部素材的该偏移位置开始返回，0表示从第一个素材返回
     * @param count     返回素材的数量，取值在1到20之间
     * @param noContent 1 表示不返回 content 字段，0 表示正常返回
     */
    public JsonNode batchGetDrafts(int offset, int count, int noContent) {
        Map<String, Object> body = Map.of(
                "offset", offset,
                "count", count,
                "no_content", noContent
        );
        return httpClient.postJson("/cgi-bin/draft/batchget", null, body, JsonNode.class);
    }

    /**
     * MP端草稿箱与发布功能开关设置或检查 (/cgi-bin/draft/switch)
     *
     * @param checkOnly true 表示仅查询状态（携带 checkonly=1 参数），false 表示开启开关
     */
    public JsonNode draftSwitch(boolean checkOnly) {
        Map<String, String> query = checkOnly ? Map.of("checkonly", "1") : null;
        return httpClient.postJson("/cgi-bin/draft/switch", query, Map.of(), JsonNode.class);
    }
}
