package com.wechat.sdk.api;

import com.wechat.sdk.http.WeChatHttpClient;
import com.wechat.sdk.model.WeChatResponse;
import com.wechat.sdk.model.base.IpListResponse;
import com.wechat.sdk.model.base.NetworkCheckResponse;
import com.wechat.sdk.model.base.QuotaResponse;
import com.wechat.sdk.model.base.RidInfoResponse;
import com.wechat.sdk.token.AccessToken;
import com.wechat.sdk.token.AccessTokenManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基础支持与 OpenAPI 管理接口服务
 * 对应官方文档：基础接口 / 接口调用凭据 / OpenAPI管理
 */
public class BaseApiService {

    private final WeChatHttpClient httpClient;
    private final AccessTokenManager tokenManager;

    public BaseApiService(WeChatHttpClient httpClient, AccessTokenManager tokenManager) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.tokenManager = Objects.requireNonNull(tokenManager);
    }

    /**
     * 获取当前有效 access_token
     */
    public String getAccessToken() {
        return tokenManager.getAccessToken();
    }

    /**
     * 强制刷新 access_token
     */
    public AccessToken refreshAccessToken() {
        return tokenManager.refreshAccessToken();
    }

    /**
     * 直接请求获取稳定版接口调用凭据 (/cgi-bin/stable_token)
     *
     * @param forceRefresh 是否强制刷新
     */
    public AccessToken fetchStableAccessToken(boolean forceRefresh) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("grant_type", "client_credential");
        body.put("appid", httpClient.getConfig().getAppId());
        body.put("secret", httpClient.getConfig().getAppSecret());
        body.put("force_refresh", forceRefresh);
        var node = httpClient.postJson("/cgi-bin/stable_token", null, body, com.fasterxml.jackson.databind.JsonNode.class, false);
        return AccessToken.create(
                node.path("access_token").asText(),
                node.path("expires_in").asInt(7200)
        );
    }

    /**
     * 获取微信 API 接口 IP 地址 (/cgi-bin/get_api_domain_ip)
     */
    public List<String> getApiDomainIp() {
        IpListResponse response = httpClient.get("/cgi-bin/get_api_domain_ip", null, IpListResponse.class);
        return response.getIpList();
    }

    /**
     * 获取微信推送/回调服务器 IP 地址 (/cgi-bin/getcallbackip)
     */
    public List<String> getCallbackIp() {
        IpListResponse response = httpClient.get("/cgi-bin/getcallbackip", null, IpListResponse.class);
        return response.getIpList();
    }

    /**
     * 网络通信检测 (/cgi-bin/callback/check)
     *
     * @param action        执行的检测动作：dns / ping / all
     * @param checkOperator 指定平台从某个运营商进行检测：CHINANET / UNICOM / CAP / DEFAULT
     */
    public NetworkCheckResponse checkNetwork(String action, String checkOperator) {
        Map<String, String> body = Map.of(
                "action", action != null ? action : "all",
                "check_operator", checkOperator != null ? checkOperator : "DEFAULT"
        );
        return httpClient.postJson("/cgi-bin/callback/check", null, body, NetworkCheckResponse.class);
    }

    /**
     * 清空 API 的调用 quota (/cgi-bin/clear_quota)
     */
    public WeChatResponse clearQuota() {
        Map<String, String> body = Map.of("appid", httpClient.getConfig().getAppId());
        return httpClient.postJson("/cgi-bin/clear_quota", null, body, WeChatResponse.class);
    }

    /**
     * 使用 AppSecret 重置 API 调用次数 (/cgi-bin/clear_quota/v2)，无需 access_token
     */
    public WeChatResponse clearQuotaByAppSecret() {
        Map<String, String> query = Map.of(
                "appid", httpClient.getConfig().getAppId(),
                "appsecret", httpClient.getConfig().getAppSecret()
        );
        return httpClient.postJson("/cgi-bin/clear_quota/v2", query, Map.of(), WeChatResponse.class, false);
    }

    /**
     * 查询 OPENAPI 调用额度 (/cgi-bin/openapi/quota/get)
     *
     * @param cgiPath API 的请求地址，例如 "/cgi-bin/message/custom/send"
     */
    public QuotaResponse getOpenApiQuota(String cgiPath) {
        Objects.requireNonNull(cgiPath, "cgiPath must not be null");
        return httpClient.postJson("/cgi-bin/openapi/quota/get", null, Map.of("cgi_path", cgiPath), QuotaResponse.class);
    }

    /**
     * 查询 rid 信息 (/cgi-bin/openapi/rid/get)
     *
     * @param rid 调用接口报错返回的 rid
     */
    public RidInfoResponse getRidInfo(String rid) {
        Objects.requireNonNull(rid, "rid must not be null");
        return httpClient.postJson("/cgi-bin/openapi/rid/get", null, Map.of("rid", rid), RidInfoResponse.class);
    }
}
