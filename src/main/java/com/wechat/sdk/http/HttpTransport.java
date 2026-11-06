package com.wechat.sdk.http;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * 可插拔的 HTTP 传输层接口（默认基于 JDK 21 {@link java.net.http.HttpClient} 实现）
 */
@FunctionalInterface
public interface HttpTransport {

    /**
     * 执行 HTTP 请求并返回原始字节响应
     */
    RawHttpResponse execute(RawHttpRequest request) throws IOException, InterruptedException;

    record RawHttpRequest(
            String method,
            URI uri,
            Map<String, String> headers,
            byte[] body
    ) {
    }

    record RawHttpResponse(
            int statusCode,
            Map<String, List<String>> headers,
            byte[] body
    ) {
        public String firstHeader(String name) {
            if (headers == null) {
                return null;
            }
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                    List<String> values = entry.getValue();
                    if (values != null && !values.isEmpty()) {
                        return values.getFirst();
                    }
                }
            }
            return null;
        }
    }
}
