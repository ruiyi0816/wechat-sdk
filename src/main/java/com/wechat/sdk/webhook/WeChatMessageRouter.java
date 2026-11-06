package com.wechat.sdk.webhook;

import com.wechat.sdk.config.WeChatConfig;
import com.wechat.sdk.crypto.WeChatCryptUtil;
import com.wechat.sdk.crypto.WeChatSignatureUtil;
import com.wechat.sdk.exception.WeChatException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 微信回调消息路由器（支持签名校验、明文/安全模式加解密、消息去重与规则路由）
 */
public class WeChatMessageRouter {

    private static final String SUCCESS_RESPONSE = "success";
    private static final long DEFAULT_DEDUP_TTL_MILLIS = 15_000L;

    private final WeChatConfig config;
    private final WeChatCryptUtil cryptUtil;
    private final List<RouteRule> rules = new ArrayList<>();
    private final Map<String, Long> deduplicationCache = new ConcurrentHashMap<>();
    private boolean enableDeduplication = true;
    private Function<WebhookMessage, String> defaultHandler = msg -> SUCCESS_RESPONSE;

    public WeChatMessageRouter(WeChatConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        if (config.getToken() != null && config.getEncodingAesKey() != null && !config.getEncodingAesKey().isBlank()) {
            this.cryptUtil = new WeChatCryptUtil(config.getAppId(), config.getToken(), config.getEncodingAesKey());
        } else {
            this.cryptUtil = null;
        }
    }

    public WeChatMessageRouter enableDeduplication(boolean enableDeduplication) {
        this.enableDeduplication = enableDeduplication;
        return this;
    }

    /**
     * 注册按消息类型 (MsgType) 匹配的处理器
     */
    public WeChatMessageRouter onMsgType(String msgType, Function<WebhookMessage, String> handler) {
        Objects.requireNonNull(msgType, "msgType must not be null");
        Objects.requireNonNull(handler, "handler must not be null");
        rules.add(new RouteRule(msg -> msgType.equalsIgnoreCase(msg.getMsgType()), handler));
        return this;
    }

    /**
     * 注册按事件类型 (MsgType=event 且 Event=eventType) 匹配的处理器
     */
    public WeChatMessageRouter onEvent(String eventType, Function<WebhookMessage, String> handler) {
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(handler, "handler must not be null");
        rules.add(new RouteRule(
                msg -> "event".equalsIgnoreCase(msg.getMsgType()) && eventType.equalsIgnoreCase(msg.getEvent()),
                handler
        ));
        return this;
    }

    /**
     * 注册按自定义条件匹配的处理器
     */
    public WeChatMessageRouter onMatch(Predicate<WebhookMessage> matcher, Function<WebhookMessage, String> handler) {
        rules.add(new RouteRule(Objects.requireNonNull(matcher), Objects.requireNonNull(handler)));
        return this;
    }

    /**
     * 设置兜底处理器（未匹配任何规则时调用，默认返回 "success"）
     */
    public WeChatMessageRouter fallback(Function<WebhookMessage, String> defaultHandler) {
        this.defaultHandler = Objects.requireNonNull(defaultHandler);
        return this;
    }

    /**
     * 处理微信服务器 GET 接入验证请求
     *
     * @return 验证通过返回 echoStr，否则抛出 WeChatException
     */
    public String verifyUrl(String signature, String timestamp, String nonce, String echoStr) {
        if (config.getToken() == null || config.getToken().isBlank()) {
            throw new WeChatException("Token is not configured in WeChatConfig");
        }
        if (!WeChatSignatureUtil.verifySignature(config.getToken(), timestamp, nonce, signature)) {
            throw new WeChatException("WeChat webhook URL signature verification failed");
        }
        return echoStr;
    }

    /**
     * 处理微信回调 POST 请求（自动识别明文或 AES 加密模式）
     *
     * @param signature    URL 参数 signature
     * @param timestamp    URL 参数 timestamp
     * @param nonce        URL 参数 nonce
     * @param encryptType  URL 参数 encrypt_type（若为 "aes" 则启用加解密，否则为明文）
     * @param msgSignature URL 参数 msg_signature（安全模式必填）
     * @param requestXml   POST 请求体 XML
     * @return 响应给微信服务器的字符串（被动回复 XML 或 "success"）
     */
    public String handleWebhook(
            String signature,
            String timestamp,
            String nonce,
            String encryptType,
            String msgSignature,
            String requestXml
    ) {
        boolean encrypted = "aes".equalsIgnoreCase(encryptType);
        String plainXml;
        if (encrypted) {
            if (cryptUtil == null) {
                throw new WeChatException("Encrypted message received, but encodingAesKey/token is not configured");
            }
            plainXml = cryptUtil.decryptXml(msgSignature, timestamp, nonce, requestXml);
        } else {
            if (config.getToken() != null && signature != null) {
                if (!WeChatSignatureUtil.verifySignature(config.getToken(), timestamp, nonce, signature)) {
                    throw new WeChatException("WeChat webhook signature verification failed");
                }
            }
            plainXml = requestXml;
        }

        WebhookMessage message = WebhookMessage.fromXml(plainXml);
        String replyXml = route(message);
        if (replyXml == null || replyXml.isBlank() || SUCCESS_RESPONSE.equalsIgnoreCase(replyXml)) {
            return SUCCESS_RESPONSE;
        }
        if (encrypted) {
            return cryptUtil.encryptXml(replyXml, timestamp, nonce);
        }
        return replyXml;
    }

    /**
     * 直接对已解析的 WebhookMessage 执行路由分发
     */
    public String route(WebhookMessage message) {
        Objects.requireNonNull(message, "message must not be null");
        if (enableDeduplication && isDuplicate(message)) {
            return SUCCESS_RESPONSE;
        }
        for (RouteRule rule : rules) {
            if (rule.matcher().test(message)) {
                String result = rule.handler().apply(message);
                return result != null ? result : SUCCESS_RESPONSE;
            }
        }
        return defaultHandler.apply(message);
    }

    private boolean isDuplicate(WebhookMessage message) {
        long now = System.currentTimeMillis();
        deduplicationCache.entrySet().removeIf(entry -> now - entry.getValue() > DEFAULT_DEDUP_TTL_MILLIS);
        String key = message.getDeduplicationKey();
        Long previous = deduplicationCache.putIfAbsent(key, now);
        return previous != null;
    }

    private record RouteRule(Predicate<WebhookMessage> matcher, Function<WebhookMessage, String> handler) {
    }
}
