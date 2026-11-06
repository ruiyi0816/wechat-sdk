package com.wechat.sdk.model.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wechat.sdk.model.WeChatResponse;

/**
 * 带参数二维码创建响应 (/cgi-bin/qrcode/create)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QrCodeTicket extends WeChatResponse {

    @JsonProperty("ticket")
    private String ticket;

    @JsonProperty("expire_seconds")
    private int expireSeconds;

    @JsonProperty("url")
    private String url;

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public int getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(int expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
