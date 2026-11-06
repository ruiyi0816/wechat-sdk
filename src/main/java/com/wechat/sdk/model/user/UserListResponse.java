package com.wechat.sdk.model.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wechat.sdk.model.WeChatResponse;

import java.util.Collections;
import java.util.List;

/**
 * 关注用户列表 / 标签下粉丝列表 / 黑名单列表响应
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserListResponse extends WeChatResponse {

    @JsonProperty("total")
    private int total;

    @JsonProperty("count")
    private int count;

    @JsonProperty("data")
    private OpenIdData data;

    @JsonProperty("next_openid")
    private String nextOpenId;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public OpenIdData getData() {
        return data;
    }

    public void setData(OpenIdData data) {
        this.data = data;
    }

    public List<String> getOpenIds() {
        return data != null && data.openIdList() != null ? data.openIdList() : Collections.emptyList();
    }

    public String getNextOpenId() {
        return nextOpenId;
    }

    public void setNextOpenId(String nextOpenId) {
        this.nextOpenId = nextOpenId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpenIdData(
            @JsonProperty("openid") List<String> openIdList
    ) {
    }
}
