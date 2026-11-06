package com.wechat.sdk.exception;

/**
 * 微信 SDK 基础异常
 */
public class WeChatException extends RuntimeException {

    public WeChatException(String message) {
        super(message);
    }

    public WeChatException(String message, Throwable cause) {
        super(message, cause);
    }
}
