package com.wechat.sdk.exception;

/**
 * 微信服务端 API 调用异常（包含 errcode、errmsg、rid 等诊断信息）
 */
public class WeChatApiException extends WeChatException {

    private final int errCode;
    private final String errMsg;
    private final String rid;
    private final String apiPath;

    public WeChatApiException(int errCode, String errMsg, String rid, String apiPath) {
        super(formatMessage(errCode, errMsg, rid, apiPath));
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.rid = rid;
        this.apiPath = apiPath;
    }

    private static String formatMessage(int errCode, String errMsg, String rid, String apiPath) {
        StringBuilder sb = new StringBuilder("WeChat API error");
        if (apiPath != null && !apiPath.isBlank()) {
            sb.append(" [").append(apiPath).append("]");
        }
        sb.append(": errcode=").append(errCode).append(", errmsg=").append(errMsg);
        if (rid != null && !rid.isBlank()) {
            sb.append(", rid=").append(rid);
        }
        return sb.toString();
    }

    public int getErrCode() {
        return errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public String getRid() {
        return rid;
    }

    public String getApiPath() {
        return apiPath;
    }

    /**
     * 判断是否为 access_token 失效或过期错误码：
     * 40001: 获取 access_token 时 AppSecret 错误，或者 access_token 无效
     * 40014: 不合法的 access_token
     * 42001: access_token 超时
     */
    public boolean isAccessTokenInvalid() {
        return errCode == 40001 || errCode == 40014 || errCode == 42001;
    }
}
