package com.wechat.sdk.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wechat.sdk.exception.WeChatException;

/**
 * JSON 序列化与反序列化工具类
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private JsonUtils() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * 序列化对象为 JSON 字符串。
     * <p>使用 JDK 21 的 switch 模式匹配（JEP 441，含 {@code case null}）统一处理
     * null、原始 JSON 字符串与普通对象三种情况。
     */
    public static String toJson(Object obj) {
        return switch (obj) {
            case null -> "{}";
            case String s -> s;
            default -> serialize(obj);
        };
    }

    private static String serialize(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new WeChatException("Failed to serialize object to JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new WeChatException("Failed to parse JSON to " + clazz.getSimpleName() + ": " + json, e);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new WeChatException("Failed to parse JSON: " + json, e);
        }
    }

    public static JsonNode readTree(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new WeChatException("Failed to parse JSON tree: " + json, e);
        }
    }
}
