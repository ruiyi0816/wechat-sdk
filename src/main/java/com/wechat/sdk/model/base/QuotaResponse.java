package com.wechat.sdk.model.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wechat.sdk.model.WeChatResponse;

/**
 * 查询 OpenAPI 调用额度响应 (/cgi-bin/openapi/quota/get)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuotaResponse extends WeChatResponse {

    @JsonProperty("quota")
    private QuotaDetail quota;

    @JsonProperty("rate_limit")
    private RateLimit rateLimit;

    @JsonProperty("component_rate_limit")
    private RateLimit componentRateLimit;

    public QuotaDetail getQuota() {
        return quota;
    }

    public void setQuota(QuotaDetail quota) {
        this.quota = quota;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public RateLimit getComponentRateLimit() {
        return componentRateLimit;
    }

    public void setComponentRateLimit(RateLimit componentRateLimit) {
        this.componentRateLimit = componentRateLimit;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record QuotaDetail(
            @JsonProperty("daily_limit") long dailyLimit,
            @JsonProperty("used") long used,
            @JsonProperty("remain") long remain
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RateLimit(
            @JsonProperty("call_count") long callCount,
            @JsonProperty("refresh_second") long refreshSecond
    ) {
    }
}
