package com.wechat.sdk.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 微信接口调用凭据 (Access Token) 实体
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AccessToken(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") int expiresIn,
        long expiresAtEpochMillis
) {
    /**
     * 默认提前 200 秒刷新，避免临界点过期
     */
    private static final long EXPIRY_BUFFER_MILLIS = 200_000L;

    public static AccessToken create(String accessToken, int expiresIn) {
        long now = System.currentTimeMillis();
        long effectiveMillis = Math.max(0L, (expiresIn * 1000L) - EXPIRY_BUFFER_MILLIS);
        return new AccessToken(accessToken, expiresIn, now + effectiveMillis);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAtEpochMillis;
    }
}
