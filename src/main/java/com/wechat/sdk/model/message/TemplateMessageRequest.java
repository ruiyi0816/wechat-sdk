package com.wechat.sdk.model.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 模板消息发送请求 (/cgi-bin/message/template/send)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateMessageRequest {

    @JsonProperty("touser")
    private String toUser;

    @JsonProperty("template_id")
    private String templateId;

    @JsonProperty("url")
    private String url;

    @JsonProperty("miniprogram")
    private MiniProgramTarget miniProgram;

    @JsonProperty("client_msg_id")
    private String clientMsgId;

    @JsonProperty("data")
    private Map<String, DataItem> data = new LinkedHashMap<>();

    public TemplateMessageRequest() {
    }

    public TemplateMessageRequest(String toUser, String templateId) {
        this.toUser = toUser;
        this.templateId = templateId;
    }

    public TemplateMessageRequest addData(String key, String value) {
        this.data.put(key, new DataItem(value, null));
        return this;
    }

    public TemplateMessageRequest addData(String key, String value, String color) {
        this.data.put(key, new DataItem(value, color));
        return this;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public MiniProgramTarget getMiniProgram() {
        return miniProgram;
    }

    public void setMiniProgram(MiniProgramTarget miniProgram) {
        this.miniProgram = miniProgram;
    }

    public String getClientMsgId() {
        return clientMsgId;
    }

    public void setClientMsgId(String clientMsgId) {
        this.clientMsgId = clientMsgId;
    }

    public Map<String, DataItem> getData() {
        return data;
    }

    public void setData(Map<String, DataItem> data) {
        this.data = data;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MiniProgramTarget(
            @JsonProperty("appid") String appId,
            @JsonProperty("pagepath") String pagePath
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DataItem(
            @JsonProperty("value") String value,
            @JsonProperty("color") String color
    ) {
    }
}
