package com.wechat.sdk.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.wechat.sdk.config.WeChatConfig;
import com.wechat.sdk.http.WeChatHttpClient;

import java.util. LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 默认的内存级线程安全 AccessToken 与 Ticket 管理器。
 * 支持普通模式 (/cgi-bin/token) 与稳定版模式 (/cgi-bin/stable_token)。
 */
public class DefaultAccessTokenManager implements AccessTokenManager {

    private final WeChatConfig config;
    private final WeChatHttpClient httpClient;
    private final ReentrantLock tokenLock = new ReentrantLock();
    private final ReentrantLock ticketLock = new ReentrantLock();

    private volatile AccessToken currentToken;
    private final Map<String, AccessToken> ticketCache = new ConcurrentHashMap<>();

    public DefaultAccessTokenManager(WeChatConfig config, WeChatHttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
    }

    @Override
    public String getAccessToken() {
        return getAccessTokenEntity().accessToken();
    }

    @Override
    public AccessToken getAccessTokenEntity() {
        AccessToken token = this.currentToken;
        if (token != null && !token.isExpired()) {
            return token;
        }
        tokenLock.lock();
        try {
            token = this.currentToken;
            if (token != null && !token.isExpired()) {
                return token;
            }
            return fetchAndCacheToken(config.isForceRefreshStableToken());
        } finally {
            tokenLock.unlock();
        }
    }

    @Override
    public AccessToken refreshAccessToken() {
        tokenLock.lock();
        try {
            return fetchAndCacheToken(config.isForceRefreshStableToken());
        } finally {
            tokenLock.unlock();
        }
    }

    /**
     * 强制刷新稳定版 AccessToken（指定是否使用 force_refresh 模式）
     */
    public AccessToken refreshStableAccessToken(boolean forceRefresh) {
        tokenLock.lock();
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("grant_type", "client_credential");
            body.put("appid", config.getAppId());
            body.put("secret", config.getAppSecret());
            body.put("force_refresh", forceRefresh);
            JsonNode node = httpClient.postJson("/cgi-bin/stable_token", null, body, JsonNode.class, false);
            AccessToken newToken = AccessToken.create(
                    node.path("access_token").asText(),
                    node.path("expires_in").asInt(7200)
            );
            this.currentToken = newToken;
            return newToken;
        } finally {
            tokenLock.unlock();
        }
    }

    @Override
    public void invalidateAccessToken() {
        tokenLock.lock();
        try {
            this.currentToken = null;
        } finally {
            tokenLock.unlock();
        }
    }

    @Override
    public String getTicket(String type) {
        String ticketType = (type == null || type.isBlank()) ? "jsapi" : type;
        AccessToken cached = ticketCache.get(ticketType);
        if (cached != null && !cached.isExpired()) {
            return cached.accessToken();
        }
        ticketLock.lock();
        try {
            cached = ticketCache.get(ticketType);
            if (cached != null && !cached.isExpired()) {
                return cached.accessToken();
            }
            return fetchAndCacheTicket(ticketType);
        } finally {
            ticketLock.unlock();
        }
    }

    @Override
    public String refreshTicket(String type) {
        String ticketType = (type == null || type.isBlank()) ? "jsapi" : type;
        ticketLock.lock();
        try {
            return fetchAndCacheTicket(ticketType);
        } finally {
            ticketLock.unlock();
        }
    }

    private AccessToken fetchAndCacheToken(boolean forceRefreshStable) {
        JsonNode node;
        if (config.isUseStableToken()) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("grant_type", "client_credential");
            body.put("appid", config.getAppId());
            body.put("secret", config.getAppSecret());
            body.put("force_refresh", forceRefreshStable);
            node = httpClient.postJson("/cgi-bin/stable_token", null, body, JsonNode.class, false);
        } else {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("grant_type", "client_credential");
            params.put("appid", config.getAppId());
            params.put("secret", config.getAppSecret());
            node = httpClient.get("/cgi-bin/token", params, JsonNode.class, false);
        }
        AccessToken newToken = AccessToken.create(
                node.path("access_token").asText(),
                node.path("expires_in").asInt(7200)
        );
        this.currentToken = newToken;
        return newToken;
    }

    private String fetchAndCacheTicket(String ticketType) {
        Map<String, String> params = Map.of("type", ticketType);
        JsonNode node = httpClient.get("/cgi-bin/ticket/getticket", params, JsonNode.class, true);
        String ticketStr = node.path("ticket").asText();
        int expiresIn = node.path("expires_in").asInt(7200);
        AccessToken ticketToken = AccessToken.create(ticketStr, expiresIn);
        ticketCache.put(ticketType, ticketToken);
        return ticketStr;
    }
}
