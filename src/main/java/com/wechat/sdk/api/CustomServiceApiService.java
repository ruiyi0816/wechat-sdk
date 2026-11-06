package com.wechat.sdk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.wechat.sdk.http.WeChatHttpClient;
import com.wechat.sdk.model.WeChatResponse;

import java.util.Map;
import java.util.Objects;

/**
 * 多客服账号、会话管理与聊天记录接口服务
 * 对应官方文档：客服功能 / 客服管理 / 会话控制 / 聊天记录
 */
public class CustomServiceApiService {

    private final WeChatHttpClient httpClient;

    public CustomServiceApiService(WeChatHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    /**
     * 添加客服账号 (/customservice/kfaccount/add)
     */
    public WeChatResponse addKfAccount(String kfAccount, String nickname) {
        Map<String, String> body = Map.of(
                "kf_account", Objects.requireNonNull(kfAccount),
                "nickname", Objects.requireNonNull(nickname)
        );
        return httpClient.postJson("/customservice/kfaccount/add", null, body, WeChatResponse.class);
    }

    /**
     * 修改客服账号信息 (/customservice/kfaccount/update)
     */
    public WeChatResponse updateKfAccount(String kfAccount, String nickname) {
        Map<String, String> body = Map.of(
                "kf_account", Objects.requireNonNull(kfAccount),
                "nickname", Objects.requireNonNull(nickname)
        );
        return httpClient.postJson("/customservice/kfaccount/update", null, body, WeChatResponse.class);
    }

    /**
     * 删除客服账号 (/customservice/kfaccount/del)
     */
    public WeChatResponse deleteKfAccount(String kfAccount) {
        return httpClient.get("/customservice/kfaccount/del", Map.of("kf_account", Objects.requireNonNull(kfAccount)), WeChatResponse.class);
    }

    /**
     * 邀请绑定客服账号 (/customservice/kfaccount/inviteworker)
     */
    public WeChatResponse inviteWorker(String kfAccount, String inviteWx) {
        Map<String, String> body = Map.of(
                "kf_account", Objects.requireNonNull(kfAccount),
                "invite_wx", Objects.requireNonNull(inviteWx)
        );
        return httpClient.postJson("/customservice/kfaccount/inviteworker", null, body, WeChatResponse.class);
    }

    /**
     * 上传客服头像 (/customservice/kfaccount/uploadheadimg)
     */
    public WeChatResponse uploadKfHeadImg(String kfAccount, String fileName, byte[] imageBytes) {
        return httpClient.postMultipart(
                "/customservice/kfaccount/uploadheadimg",
                Map.of("kf_account", Objects.requireNonNull(kfAccount)),
                "media",
                fileName,
                imageBytes,
                "image/jpeg",
                null,
                WeChatResponse.class
        );
    }

    /**
     * 获取所有客服账号 (/cgi-bin/customservice/getkflist)
     */
    public JsonNode getKfList() {
        return httpClient.get("/cgi-bin/customservice/getkflist", null, JsonNode.class);
    }

    /**
     * 获取在线客服列表 (/cgi-bin/customservice/getonlinekflist)
     */
    public JsonNode getOnlineKfList() {
        return httpClient.get("/cgi-bin/customservice/getonlinekflist", null, JsonNode.class);
    }

    /**
     * 创建客服会话 (/customservice/kfsession/create)
     */
    public WeChatResponse createSession(String kfAccount, String openId) {
        Map<String, String> body = Map.of(
                "kf_account", Objects.requireNonNull(kfAccount),
                "openid", Objects.requireNonNull(openId)
        );
        return httpClient.postJson("/customservice/kfsession/create", null, body, WeChatResponse.class);
    }

    /**
     * 关闭客服会话 (/customservice/kfsession/close)
     */
    public WeChatResponse closeSession(String kfAccount, String openId) {
        Map<String, String> body = Map.of(
                "kf_account", Objects.requireNonNull(kfAccount),
                "openid", Objects.requireNonNull(openId)
        );
        return httpClient.postJson("/customservice/kfsession/close", null, body, WeChatResponse.class);
    }

    /**
     * 获取客户会话状态 (/customservice/kfsession/getsession)
     */
    public JsonNode getSession(String openId) {
        return httpClient.get("/customservice/kfsession/getsession", Map.of("openid", Objects.requireNonNull(openId)), JsonNode.class);
    }

    /**
     * 获取客服会话列表 (/customservice/kfsession/getsessionlist)
     */
    public JsonNode getSessionList(String kfAccount) {
        return httpClient.get("/customservice/kfsession/getsessionlist", Map.of("kf_account", Objects.requireNonNull(kfAccount)), JsonNode.class);
    }

    /**
     * 获取未接入会话列表 (/customservice/kfsession/getwaitcase)
     */
    public JsonNode getWaitCase() {
        return httpClient.get("/customservice/kfsession/getwaitcase", null, JsonNode.class);
    }

    /**
     * 获取聊天记录 (/customservice/msgrecord/getmsglist)
     */
    public JsonNode getMsgRecordList(long startTime, long endTime, long msgId, int number) {
        Map<String, Object> body = Map.of(
                "starttime", startTime,
                "endtime", endTime,
                "msgid", msgId,
                "number", number
        );
        return httpClient.postJson("/customservice/msgrecord/getmsglist", null, body, JsonNode.class);
    }
}
