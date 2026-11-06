package com.wechat.sdk.crypto;

import com.wechat.sdk.exception.WeChatException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 * 微信签名校验工具类（支持服务器地址校验 signature、消息加密 msg_signature 及 JS-SDK 签名）
 */
public final class WeChatSignatureUtil {

    private WeChatSignatureUtil() {
    }

    /**
     * 验证微信回调 URL 签名（signature = SHA1(sort(token, timestamp, nonce))）
     */
    public static boolean verifySignature(String token, String timestamp, String nonce, String signature) {
        if (token == null || timestamp == null || nonce == null || signature == null) {
            return false;
        }
        String computed = generateSignature(token, timestamp, nonce);
        return MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                signature.toLowerCase().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 验证安全模式加密消息签名（msg_signature = SHA1(sort(token, timestamp, nonce, encrypt))）
     */
    public static boolean verifyMsgSignature(String token, String timestamp, String nonce, String encryptedMsg, String msgSignature) {
        if (token == null || timestamp == null || nonce == null || encryptedMsg == null || msgSignature == null) {
            return false;
        }
        String computed = generateSignature(token, timestamp, nonce, encryptedMsg);
        return MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                msgSignature.toLowerCase().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 对多个参数按字典序排序拼接并计算 SHA-1 十六进制摘要
     */
    public static String generateSignature(String... parts) {
        Objects.requireNonNull(parts, "parts must not be null");
        String[] copy = Arrays.copyOf(parts, parts.length);
        for (int i = 0; i < copy.length; i++) {
            if (copy[i] == null) {
                copy[i] = "";
            }
        }
        Arrays.sort(copy);
        StringBuilder sb = new StringBuilder();
        for (String s : copy) {
            sb.append(s);
        }
        return sha1Hex(sb.toString());
    }

    /**
     * 计算字符串的 SHA-1 十六进制小写摘要
     */
    public static String sha1Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new WeChatException("SHA-1 algorithm not available", e);
        }
    }
}
