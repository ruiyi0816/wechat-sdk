package com.wechat.sdk.token;

/**
 * 微信 AccessToken 与 Ticket 管理器接口。
 * 分布式/集群环境可实现此接口接入 Redis 等集中式存储。
 */
public interface AccessTokenManager {

    /**
     * 获取当前有效的 access_token 字符串；若不存在或已过期则自动刷新
     */
    String getAccessToken();

    /**
     * 获取当前完整的 AccessToken 对象
     */
    AccessToken getAccessTokenEntity();

    /**
     * 强制刷新并返回新的 AccessToken
     */
    AccessToken refreshAccessToken();

    /**
     * 使当前缓存的 AccessToken 失效
     */
    void invalidateAccessToken();

    /**
     * 获取指定类型的 Ticket（如 "jsapi" 或 "wx_card"），支持自动缓存与过期刷新
     */
    String getTicket(String type);

    /**
     * 强制刷新指定类型的 Ticket
     */
    String refreshTicket(String type);
}
