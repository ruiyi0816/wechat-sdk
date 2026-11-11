package com.wechat.sdk.crypto;

import com.wechat.sdk.exception.WeChatException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * 微信消息加解密工具类（兼容官方 WXBizMsgCrypt AES-256-CBC + 32字节 PKCS#7 填充规范）
 */
public final class WeChatCryptUtil {

    private static final int PKCS7_BLOCK_SIZE = 32;
    private static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final String appId;
    private final String token;
    private final byte[] aesKey;

    public WeChatCryptUtil(String appId, String token, String encodingAesKey) {
        this.appId = Objects.requireNonNull(appId, "appId must not be null");
        this.token = Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(encodingAesKey, "encodingAesKey must not be null");
        if (encodingAesKey.length() != 43) {
            throw new WeChatException("Invalid encodingAesKey length: must be 43 characters");
        }
        this.aesKey = Base64.getDecoder().decode(encodingAesKey + "=");
    }

    /**
     * 加密明文消息并生成标准微信安全模式 XML 报文
     */
    public String encryptXml(String replyMsgXml, String timestamp, String nonce) {
        String encrypted = encrypt(generateRandomString(16), replyMsgXml);
        String signature = WeChatSignatureUtil.generateSignature(token, timestamp, nonce, encrypted);
        return """
                <xml>
                <Encrypt><![CDATA[%s]]></Encrypt>
                <MsgSignature><![CDATA[%s]]></MsgSignature>
                <TimeStamp>%s</TimeStamp>
                <Nonce><![CDATA[%s]]></Nonce>
                </xml>""".formatted(encrypted, signature, timestamp, nonce);
    }

    /**
     * 校验签名并从安全模式 XML 报文中解密出明文 XML
     */
    public String decryptXml(String msgSignature, String timestamp, String nonce, String encryptedXml) {
        String cipherText = extractEncryptTag(encryptedXml);
        if (!WeChatSignatureUtil.verifyMsgSignature(token, timestamp, nonce, cipherText, msgSignature)) {
            throw new WeChatException("WeChat message signature verification failed");
        }
        return decrypt(cipherText);
    }

    /**
     * 加密明文字符串，返回 Base64 密文
     */
    public String encrypt(String random16Str, String plainText) {
        try {
            byte[] randomBytes = random16Str.getBytes(StandardCharsets.UTF_8);
            byte[] textBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] networkOrderLength = ByteBuffer.allocate(4).putInt(textBytes.length).array();
            byte[] appIdBytes = appId.getBytes(StandardCharsets.UTF_8);

            ByteArrayOutputStream collector = new ByteArrayOutputStream();
            collector.write(randomBytes);
            collector.write(networkOrderLength);
            collector.write(textBytes);
            collector.write(appIdBytes);

            byte[] unpadded = collector.toByteArray();
            byte[] padBytes = pkcs7Encode(unpadded.length);
            collector.write(padBytes);
            byte[] padded = collector.toByteArray();

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(aesKey, 0, 16);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            byte[] encrypted = cipher.doFinal(padded);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new WeChatException("Failed to encrypt WeChat message", e);
        }
    }

    /**
     * 解密 Base64 密文并校验 AppID，返回明文字符串
     */
    public String decrypt(String cipherTextBase64) {
        byte[] original;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            byte[] encrypted = Base64.getDecoder().decode(cipherTextBase64);
            original = cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new WeChatException("Failed to decrypt WeChat message", e);
        }

        byte[] bytes = pkcs7Decode(original);
        if (bytes.length < 20) {
            throw new WeChatException("Decrypted message payload too short");
        }
        int xmlLength = ByteBuffer.wrap(bytes, 16, 4).getInt();
        if (xmlLength < 0 || 20 + xmlLength > bytes.length) {
            throw new WeChatException("Invalid XML length in decrypted WeChat message: " + xmlLength);
        }
        String xmlContent = new String(bytes, 20, xmlLength, StandardCharsets.UTF_8);
        String fromAppId = new String(bytes, 20 + xmlLength, bytes.length - 20 - xmlLength, StandardCharsets.UTF_8);
        if (!appId.equals(fromAppId)) {
            throw new WeChatException("AppID mismatch in decrypted message: expected " + appId + ", got " + fromAppId);
        }
        return xmlContent;
    }

    public static String extractEncryptTag(String xml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xml)));
            Element root = doc.getDocumentElement();
            var nodelist = root.getElementsByTagName("Encrypt");
            if (nodelist.getLength() == 0) {
                throw new WeChatException("Missing <Encrypt> tag in encrypted XML");
            }
            return nodelist.item(0).getTextContent();
        } catch (WeChatException e) {
            throw e;
        } catch (Exception e) {
            throw new WeChatException("Failed to parse encrypted XML", e);
        }
    }

    private static byte[] pkcs7Encode(int count) {
        // 32 - (count % 32) 的取值恒在 1..32：整块对齐时补满 32 字节，其余补 1~31 字节。
        int amountToPad = PKCS7_BLOCK_SIZE - (count % PKCS7_BLOCK_SIZE);
        byte padByte = (byte) (amountToPad & 0xFF);
        byte[] pads = new byte[amountToPad];
        Arrays.fill(pads, padByte);
        return pads;
    }

    private static byte[] pkcs7Decode(byte[] decrypted) {
        int pad = decrypted[decrypted.length - 1];
        if (pad < 1 || pad > PKCS7_BLOCK_SIZE) {
            pad = 0;
        }
        return Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);
    }

    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM_CHARS.charAt(SECURE_RANDOM.nextInt(RANDOM_CHARS.length())));
        }
        return sb.toString();
    }
}
