package com.wechat.sdk.model.material;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 草稿箱与图文消息文章实体
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DraftArticle {

    @JsonProperty("article_type")
    private String articleType = "news";

    @JsonProperty("title")
    private String title;

    @JsonProperty("author")
    private String author;

    @JsonProperty("digest")
    private String digest;

    @JsonProperty("content")
    private String content;

    @JsonProperty("content_source_url")
    private String contentSourceUrl;

    @JsonProperty("thumb_media_id")
    private String thumbMediaId;

    @JsonProperty("need_open_comment")
    private Integer needOpenComment;

    @JsonProperty("only_fans_can_comment")
    private Integer onlyFansCanComment;

    @JsonProperty("pic_crop_235_1")
    private String picCrop2351;

    @JsonProperty("pic_crop_1_1")
    private String picCrop11;

    @JsonProperty("url")
    private String url;

    public DraftArticle() {
    }

    public DraftArticle(String title, String content, String thumbMediaId) {
        this.title = title;
        this.content = content;
        this.thumbMediaId = thumbMediaId;
    }

    public String getArticleType() {
        return articleType;
    }

    public void setArticleType(String articleType) {
        this.articleType = articleType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentSourceUrl() {
        return contentSourceUrl;
    }

    public void setContentSourceUrl(String contentSourceUrl) {
        this.contentSourceUrl = contentSourceUrl;
    }

    public String getThumbMediaId() {
        return thumbMediaId;
    }

    public void setThumbMediaId(String thumbMediaId) {
        this.thumbMediaId = thumbMediaId;
    }

    public Integer getNeedOpenComment() {
        return needOpenComment;
    }

    public void setNeedOpenComment(Integer needOpenComment) {
        this.needOpenComment = needOpenComment;
    }

    public Integer getOnlyFansCanComment() {
        return onlyFansCanComment;
    }

    public void setOnlyFansCanComment(Integer onlyFansCanComment) {
        this.onlyFansCanComment = onlyFansCanComment;
    }

    public String getPicCrop2351() {
        return picCrop2351;
    }

    public void setPicCrop2351(String picCrop2351) {
        this.picCrop2351 = picCrop2351;
    }

    public String getPicCrop11() {
        return picCrop11;
    }

    public void setPicCrop11(String picCrop11) {
        this.picCrop11 = picCrop11;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
