package com.wechat.sdk.model.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wechat.sdk.model.WeChatResponse;

import java.util.List;

/**
 * 公众号关注用户基本信息 (/cgi-bin/user/info)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo extends WeChatResponse {

    @JsonProperty("subscribe")
    private int subscribe;

    @JsonProperty("openid")
    private String openId;

    @JsonProperty("language")
    private String language;

    @JsonProperty("subscribe_time")
    private long subscribeTime;

    @JsonProperty("unionid")
    private String unionId;

    @JsonProperty("remark")
    private String remark;

    @JsonProperty("groupid")
    private int groupId;

    @JsonProperty("tagid_list")
    private List<Integer> tagIdList;

    @JsonProperty("subscribe_scene")
    private String subscribeScene;

    @JsonProperty("qr_scene")
    private long qrScene;

    @JsonProperty("qr_scene_str")
    private String qrSceneStr;

    public int getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(int subscribe) {
        this.subscribe = subscribe;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getSubscribeTime() {
        return subscribeTime;
    }

    public void setSubscribeTime(long subscribeTime) {
        this.subscribeTime = subscribeTime;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public List<Integer> getTagIdList() {
        return tagIdList;
    }

    public void setTagIdList(List<Integer> tagIdList) {
        this.tagIdList = tagIdList;
    }

    public String getSubscribeScene() {
        return subscribeScene;
    }

    public void setSubscribeScene(String subscribeScene) {
        this.subscribeScene = subscribeScene;
    }

    public long getQrScene() {
        return qrScene;
    }

    public void setQrScene(long qrScene) {
        this.qrScene = qrScene;
    }

    public String getQrSceneStr() {
        return qrSceneStr;
    }

    public void setQrSceneStr(String qrSceneStr) {
        this.qrSceneStr = qrSceneStr;
    }
}
