package com.wechat.sdk.model.material;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wechat.sdk.model.WeChatResponse;

/**
 * 永久素材总数统计响应 (/cgi-bin/material/get_materialcount)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaterialCountResponse extends WeChatResponse {

    @JsonProperty("voice_count")
    private int voiceCount;

    @JsonProperty("video_count")
    private int videoCount;

    @JsonProperty("image_count")
    private int imageCount;

    @JsonProperty("news_count")
    private int newsCount;

    public int getVoiceCount() {
        return voiceCount;
    }

    public void setVoiceCount(int voiceCount) {
        this.voiceCount = voiceCount;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public int getNewsCount() {
        return newsCount;
    }

    public void setNewsCount(int newsCount) {
        this.newsCount = newsCount;
    }
}
