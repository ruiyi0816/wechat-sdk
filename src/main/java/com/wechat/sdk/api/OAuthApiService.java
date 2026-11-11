package com.wechat.sdk.api;

import com.wechat.sdk.crypto.WeChatSignatureUtil;
import com.wechat.sdk.http.WeChatHttpClient;
import com.wechat.sdk.model.WeChatResponse;
import com.wechat.sdk.model.oauth.JsApiSignature;
import com.wechat.sdk.model.oauth.OAuthAccessToken;
import com.wechat.sdk.model.oauth.OAuthUserInfo;
import com.wechat.sdk.token.AccessTokenManager;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 微信网页开发接口服务（OAuth2.0 网页授权与 JS-SDK Ticket/签名）
 * 对应官方文档：网页开发 / 网页授权 / JS-SDK
 */
public class OAuthApiService {

    public static final String SCOPE_BASE = "snsapi_base";
    public static final String SCOPE_USERINFO = "snsapi_userinfo";

    private final WeChatHttpClient httpClient;
    private final AccessTokenManager tokenManager;

    public OAuthApiService(WeChatHttpClient httpClient, AccessTokenManager tokenManager) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.tokenManager = Objects.requireNonNull(tokenManager);
    }

    /**
     * 构造微信 OAuth2 网页授权跳转 URL
     *
     * @param redirectUri 授权后重定向的回调链接地址
     * @param scope       应用授权作用域：snsapi_base 或 snsapi_userinfo
     * @param state       重定向后会带上 state 参数（可为 null）
     * @param forcePopup  是否强制此次授权需要用户弹窗确认（可为 null）
     */
    public String buildAuthorizationUrl(String redirectUri, String scope, String state, Boolean forcePopup) {
        Objects.requireNonNull(redirectUri, "redirectUri must not be null");
        String effectiveScope = (scope == null || scope.isBlank()) ? SCOPE_BASE : scope;
        String encodedUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(httpClient.getConfig().getOpenBaseUrl())
                .append("/connect/oauth2/authorize?appid=")
                .append(URLEncoder.encode(httpClient.getConfig().getAppId(), StandardCharsets.UTF_8))
                .append("&redirect_uri=").append(encodedUri)
                .append("&response_type=code")
                .append("&scope=").append(URLEncoder.encode(effectiveScope, StandardCharsets.UTF_8));
        if (state != null && !state.isBlank()) {
            sb.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }
        if (forcePopup != null) {
            sb.append("&forcePopup=").append(forcePopup);
        }
        sb.append("#wechat_redirect");
        return sb.toString();
    }

    /**
     * 通过 code 换取网页授权 access_token (/sns/oauth2/access_token)
     */
    public OAuthAccessToken getOAuthAccessToken(String code) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("appid", httpClient.getConfig().getAppId());
        params.put("secret", httpClient.getConfig().getAppSecret());
        params.put("code", Objects.requireNonNull(code, "code must not be null"));
        params.put("grant_type", "authorization_code");
        return httpClient.get("/sns/oauth2/access_token", params, OAuthAccessToken.class, false);
    }

    /**
     * 刷新网页授权 access_token (/sns/oauth2/refresh_token)
     */
    public OAuthAccessToken refreshOAuthAccessToken(String refreshToken) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("appid", httpClient.getConfig().getAppId());
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", Objects.requireNonNull(refreshToken, "refreshToken must not be null"));
        return httpClient.get("/sns/oauth2/refresh_token", params, OAuthAccessToken.class, false);
    }

    /**
     * 拉取用户信息(需 scope 为 snsapi_userinfo) (/sns/userinfo)
     */
    public OAuthUserInfo getOAuthUserInfo(String oauthAccessToken, String openId, String lang) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("access_token", Objects.requireNonNull(oauthAccessToken));
        params.put("openid", Objects.requireNonNull(openId));
        params.put("lang", lang != null ? lang : "zh_CN");
        return httpClient.get("/sns/userinfo", params, OAuthUserInfo.class, false);
    }

    /**
     * 检验网页授权凭证 (access_token) 是否有效 (/sns/auth)
     */
    public boolean checkOAuthAccessToken(String oauthAccessToken, String openId) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("access_token", Objects.requireNonNull(oauthAccessToken));
        params.put("openid", Objects.requireNonNull(openId));
        WeChatResponse resp = httpClient.get("/sns/auth", params, WeChatResponse.class, false);
        return resp.isSuccess();
    }

    /**
     * 获取 JS-SDK jsapi_ticket（自动缓存）
     */
    public String getJsApiTicket() {
        return tokenManager.getTicket("jsapi");
    }

    /**
     * 获取微信卡券 api_ticket（自动缓存）
     */
    public String getWxCardTicket() {
        return tokenManager.getTicket("wx_card");
    }

    /**
     * 生成 JS-SDK wx.config 所需的签名配置
     *
     * @param url 当前网页的 URL，不包含 # 及其后面部分
     */
    public JsApiSignature createJsApiSignature(String url) {
        String nonceStr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        long timestamp = System.currentTimeMillis() / 1000L;
        return createJsApiSignature(url, nonceStr, timestamp);
    }

    /**
     * 使用指定的 nonceStr 与 timestamp 生成 JS-SDK 签名配置
     */
    public JsApiSignature createJsApiSignature(String url, String nonceStr, long timestamp) {
        Objects.requireNonNull(url, "url must not be null");
        Objects.requireNonNull(nonceStr, "nonceStr must not be null");
        String cleanUrl = url.contains("#") ? url.substring(0, url.indexOf('#')) : url;
        String ticket = getJsApiTicket();
        // 注意：签名原文必须保证与区域设置无关（timestamp 使用 ASCII 数字），
        // 因此保持字符串拼接，不要改为受默认 Locale 影响的 String.formatted("%d")。
        String raw = "jsapi_ticket=" + ticket
                + "&noncestr=" + nonceStr
                + "&timestamp=" + timestamp
                + "&url=" + cleanUrl;
        String signature = WeChatSignatureUtil.sha1Hex(raw);
        return new JsApiSignature(httpClient.getConfig().getAppId(), nonceStr, timestamp, cleanUrl, signature);
    }
}
