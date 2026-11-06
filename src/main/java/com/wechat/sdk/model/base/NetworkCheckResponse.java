package com.wechat.sdk.model.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wechat.sdk.model.WeChatResponse;

import java.util.List;

/**
 * 网络通信检测响应 (/cgi-bin/callback/check)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkCheckResponse extends WeChatResponse {

    @JsonProperty("dns")
    private List<DnsInfo> dns;

    @JsonProperty("ping")
    private List<PingInfo> ping;

    public List<DnsInfo> getDns() {
        return dns;
    }

    public void setDns(List<DnsInfo> dns) {
        this.dns = dns;
    }

    public List<PingInfo> getPing() {
        return ping;
    }

    public void setPing(List<PingInfo> ping) {
        this.ping = ping;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DnsInfo(
            @JsonProperty("ip") String ip,
            @JsonProperty("real_operator") String realOperator
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PingInfo(
            @JsonProperty("ip") String ip,
            @JsonProperty("from_operator") String fromOperator,
            @JsonProperty("package_loss") String packageLoss,
            @JsonProperty("time") String time
    ) {
    }
}
