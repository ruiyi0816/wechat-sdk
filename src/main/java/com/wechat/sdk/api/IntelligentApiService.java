package com.wechat.sdk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.wechat.sdk.http.WeChatHttpClient;
import com.wechat.sdk.model.WeChatResponse;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 智能接口服务（OCR 识别、语音识别、微信翻译、语义理解）
 * 对应官方文档：智能接口
 */
public class IntelligentApiService {

    private final WeChatHttpClient httpClient;

    public IntelligentApiService(WeChatHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    // ==================== 1. OCR 识别接口 ====================

    /**
     * 身份证 OCR 识别 (/cv/ocr/idcard)
     */
    public JsonNode ocrIdCard(String imgUrl) {
        return httpClient.postJson("/cv/ocr/idcard", Map.of("img_url", Objects.requireNonNull(imgUrl)), Map.of(), JsonNode.class);
    }

    /**
     * 银行卡 OCR 识别 (/cv/ocr/bankcard)
     */
    public JsonNode ocrBankCard(String imgUrl) {
        return httpClient.postJson("/cv/ocr/bankcard", Map.of("img_url", Objects.requireNonNull(imgUrl)), Map.of(), JsonNode.class);
    }

    /**
     * 行驶证 OCR 识别 (/cv/ocr/driving)
     */
    public JsonNode ocrDriving(String imgUrl) {
        return httpClient.postJson("/cv/ocr/driving", Map.of("img_url", Objects.requireNonNull(imgUrl)), Map.of(), JsonNode.class);
    }

    /**
     * 驾驶证 OCR 识别 (/cv/ocr/drivinglicense)
     */
    public JsonNode ocrDrivingLicense(String imgUrl) {
        return httpClient.postJson("/cv/ocr/drivinglicense", Map.of("img_url", Objects.requireNonNull(imgUrl)), Map.of(), JsonNode.class);
    }

    /**
     * 营业执照 OCR 识别 (/cv/ocr/bizlicense)
     */
    public JsonNode ocrBizLicense(String imgUrl) {
        return httpClient.postJson("/cv/ocr/bizlicense", Map.of("img_url", Objects.requireNonNull(imgUrl)), Map.of(), JsonNode.class);
    }

    /**
     * 通用印刷体 OCR 识别 (/cv/ocr/comm)
     */
    public JsonNode ocrCommon(String imgUrl) {
        return httpClient.postJson("/cv/ocr/comm", Map.of("img_url", Objects.requireNonNull(imgUrl)), Map.of(), JsonNode.class);
    }

    // ==================== 2. AI 语音与翻译接口 ====================

    /**
     * 提交语音进行识别 (/cgi-bin/media/voice/addvoicetorecofortext)
     *
     * @param voiceId   语音唯一标识
     * @param format    文件格式（如 mp3 / speex）
     * @param lang      语言，zh_CN 或 en_US
     * @param voiceData 语音二进制数据
     */
    public WeChatResponse submitVoiceForRecognition(String voiceId, String format, String lang, byte[] voiceData) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("format", format != null ? format : "mp3");
        params.put("voice_id", Objects.requireNonNull(voiceId));
        params.put("lang", lang != null ? lang : "zh_CN");
        return httpClient.postRawBytes("/cgi-bin/media/voice/addvoicetorecofortext", params, voiceData, "application/octet-stream", WeChatResponse.class);
    }

    /**
     * 获取语音识别结果 (/cgi-bin/media/voice/queryrecoresultfortext)
     */
    public String queryVoiceRecognitionResult(String voiceId, String lang) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("voice_id", Objects.requireNonNull(voiceId));
        params.put("lang", lang != null ? lang : "zh_CN");
        JsonNode node = httpClient.postJson("/cgi-bin/media/voice/queryrecoresultfortext", params, Map.of(), JsonNode.class);
        return node.path("result").asText();
    }

    /**
     * 微信翻译 (/cgi-bin/media/voice/translatecontent)
     *
     * @param lfrom   源语言，zh_CN 或 en_US
     * @param lto     目标语言，zh_CN 或 en_US
     * @param content 要翻译的文本内容
     */
    public JsonNode translateContent(String lfrom, String lto, String content) {
        Map<String, String> params = Map.of(
                "lfrom", Objects.requireNonNull(lfrom),
                "lto", Objects.requireNonNull(lto)
        );
        return httpClient.postRawBytes(
                "/cgi-bin/media/voice/translatecontent",
                params,
                Objects.requireNonNull(content).getBytes(StandardCharsets.UTF_8),
                "text/plain; charset=UTF-8",
                JsonNode.class
        );
    }

    /**
     * 语义理解 (/semantic/semproxy/search)
     */
    public JsonNode semanticSearch(Map<String, Object> requestBody) {
        return httpClient.postJson("/semantic/semproxy/search", null, Objects.requireNonNull(requestBody), JsonNode.class);
    }
}
