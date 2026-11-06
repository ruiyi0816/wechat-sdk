package com.wechat.sdk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.wechat.sdk.http.WeChatHttpClient;
import com.wechat.sdk.model.WeChatResponse;
import com.wechat.sdk.model.message.CustomMessageRequest;
import com.wechat.sdk.model.message.MassMessageRequest;
import com.wechat.sdk.model.message.MassMessageRequest.MassSendResponse;
import com.wechat.sdk.model.message.SubscribeMessageRequest;
import com.wechat.sdk.model.message.TemplateListResponse;
import com.wechat.sdk.model.message.TemplateMessageRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 消息与通知管理接口服务（涵盖客服消息、模板消息、服务号订阅通知、高级群发）
 * 对应官方文档：消息能力 / 模板消息 / 客服消息 / 订阅通知 / 群发接口
 */
public class MessageApiService {

    private final WeChatHttpClient httpClient;

    public MessageApiService(WeChatHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    // ==================== 1. 客服消息 ====================

    /**
     * 发送客服消息 (/cgi-bin/message/custom/send)
     */
    public WeChatResponse sendCustomMessage(CustomMessageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return httpClient.postJson("/cgi-bin/message/custom/send", null, request, WeChatResponse.class);
    }

    /**
     * 客服输入状态下发 (/cgi-bin/message/custom/typing)
     *
     * @param toUser OpenID
     * @param typing true 表示 "Typing"（正在输入），false 表示 "CancelTyping"（取消正在输入）
     */
    public WeChatResponse setCustomerTyping(String toUser, boolean typing) {
        Map<String, String> body = Map.of(
                "touser", Objects.requireNonNull(toUser),
                "command", typing ? "Typing" : "CancelTyping"
        );
        return httpClient.postJson("/cgi-bin/message/custom/typing", null, body, WeChatResponse.class);
    }

    // ==================== 2. 模板消息 ====================

    /**
     * 设置所属行业 (/cgi-bin/template/api_set_industry)
     */
    public WeChatResponse setIndustry(String industryId1, String industryId2) {
        Map<String, String> body = Map.of(
                "industry_id1", Objects.requireNonNull(industryId1),
                "industry_id2", Objects.requireNonNull(industryId2)
        );
        return httpClient.postJson("/cgi-bin/template/api_set_industry", null, body, WeChatResponse.class);
    }

    /**
     * 获取设置的行业信息 (/cgi-bin/template/get_industry)
     */
    public JsonNode getIndustry() {
        return httpClient.get("/cgi-bin/template/get_industry", null, JsonNode.class);
    }

    /**
     * 获得模板 ID (/cgi-bin/template/api_add_template)
     *
     * @param templateIdShort 模板库中模板的编号，如 "TM00015"
     * @param keywordNameList 选用的类目模板关键词名称列表（可传 null）
     * @return 模板 ID (template_id)
     */
    public String addTemplate(String templateIdShort, List<String> keywordNameList) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("template_id_short", Objects.requireNonNull(templateIdShort));
        if (keywordNameList != null && !keywordNameList.isEmpty()) {
            body.put("keyword_name_list", keywordNameList);
        }
        JsonNode node = httpClient.postJson("/cgi-bin/template/api_add_template", null, body, JsonNode.class);
        return node.path("template_id").asText();
    }

    /**
     * 获取已添加至账号下所有模板列表 (/cgi-bin/template/get_all_private_template)
     */
    public TemplateListResponse getAllPrivateTemplates() {
        return httpClient.get("/cgi-bin/template/get_all_private_template", null, TemplateListResponse.class);
    }

    /**
     * 删除模板 (/cgi-bin/template/del_private_template)
     */
    public WeChatResponse deletePrivateTemplate(String templateId) {
        Objects.requireNonNull(templateId, "templateId must not be null");
        return httpClient.postJson("/cgi-bin/template/del_private_template", null, Map.of("template_id", templateId), WeChatResponse.class);
    }

    /**
     * 发送模板消息 (/cgi-bin/message/template/send)
     *
     * @return 消息 ID (msgid)
     */
    public long sendTemplateMessage(TemplateMessageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        JsonNode node = httpClient.postJson("/cgi-bin/message/template/send", null, request, JsonNode.class);
        return node.path("msgid").asLong();
    }

    // ==================== 3. 服务号订阅通知 ====================

