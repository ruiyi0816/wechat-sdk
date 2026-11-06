package com.wechat.sdk.model.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * 客服消息发送请求 (/cgi-bin/message/custom/send)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomMessageRequest {

    @JsonProperty("touser")
    private String toUser;

    @JsonProperty("msgtype")
    private String msgType;

    @JsonProperty("text")
    private Map<String, String> text;

    @JsonProperty("image")
    private Map<String, String> image;

    @JsonProperty("voice")
    private Map<String, String> voice;

    @JsonProperty("video")
    private VideoPayload video;

    @JsonProperty("music")
    private MusicPayload music;

    @JsonProperty("news")
    private Map<String, List<NewsArticle>> news;

    @JsonProperty("mpnewsarticle")
    private Map<String, String> mpNewsArticle;

    @JsonProperty("msgmenu")
    private MsgMenuPayload msgMenu;

    @JsonProperty("miniprogrampage")
    private MiniProgramPagePayload miniProgramPage;

    @JsonProperty("wxcard")
    private Map<String, String> wxCard;

    @JsonProperty("customservice")
    private Map<String, String> customService;

    public CustomMessageRequest() {
    }

    public static CustomMessageRequest ofText(String toUser, String content) {
        CustomMessageRequest req = new CustomMessageRequest();
        req.toUser = toUser;
        req.msgType = "text";
        req.text = Map.of("content", content);
        return req;
    }

    public static CustomMessageRequest ofImage(String toUser, String mediaId) {
        CustomMessageRequest req = new CustomMessageRequest();
        req.toUser = toUser;
        req.msgType = "image";
        req.image = Map.of("media_id", mediaId);
        return req;
    }

    public static CustomMessageRequest ofVoice(String toUser, String mediaId) {
        CustomMessageRequest req = new CustomMessageRequest();
        req.toUser = toUser;
        req.msgType = "voice";
        req.voice = Map.of("media_id", mediaId);
        return req;
    }

    public static CustomMessageRequest ofVideo(String toUser, String mediaId, String thumbMediaId, String title, String description) {
        CustomMessageRequest req = new CustomMessageRequest();
        req.toUser = toUser;
        req.msgType = "video";
        req.video = new VideoPayload(mediaId, thumbMediaId, title, description);
        return req;
    }

    public static CustomMessageRequest ofMusic(String toUser, String title, String description, String musicUrl, String hqMusicUrl, String thumbMediaId) {
        CustomMessageRequest req = new CustomMessageRequest();
        req.toUser = toUser;
        req.msgType = "music";
        req.music = new MusicPayload(title, description, musicUrl, hqMusicUrl, thumbMediaId);
        return req;
    }

    public static CustomMessageRequest ofNews(String toUser, List<NewsArticle> articles) {
        CustomMessageRequest req = new CustomMessageRequest();
        req.toUser = toUser;
        req.msgType = "news";
        req.news = Map.of("articles", articles);
        return req;
    }

    public static CustomMessageRequest ofMpNewsArticle(String toUser, String articleId) {
        CustomMessageRequest req = new CustomMessageRequest();
        req.toUser = toUser;
        req.msgType = "mpnewsarticle";
        req.mpNewsArticle = Map.of("article_id", articleId);
        return req;
    }

    public static CustomMessageRequest ofMiniProgramPage(String toUser, String title, String appId, String pagePath, String thumbMediaId) {
        CustomMessageRequest req = new CustomMessageRequest();
        req.toUser = toUser;
        req.msgType = "miniprogrampage";
        req.miniProgramPage = new MiniProgramPagePayload(title, appId, pagePath, thumbMediaId);
        return req;
    }

    public static CustomMessageRequest ofMsgMenu(String toUser, String headContent, List<MenuOption> list, String tailContent) {
        CustomMessageRequest req = new CustomMessageRequest();
        req.toUser = toUser;
        req.msgType = "msgmenu";
        req.msgMenu = new MsgMenuPayload(headContent, list, tailContent);
        return req;
    }

    /**
     * 指定以某个客服账号发送消息
     */
    public CustomMessageRequest withKfAccount(String kfAccount) {
        if (kfAccount != null && !kfAccount.isBlank()) {
            this.customService = Map.of("kf_account", kfAccount);
        }
        return this;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public Map<String, String> getText() {
        return text;
    }

    public void setText(Map<String, String> text) {
        this.text = text;
    }

    public Map<String, String> getImage() {
        return image;
    }

    public void setImage(Map<String, String> image) {
        this.image = image;
    }

    public Map<String, String> getVoice() {
        return voice;
    }

    public void setVoice(Map<String, String> voice) {
        this.voice = voice;
    }

    public VideoPayload getVideo() {
        return video;
    }

    public void setVideo(VideoPayload video) {
        this.video = video;
    }

    public MusicPayload getMusic() {
        return music;
    }

    public void setMusic(MusicPayload music) {
        this.music = music;
    }

    public Map<String, List<NewsArticle>> getNews() {
        return news;
    }

    public void setNews(Map<String, List<NewsArticle>> news) {
        this.news = news;
    }

    public Map<String, String> getMpNewsArticle() {
        return mpNewsArticle;
    }

    public void setMpNewsArticle(Map<String, String> mpNewsArticle) {
        this.mpNewsArticle = mpNewsArticle;
    }

    public MsgMenuPayload getMsgMenu() {
        return msgMenu;
    }

    public void setMsgMenu(MsgMenuPayload msgMenu) {
        this.msgMenu = msgMenu;
    }

    public MiniProgramPagePayload getMiniProgramPage() {
        return miniProgramPage;
    }

    public void setMiniProgramPage(MiniProgramPagePayload miniProgramPage) {
        this.miniProgramPage = miniProgramPage;
    }

    public Map<String, String> getWxCard() {
        return wxCard;
    }

    public void setWxCard(Map<String, String> wxCard) {
        this.wxCard = wxCard;
    }

    public Map<String, String> getCustomService() {
        return customService;
    }

    public void setCustomService(Map<String, String> customService) {
        this.customService = customService;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VideoPayload(
            @JsonProperty("media_id") String mediaId,
            @JsonProperty("thumb_media_id") String thumbMediaId,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MusicPayload(
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("musicurl") String musicUrl,
            @JsonProperty("hqmusicurl") String hqMusicUrl,
            @JsonProperty("thumb_media_id") String thumbMediaId
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record NewsArticle(
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("url") String url,
            @JsonProperty("picurl") String picUrl
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MiniProgramPagePayload(
            @JsonProperty("title") String title,
            @JsonProperty("appid") String appId,
            @JsonProperty("pagepath") String pagePath,
            @JsonProperty("thumb_media_id") String thumbMediaId
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MsgMenuPayload(
            @JsonProperty("head_content") String headContent,
            @JsonProperty("list") List<MenuOption> list,
            @JsonProperty("tail_content") String tailContent
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MenuOption(
            @JsonProperty("id") String id,
            @JsonProperty("content") String content
    ) {
    }
}
