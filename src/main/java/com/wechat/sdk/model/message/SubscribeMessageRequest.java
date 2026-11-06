package com.wechat.sdk.model.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 服务号订阅通知发送请求 (/cgi-bin/message/subscribe/bizsend)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscribeMessageRequest {

    @JsonProperty("touser")
    private String toUser;

    @JsonProperty("template_id")
    private String templateId;

    @JsonProperty("page")
    private String page;

    @JsonProperty("miniprogram")
    private TemplateMessageRequest.MiniProgramTarget miniProgram;

    @JsonProperty("data")
    private Map<String, ValueItem> data = new LinkedHashMap<>();

    public SubscribeMessageRequest() {
    }

    public SubscribeMessageRequest(String toUser, String templateId) {
        this.toUser = toUser;
        this.templateId = templateId;
    }

    public SubscribeMessageRequest addData(String key, String value) {
        this.data.put(key, new ValueItem(value));
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

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public TemplateMessageRequest.MiniProgramTarget getMiniProgram() {
        return miniProgram;
    }

    public void setMiniProgram(TemplateMessageRequest.MiniProgramTarget miniProgram) {
        this.miniProgram = miniProgram;
    }

    public Map<String, ValueItem> getData() {
        return data;
    }

    public void setData(Map<String, ValueItem> data) {
        this.data = data;
    }

    public record ValueItem(@JsonProperty("value") String value) {
    }
}
