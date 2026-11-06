package com.wechat.sdk.model.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wechat.sdk.model.WeChatResponse;

import java.util.List;

/**
 * 私有模板列表响应 (/cgi-bin/template/get_all_private_template)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateListResponse extends WeChatResponse {

    @JsonProperty("template_list")
    private List<TemplateInfo> templateList;

    public List<TemplateInfo> getTemplateList() {
        return templateList;
    }

    public void setTemplateList(List<TemplateInfo> templateList) {
        this.templateList = templateList;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TemplateInfo(
            @JsonProperty("template_id") String templateId,
            @JsonProperty("title") String title,
            @JsonProperty("primary_industry") String primaryIndustry,
            @JsonProperty("deputy_industry") String deputyIndustry,
            @JsonProperty("content") String content,
            @JsonProperty("example") String example
    ) {
    }
}
