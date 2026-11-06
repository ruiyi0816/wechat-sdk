package com.wechat.sdk.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.wechat.sdk.http.WeChatHttpClient;
import com.wechat.sdk.model.WeChatResponse;
import com.wechat.sdk.model.user.UserInfo;
import com.wechat.sdk.model.user.UserListResponse;
import com.wechat.sdk.model.user.UserTag;
import com.wechat.sdk.util.JsonUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用户管理接口服务（用户标签、用户基本信息、用户列表、黑名单、账号迁移OpenID转换）
 * 对应官方文档：用户管理
 */
public class UserApiService {

    private final WeChatHttpClient httpClient;

    public UserApiService(WeChatHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    // ==================== 1. 用户标签管理 ====================

    /**
     * 创建标签 (/cgi-bin/tags/create)
     */
    public UserTag createTag(String name) {
        Map<String, Object> body = Map.of("tag", Map.of("name", Objects.requireNonNull(name)));
        JsonNode node = httpClient.postJson("/cgi-bin/tags/create", null, body, JsonNode.class);
        return JsonUtils.fromJson(node.path("tag").toString(), UserTag.class);
    }

    /**
     * 获取公众号已创建的标签 (/cgi-bin/tags/get)
     */
    public List<UserTag> getTags() {
        JsonNode node = httpClient.get("/cgi-bin/tags/get", null, JsonNode.class);
        return JsonUtils.fromJson(node.path("tags").toString(), new TypeReference<>() {});
    }

    /**
     * 编辑标签 (/cgi-bin/tags/update)
     */
    public WeChatResponse updateTag(int id, String name) {
        Map<String, Object> body = Map.of("tag", Map.of("id", id, "name", Objects.requireNonNull(name)));
        return httpClient.postJson("/cgi-bin/tags/update", null, body, WeChatResponse.class);
    }

    /**
     * 删除标签 (/cgi-bin/tags/delete)
     */
    public WeChatResponse deleteTag(int id) {
        Map<String, Object> body = Map.of("tag", Map.of("id", id));
        return httpClient.postJson("/cgi-bin/tags/delete", null, body, WeChatResponse.class);
    }

    /**
     * 获取标签下粉丝列表 (/cgi-bin/user/tag/get)
     */
    public UserListResponse getTagUsers(int tagId, String nextOpenId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tagid", tagId);
        body.put("next_openid", nextOpenId != null ? nextOpenId : "");
        return httpClient.postJson("/cgi-bin/user/tag/get", null, body, UserListResponse.class);
    }

    /**
     * 批量为用户打标签 (/cgi-bin/tags/members/batchtagging)
     */
    public WeChatResponse batchTagUsers(int tagId, List<String> openIdList) {
        Map<String, Object> body = Map.of(
                "tagid", tagId,
                "openid_list", Objects.requireNonNull(openIdList)
        );
        return httpClient.postJson("/cgi-bin/tags/members/batchtagging", null, body, WeChatResponse.class);
    }

    /**
     * 批量为用户取消标签 (/cgi-bin/tags/members/batchuntagging)
     */
    public WeChatResponse batchUntagUsers(int tagId, List<String> openIdList) {
        Map<String, Object> body = Map.of(
                "tagid", tagId,
                "openid_list", Objects.requireNonNull(openIdList)
        );
        return httpClient.postJson("/cgi-bin/tags/members/batchuntagging", null, body, WeChatResponse.class);
    }

    /**
     * 获取用户身上的标签列表 (/cgi-bin/tags/getidlist)
     */
    public List<Integer> getUserTagIds(String openId) {
        JsonNode node = httpClient.postJson("/cgi-bin/tags/getidlist", null, Map.of("openid", Objects.requireNonNull(openId)), JsonNode.class);
        return JsonUtils.fromJson(node.path("tagid_list").toString(), new TypeReference<>() {});
    }

    // ==================== 2. 用户信息与备注 ====================

    /**
     * 设置用户备注名 (/cgi-bin/user/info/updateremark)
     */
    public WeChatResponse updateUserRemark(String openId, String remark) {
        Map<String, String> body = Map.of(
                "openid", Objects.requireNonNull(openId),
                "remark", Objects.requireNonNull(remark)
        );
        return httpClient.postJson("/cgi-bin/user/info/updateremark", null, body, WeChatResponse.class);
    }

    /**
     * 获取用户基本信息(UnionID机制) (/cgi-bin/user/info)
     */
    public UserInfo getUserInfo(String openId, String lang) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("openid", Objects.requireNonNull(openId));
        params.put("lang", lang != null ? lang : "zh_CN");
        return httpClient.get("/cgi-bin/user/info", params, UserInfo.class);
    }

    /**
     * 批量获取用户基本信息 (/cgi-bin/user/info/batchget)
     */
    public List<UserInfo> batchGetUserInfo(List<String> openIds, String lang) {
        String effectiveLang = lang != null ? lang : "zh_CN";
        List<Map<String, String>> userList = openIds.stream()
                .map(id -> Map.of("openid", id, "lang", effectiveLang))
                .toList();
        JsonNode node = httpClient.postJson("/cgi-bin/user/info/batchget", null, Map.of("user_list", userList), JsonNode.class);
        return JsonUtils.fromJson(node.path("user_info_list").toString(), new TypeReference<>() {});
    }

    /**
     * 获取关注用户列表 (/cgi-bin/user/get)
     */
    public UserListResponse getUserList(String nextOpenId) {
        Map<String, String> params = new LinkedHashMap<>();
        if (nextOpenId != null && !nextOpenId.isBlank()) {
            params.put("next_openid", nextOpenId);
        }
        return httpClient.get("/cgi-bin/user/get", params, UserListResponse.class);
    }

    /**
     * 账号迁移 OpenID 转换 (/cgi-bin/changeopenid)
     */
    public JsonNode changeOpenId(String fromAppId, List<String> openIdList) {
        Map<String, Object> body = Map.of(
                "from_appid", Objects.requireNonNull(fromAppId),
                "openid_list", Objects.requireNonNull(openIdList)
        );
        return httpClient.postJson("/cgi-bin/changeopenid", null, body, JsonNode.class);
    }

    // ==================== 3. 黑名单管理 ====================

    /**
     * 获取公众号的黑名单列表 (/cgi-bin/tags/members/getblacklist)
     */
    public UserListResponse getBlackList(String beginOpenId) {
        Map<String, String> body = Map.of("begin_openid", beginOpenId != null ? beginOpenId : "");
        return httpClient.postJson("/cgi-bin/tags/members/getblacklist", null, body, UserListResponse.class);
    }

    /**
     * 拉黑用户 (/cgi-bin/tags/members/batchblacklist)
     */
    public WeChatResponse batchBlackList(List<String> openIdList) {
        return httpClient.postJson(
                "/cgi-bin/tags/members/batchblacklist",
                null,
                Map.of("openid_list", Objects.requireNonNull(openIdList)),
                WeChatResponse.class
        );
    }

    /**
     * 取消拉黑用户 (/cgi-bin/tags/members/batchunblacklist)
     */
    public WeChatResponse batchUnblackList(List<String> openIdList) {
        return httpClient.postJson(
                "/cgi-bin/tags/members/batchunblacklist",
                null,
                Map.of("openid_list", Objects.requireNonNull(openIdList)),
                WeChatResponse.class
        );
    }
}