    /**
     * 选用订阅通知公共模板 (/wxaapi/newtmpl/addtemplate)
     *
     * @return 私有模板 ID (priTmplId)
     */
    public String addSubscribeTemplate(String tid, List<Integer> kidList, String sceneDesc) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tid", Objects.requireNonNull(tid));
        body.put("kidList", Objects.requireNonNull(kidList));
        if (sceneDesc != null) {
            body.put("sceneDesc", sceneDesc);
        }
        JsonNode node = httpClient.postJson("/wxaapi/newtmpl/addtemplate", null, body, JsonNode.class);
        return node.path("priTmplId").asText();
    }

    /**
     * 删除账号下的订阅通知私有模板 (/wxaapi/newtmpl/deltemplate)
     */
    public WeChatResponse deleteSubscribeTemplate(String priTmplId) {
        Objects.requireNonNull(priTmplId, "priTmplId must not be null");
        return httpClient.postJson("/wxaapi/newtmpl/deltemplate", null, Map.of("priTmplId", priTmplId), WeChatResponse.class);
    }

    /**
     * 获取公众号所属类目 (/wxaapi/newtmpl/getcategory)
     */
    public JsonNode getSubscribeCategories() {
        return httpClient.get("/wxaapi/newtmpl/getcategory", null, JsonNode.class);
    }

    /**
     * 获取公共模板下的关键词列表 (/wxaapi/newtmpl/getpubtemplatekeywords)
     */
    public JsonNode getPubTemplateKeywords(String tid) {
        return httpClient.get("/wxaapi/newtmpl/getpubtemplatekeywords", Map.of("tid", Objects.requireNonNull(tid)), JsonNode.class);
    }

    /**
     * 获取类目下的公共模板标题列表 (/wxaapi/newtmpl/getpubtemplatetitles)
     */
    public JsonNode getPubTemplateTitles(String categoryIds, int start, int limit) {
        Map<String, String> params = Map.of(
                "ids", Objects.requireNonNull(categoryIds),
                "start", String.valueOf(start),
                "limit", String.valueOf(limit)
        );
        return httpClient.get("/wxaapi/newtmpl/getpubtemplatetitles", params, JsonNode.class);
    }

    /**
     * 获取私有订阅通知模板列表 (/wxaapi/newtmpl/gettemplate)
     */
    public JsonNode getSubscribeTemplates() {
        return httpClient.get("/wxaapi/newtmpl/gettemplate", null, JsonNode.class);
    }

    /**
     * 发送服务号订阅通知 (/cgi-bin/message/subscribe/bizsend)
     */
    public WeChatResponse sendSubscribeMessage(SubscribeMessageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return httpClient.postJson("/cgi-bin/message/subscribe/bizsend", null, request, WeChatResponse.class);
    }

    // ==================== 4. 高级群发接口 ====================

    /**
     * 根据标签进行群发 (/cgi-bin/message/mass/sendall)
     */
    public MassSendResponse sendMassByTag(MassMessageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return httpClient.postJson("/cgi-bin/message/mass/sendall", null, request, MassSendResponse.class);
    }

    /**
     * 根据 OpenID 列表进行群发 (/cgi-bin/message/mass/send)
     */
    public MassSendResponse sendMassByOpenIds(MassMessageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return httpClient.postJson("/cgi-bin/message/mass/send", null, request, MassSendResponse.class);
    }

    /**
     * 删除群发消息 (/cgi-bin/message/mass/delete)
     *
     * @param msgId      发送出去的消息 ID
     * @param articleIdx 要删除的文章在图文消息中的位置，第一篇编号为 1，该字段不填或填 0 会删除全部文章
     */
    public WeChatResponse deleteMassMessage(long msgId, Integer articleIdx) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("msg_id", msgId);
        if (articleIdx != null) {
            body.put("article_idx", articleIdx);
        }
        return httpClient.postJson("/cgi-bin/message/mass/delete", null, body, WeChatResponse.class);
    }

    /**
     * 预览群发消息 (/cgi-bin/message/mass/preview)
     */
    public MassSendResponse previewMassMessage(MassMessageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return httpClient.postJson("/cgi-bin/message/mass/preview", null, request, MassSendResponse.class);
    }

    /**
     * 查询群发消息发送状态 (/cgi-bin/message/mass/get)
     */
    public MassSendResponse getMassMessageStatus(String msgId) {
        return httpClient.postJson("/cgi-bin/message/mass/get", null, Map.of("msg_id", Objects.requireNonNull(msgId)), MassSendResponse.class);
    }

    /**
     * 获取群发速度 (/cgi-bin/message/mass/speed/get)
     */
    public JsonNode getMassSpeed() {
        return httpClient.postJson("/cgi-bin/message/mass/speed/get", null, Map.of(), JsonNode.class);
    }

    /**
     * 设置群发速度 (/cgi-bin/message/mass/speed/set)
     *
     * @param speed 群发速度的级别（0~4）
     */
    public WeChatResponse setMassSpeed(int speed) {
        return httpClient.postJson("/cgi-bin/message/mass/speed/set", null, Map.of("speed", speed), WeChatResponse.class);
    }
}
