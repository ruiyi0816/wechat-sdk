package com.wechat.sdk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.wechat.sdk.http.WeChatHttpClient;
import com.wechat.sdk.model.WeChatResponse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 图文消息留言（评论）管理接口服务
 * 对应官方文档：图文消息留言管理
 */
public class CommentApiService {

    private final WeChatHttpClient httpClient;

    public CommentApiService(WeChatHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    /**
     * 打开已群发文章评论 (/cgi-bin/comment/open)
     */
    public WeChatResponse openComment(long msgDataId, Integer index) {
        return httpClient.postJson("/cgi-bin/comment/open", null, buildArticleBody(msgDataId, index), WeChatResponse.class);
    }

    /**
     * 关闭已群发文章评论 (/cgi-bin/comment/close)
     */
    public WeChatResponse closeComment(long msgDataId, Integer index) {
        return httpClient.postJson("/cgi-bin/comment/close", null, buildArticleBody(msgDataId, index), WeChatResponse.class);
    }

    /**
     * 查看指定文章的评论数据 (/cgi-bin/comment/list)
     *
     * @param type 0=普通评论&精选评论，1=普通评论，2=精选评论
     */
    public JsonNode listComments(long msgDataId, Integer index, int begin, int count, int type) {
        Map<String, Object> body = buildArticleBody(msgDataId, index);
        body.put("begin", begin);
        body.put("count", count);
        body.put("type", type);
        return httpClient.postJson("/cgi-bin/comment/list", null, body, JsonNode.class);
    }

    /**
     * 将评论标记精选 (/cgi-bin/comment/markelect)
     */
    public WeChatResponse markElectComment(long msgDataId, Integer index, long userCommentId) {
        Map<String, Object> body = buildCommentBody(msgDataId, index, userCommentId);
        return httpClient.postJson("/cgi-bin/comment/markelect", null, body, WeChatResponse.class);
    }

    /**
     * 将评论取消精选 (/cgi-bin/comment/unmarkelect)
     */
    public WeChatResponse unmarkElectComment(long msgDataId, Integer index, long userCommentId) {
        Map<String, Object> body = buildCommentBody(msgDataId, index, userCommentId);
        return httpClient.postJson("/cgi-bin/comment/unmarkelect", null, body, WeChatResponse.class);
    }

    /**
     * 删除评论 (/cgi-bin/comment/delete)
     */
    public WeChatResponse deleteComment(long msgDataId, Integer index, long userCommentId) {
        Map<String, Object> body = buildCommentBody(msgDataId, index, userCommentId);
        return httpClient.postJson("/cgi-bin/comment/delete", null, body, WeChatResponse.class);
    }

    /**
     * 回复评论 (/cgi-bin/comment/reply/add)
     */
    public WeChatResponse addCommentReply(long msgDataId, Integer index, long userCommentId, String content) {
        Map<String, Object> body = buildCommentBody(msgDataId, index, userCommentId);
        body.put("content", Objects.requireNonNull(content));
        return httpClient.postJson("/cgi-bin/comment/reply/add", null, body, WeChatResponse.class);
    }

    /**
     * 删除回复 (/cgi-bin/comment/reply/delete)
     */
    public WeChatResponse deleteCommentReply(long msgDataId, Integer index, long userCommentId) {
        Map<String, Object> body = buildCommentBody(msgDataId, index, userCommentId);
        return httpClient.postJson("/cgi-bin/comment/reply/delete", null, body, WeChatResponse.class);
    }

    private static Map<String, Object> buildArticleBody(long msgDataId, Integer index) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("msg_data_id", msgDataId);
        if (index != null) {
            body.put("index", index);
        }
        return body;
    }

    private static Map<String, Object> buildCommentBody(long msgDataId, Integer index, long userCommentId) {
        Map<String, Object> body = buildArticleBody(msgDataId, index);
        body.put("user_comment_id", userCommentId);
        return body;
    }
}
