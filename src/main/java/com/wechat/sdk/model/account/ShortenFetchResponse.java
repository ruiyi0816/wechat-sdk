package com.wechat.sdk.model.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wechat.sdk.model.WeChatResponse;

/**
 * 短 key 还原长数据响应 (/cgi-bin/shorten/fetch)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShortenFetchResponse extends WeChatResponse {

    @JsonProperty("long_data")
    private String longData;

    @JsonProperty("create_time")
    private long createTime;

    @JsonProperty("expire_seconds")
    private int expireSeconds;

    public String getLongData() {
        return longData;
    }

    public void setLongData(String longData) {
        this.longData = longData;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(int expireSeconds) {
        this.expireSeconds = expireSeconds;
    }
}
