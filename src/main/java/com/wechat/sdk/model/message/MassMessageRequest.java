package com.wechat.sdk.model.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wechat.sdk.model.WeChatResponse;

import java.util.List;
import java.util.Map;

/**
 * 高级群发接口请求与响应模型 (/cgi-bin/message/mass/*)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MassMessageRequest {

    @JsonProperty("filter")
    private Filter filter;

    @JsonProperty("touser")
    private Object toUser; // List<String> (OpenID列表群发) 或 String (预览接口)

    @JsonProperty("towxname")
    private String toWxName; // 预览接口微信号

    @JsonProperty("msgtype")
    private String msgType;

    @JsonProperty("mpnews")
    private Map<String, String> mpNews;

    @JsonProperty("text")
    private Map<String, String> text;

    @JsonProperty("voice")
    private Map<String, String> voice;

    @JsonProperty("images")
    private ImagesPayload images;

    @JsonProperty("mpvideo")
    private Map<String, String> mpVideo;

    @JsonProperty("wxcard")
    private Map<String, String> wxCard;

    @JsonProperty("send_ignore_reprint")
    private Integer sendIgnoreReprint;

    @JsonProperty("clientmsgid")
    private String clientMsgId;

    public static MassMessageRequest byTagMpNews(boolean isToAll, Integer tagId, String mediaId, int sendIgnoreReprint) {
        MassMessageRequest req = new MassMessageRequest();
        req.filter = new Filter(isToAll, tagId);
        req.msgType = "mpnews";
        req.mpNews = Map.of("media_id", mediaId);
        req.sendIgnoreReprint = sendIgnoreReprint;
        return req;
    }

    public static MassMessageRequest byTagText(boolean isToAll, Integer tagId, String content) {
        MassMessageRequest req = new MassMessageRequest();
        req.filter = new Filter(isToAll, tagId);
        req.msgType = "text";
        req.text = Map.of("content", content);
        return req;
    }

    public static MassMessageRequest byOpenIdsText(List<String> openIds, String content) {
        MassMessageRequest req = new MassMessageRequest();
        req.toUser = openIds;
        req.msgType = "text";
        req.text = Map.of("content", content);
        return req;
    }

    public static MassMessageRequest byOpenIdsMpNews(List<String> openIds, String mediaId, int sendIgnoreReprint) {
        MassMessageRequest req = new MassMessageRequest();
        req.toUser = openIds;
        req.msgType = "mpnews";
        req.mpNews = Map.of("media_id", mediaId);
        req.sendIgnoreReprint = sendIgnoreReprint;
        return req;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Object getToUser() {
        return toUser;
    }

    public void setToUser(Object toUser) {
        this.toUser = toUser;
    }

    public String getToWxName() {
        return toWxName;
    }

    public void setToWxName(String toWxName) {
        this.toWxName = toWxName;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public Map<String, String> getMpNews() {
        return mpNews;
    }

    public void setMpNews(Map<String, String> mpNews) {
        this.mpNews = mpNews;
    }

    public Map<String, String> getText() {
        return text;
    }

    public void setText(Map<String, String> text) {
        this.text = text;
    }

    public Map<String, String> getVoice() {
        return voice;
    }

    public void setVoice(Map<String, String> voice) {
        this.voice = voice;
    }

    public ImagesPayload getImages() {
        return images;
    }

    public void setImages(ImagesPayload images) {
        this.images = images;
    }

    public Map<String, String> getMpVideo() {
        return mpVideo;
    }

    public void setMpVideo(Map<String, String> mpVideo) {
        this.mpVideo = mpVideo;
    }

    public Map<String, String> getWxCard() {
        return wxCard;
    }

    public void setWxCard(Map<String, String> wxCard) {
        this.wxCard = wxCard;
    }

    public Integer getSendIgnoreReprint() {
        return sendIgnoreReprint;
    }

    public void setSendIgnoreReprint(Integer sendIgnoreReprint) {
        this.sendIgnoreReprint = sendIgnoreReprint;
    }

    public String getClientMsgId() {
        return clientMsgId;
    }

    public void setClientMsgId(String clientMsgId) {
        this.clientMsgId = clientMsgId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Filter(
            @JsonProperty("is_to_all") boolean isToAll,
            @JsonProperty("tag_id") Integer tagId
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ImagesPayload(
            @JsonProperty("media_ids") List<String> mediaIds,
            @JsonProperty("recommend") String recommend,
            @JsonProperty("need_open_comment") Integer needOpenComment,
            @JsonProperty("only_fans_can_comment") Integer onlyFansCanComment
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MassSendResponse extends WeChatResponse {
        @JsonProperty("msg_id")
        private Long msgId;

        @JsonProperty("msg_data_id")
        private Long msgDataId;

        @JsonProperty("msg_status")
        private String msgStatus;

        public Long getMsgId() {
            return msgId;
        }

        public void setMsgId(Long msgId) {
            this.msgId = msgId;
        }

        public Long getMsgDataId() {
            return msgDataId;
        }

        public void setMsgDataId(Long msgDataId) {
            this.msgDataId = msgDataId;
        }

        public String getMsgStatus() {
            return msgStatus;
        }

        public void setMsgStatus(String msgStatus) {
            this.msgStatus = msgStatus;
        }
    }
}
