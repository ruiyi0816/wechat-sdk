package com.wechat.sdk.config;

import java.time.Duration;
import java.util.Objects;

/**
 * 微信公众号（服务号）SDK 核心配置类
 */
public final class WeChatConfig {

    public static final String DEFAULT_API_BASE_URL = "https://api.weixin.qq.com";
    public static final String BACKUP_API_BASE_URL = "https://api2.weixin.qq.com";
    public static final String DEFAULT_MP_BASE_URL = "https://mp.weixin.qq.com";
    public static final String DEFAULT_OPEN_BASE_URL = "https://open.weixin.qq.com";

    private final String appId;
    private final String appSecret;
    private final String token;
    private final String encodingAesKey;
    private final String apiBaseUrl;
    private final String mpBaseUrl;
    private final String openBaseUrl;
    private final Duration connectTimeout;
    private final Duration requestTimeout;
    private final boolean useStableToken;
    private final boolean forceRefreshStableToken;
    private final boolean autoRetryOnTokenExpired;
    private final int maxRetries;

    private WeChatConfig(Builder builder) {
        this.appId = Objects.requireNonNull(builder.appId, "appId must not be null");
        this.appSecret = Objects.requireNonNull(builder.appSecret, "appSecret must not be null");
        this.token = builder.token;
        this.encodingAesKey = builder.encodingAesKey;
        this.apiBaseUrl = stripTrailingSlash(builder.apiBaseUrl);
        this.mpBaseUrl = stripTrailingSlash(builder.mpBaseUrl);
        this.openBaseUrl = stripTrailingSlash(builder.openBaseUrl);
        this.connectTimeout = builder.connectTimeout;
        this.requestTimeout = builder.requestTimeout;
        this.useStableToken = builder.useStableToken;
        this.forceRefreshStableToken = builder.forceRefreshStableToken;
        this.autoRetryOnTokenExpired = builder.autoRetryOnTokenExpired;
        this.maxRetries = builder.maxRetries;
    }

    private static String stripTrailingSlash(String url) {
        if (url != null && url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static WeChatConfig of(String appId, String appSecret) {
        return builder().appId(appId).appSecret(appSecret).build();
    }

    public String getAppId() {
        return appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public String getToken() {
        return token;
    }

    public String getEncodingAesKey() {
        return encodingAesKey;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public String getMpBaseUrl() {
        return mpBaseUrl;
    }

    public String getOpenBaseUrl() {
        return openBaseUrl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public boolean isUseStableToken() {
        return useStableToken;
    }

    public boolean isForceRefreshStableToken() {
        return forceRefreshStableToken;
    }

    public boolean isAutoRetryOnTokenExpired() {
        return autoRetryOnTokenExpired;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public static final class Builder {
        private String appId;
        private String appSecret;
        private String token;
        private String encodingAesKey;
        private String apiBaseUrl = DEFAULT_API_BASE_URL;
        private String mpBaseUrl = DEFAULT_MP_BASE_URL;
        private String openBaseUrl = DEFAULT_OPEN_BASE_URL;
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration requestTimeout = Duration.ofSeconds(30);
        private boolean useStableToken = false;
        private boolean forceRefreshStableToken = false;
        private boolean autoRetryOnTokenExpired = true;
        private int maxRetries = 1;

        private Builder() {
        }

        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder appSecret(String appSecret) {
            this.appSecret = appSecret;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder encodingAesKey(String encodingAesKey) {
            this.encodingAesKey = encodingAesKey;
            return this;
        }

        public Builder apiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = Objects.requireNonNull(apiBaseUrl);
            return this;
        }

        public Builder mpBaseUrl(String mpBaseUrl) {
            this.mpBaseUrl = Objects.requireNonNull(mpBaseUrl);
            return this;
        }

        public Builder openBaseUrl(String openBaseUrl) {
            this.openBaseUrl = Objects.requireNonNull(openBaseUrl);
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = Objects.requireNonNull(connectTimeout);
            return this;
        }

        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = Objects.requireNonNull(requestTimeout);
            return this;
        }

        public Builder useStableToken(boolean useStableToken) {
            this.useStableToken = useStableToken;
            return this;
        }

        public Builder forceRefreshStableToken(boolean forceRefreshStableToken) {
            this.forceRefreshStableToken = forceRefreshStableToken;
            return this;
        }

        public Builder autoRetryOnTokenExpired(boolean autoRetryOnTokenExpired) {
            this.autoRetryOnTokenExpired = autoRetryOnTokenExpired;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = Math.max(0, maxRetries);
            return this;
        }

        public WeChatConfig build() {
            return new WeChatConfig(this);
        }
    }
}
