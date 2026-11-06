package com.wechat.sdk.model.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wechat.sdk.model.WeChatResponse;

/**
 * 查询 rid 报错详情响应 (/cgi-bin/openapi/rid/get)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RidInfoResponse extends WeChatResponse {

    @JsonProperty("request")
    private RidRequestDetail request;

    public RidRequestDetail getRequest() {
        return request;
    }

    public void setRequest(RidRequestDetail request) {
        this.request = request;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RidRequestDetail(
            @JsonProperty("invoke_time") long invokeTime,
            @JsonProperty("cost_in_ms") long costInMs,
            @JsonProperty("request_url") String requestUrl,
            @JsonProperty("request_body") String requestBody,
            @JsonProperty("response_body") String responseBody,
            @JsonProperty("client_ip") String clientIp
    ) {
    }
}
