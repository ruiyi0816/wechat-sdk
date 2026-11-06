package com.wechat.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 微信服务端 API 通用返回结果基类
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeChatResponse {

    @JsonProperty("errcode")
    private int errCode;

    @JsonProperty("errmsg")
    private String errMsg;

    @JsonProperty("rid")
    private String rid;

    public WeChatResponse() {
    }

    public WeChatResponse(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public boolean isSuccess() {
        return errCode == 0;
    }

    @Override
    public String toString() {
        return "WeChatResponse{" +
                "errCode=" + errCode +
                ", errMsg='" + errMsg + '\'' +
                ", rid='" + rid + '\'' +
                '}';
    }
}
