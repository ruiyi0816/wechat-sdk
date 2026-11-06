package com.wechat.sdk.model.oauth;

/**
 * JS-SDK wx.config 签名结果实体
 */
public record JsApiSignature(
        String appId,
        String nonceStr,
        long timestamp,
        String url,
        String signature
) {
}
